package com.adorn.wardrobe.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.OccasionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class AdornImportResult(
    val itemsCount: Int,
    val wishlistCount: Int,
    val cutoutsRestored: Int,
    val message: String
)

object AdornBackupHelper {

    private const val BACKUP_DIR = "adorn_backups"

    /**
     * Packages all wardrobe items, metadata, original photos, and transparent cutouts
     * into a portable, offline .adorn backup archive (Zip format).
     */
    suspend fun createAdornBackup(context: Context): File = withContext(Dispatchers.IO) {
        val dbHelper = AccessoryDbHelper(context)
        val allItems = dbHelper.getAllItems()

        val backupDir = File(context.cacheDir, BACKUP_DIR).apply {
            if (!exists()) mkdirs()
        }

        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupFile = File(backupDir, "adorn_vault_backup_$dateStamp.adorn")

        // 1. Build Metadata JSON
        val rootJson = JSONObject().apply {
            put("app", "Adorn Wardrobe")
            put("version", 7)
            put("exportDate", System.currentTimeMillis())
            put("itemCount", allItems.size)

            val itemsArray = JSONArray()
            allItems.forEach { item ->
                val itemObj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("category", item.category.name)
                    put("metal", item.metal.name)
                    put("purity", item.purity)
                    put("price", item.price)
                    put("originalPrice", item.originalPrice)
                    put("weightGrams", item.weightGrams ?: JSONObject.NULL)
                    put("dimensions", item.dimensions)
                    put("isSet", item.isSet)
                    put("setIncludes", item.setIncludes)
                    put("occasion", item.occasion.name)
                    put("storageLocation", item.storageLocation)
                    put("brand", item.brand)
                    put("notes", item.notes)
                    put("isFavorite", item.isFavorite)
                    put("isWishlist", item.isWishlist)
                    put("priority", item.priority)
                    put("targetPrice", item.targetPrice)
                    put("storeUrl", item.storeUrl)
                    put("timesWorn", item.timesWorn)
                    put("lastWornDate", item.lastWornDate)
                    put("dominantColorHex", item.dominantColorHex)
                    put("accentColorHex", item.accentColorHex)
                    put("colorName", item.colorName)

                    // Reference image names relative to zip
                    val originalNames = JSONArray()
                    item.imageUris.forEach { uri ->
                        val f = File(uri)
                        if (f.exists()) originalNames.put(f.name)
                    }
                    put("originalImages", originalNames)

                    if (item.hasCutout) {
                        val cutoutFile = File(item.cutoutUri)
                        if (cutoutFile.exists()) {
                            put("cutoutImage", cutoutFile.name)
                        }
                    }
                }
                itemsArray.put(itemObj)
            }
            put("items", itemsArray)
        }

        // 2. Write Zip Archive
        ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zos ->
            // Write metadata.json
            val metaEntry = ZipEntry("metadata.json")
            zos.putNextEntry(metaEntry)
            zos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            val writtenFileNames = mutableSetOf<String>()

            // Write cutouts & originals
            allItems.forEach { item ->
                // Write Cutout PNG if present
                if (item.hasCutout) {
                    val cFile = File(item.cutoutUri)
                    if (cFile.exists() && writtenFileNames.add("cutouts/" + cFile.name)) {
                        val entry = ZipEntry("cutouts/" + cFile.name)
                        zos.putNextEntry(entry)
                        FileInputStream(cFile).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()
                    }
                }

                // Write Original Photos
                item.imageUris.forEach { u ->
                    val oFile = File(u)
                    if (oFile.exists() && writtenFileNames.add("originals/" + oFile.name)) {
                        val entry = ZipEntry("originals/" + oFile.name)
                        zos.putNextEntry(entry)
                        FileInputStream(oFile).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
        }

        backupFile
    }

    /**
     * Unpacks and restores an .adorn backup archive into app-private storage and database.
     */
    suspend fun restoreAdornBackup(context: Context, backupUri: Uri): AdornImportResult = withContext(Dispatchers.IO) {
        val dbHelper = AccessoryDbHelper(context)
        val filesDir = context.filesDir

        val imagesDir = File(filesDir, "wardrobe_images").apply { if (!exists()) mkdirs() }
        val cutoutsDir = File(filesDir, "wardrobe_cutouts").apply { if (!exists()) mkdirs() }

        var metadataJsonStr: String? = null
        val restoredFiles = mutableMapOf<String, String>() // zip entry path -> local absolute path

        // 1. Read Zip Stream
        context.contentResolver.openInputStream(backupUri)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    if (entryName == "metadata.json") {
                        metadataJsonStr = zis.bufferedReader(Charsets.UTF_8).readText()
                    } else if (entryName.startsWith("cutouts/")) {
                        val fileName = File(entryName).name
                        val destFile = File(cutoutsDir, fileName)
                        FileOutputStream(destFile).use { out -> zis.copyTo(out) }
                        restoredFiles[entryName] = destFile.absolutePath
                    } else if (entryName.startsWith("originals/")) {
                        val fileName = File(entryName).name
                        val destFile = File(imagesDir, fileName)
                        FileOutputStream(destFile).use { out -> zis.copyTo(out) }
                        restoredFiles[entryName] = destFile.absolutePath
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } ?: throw IllegalArgumentException("Could not open input stream from $backupUri")

        if (metadataJsonStr == null) {
            throw IllegalArgumentException("The selected file is not a valid .adorn backup (missing metadata.json)")
        }

        val rootJson = JSONObject(metadataJsonStr!!)
        val itemsArray = rootJson.optJSONArray("items") ?: JSONArray()

        var itemsCount = 0
        var wishlistCount = 0
        var cutoutsCount = 0

        for (i in 0 until itemsArray.length()) {
            val obj = itemsArray.getJSONObject(i)
            val title = obj.optString("title", "Untitled")
            val catStr = obj.optString("category", "OTHER")
            val metalStr = obj.optString("metal", "OTHER")
            val purity = obj.optString("purity", "")
            val price = obj.optDouble("price", 0.0)
            val origPrice = obj.optDouble("originalPrice", 0.0)
            val weight = if (obj.isNull("weightGrams")) null else obj.optDouble("weightGrams")
            val dimensions = obj.optString("dimensions", "")
            val isSet = obj.optBoolean("isSet", false)
            val setIncludes = obj.optString("setIncludes", "")
            val occStr = obj.optString("occasion", "DAILY")
            val location = obj.optString("storageLocation", "")
            val brand = obj.optString("brand", "")
            val notes = obj.optString("notes", "")
            val isFavorite = obj.optBoolean("isFavorite", false)
            val isWishlist = obj.optBoolean("isWishlist", false)
            val priority = obj.optString("priority", "MEDIUM")
            val targetPrice = obj.optDouble("targetPrice", 0.0)
            val storeUrl = obj.optString("storeUrl", "")
            val timesWorn = obj.optInt("timesWorn", 0)
            val lastWorn = obj.optLong("lastWornDate", 0L)
            val domColor = obj.optString("dominantColorHex", "")
            val accColor = obj.optString("accentColorHex", "")
            val colorName = obj.optString("colorName", "")

            // Remap original images
            val origImagesArray = obj.optJSONArray("originalImages") ?: JSONArray()
            val remappedImages = mutableListOf<String>()
            for (j in 0 until origImagesArray.length()) {
                val fName = origImagesArray.getString(j)
                val localPath = restoredFiles["originals/$fName"] ?: File(imagesDir, fName).absolutePath
                remappedImages.add(localPath)
            }

            // Remap cutout
            val cutoutFileName = obj.optString("cutoutImage", "")
            val remappedCutout = if (cutoutFileName.isNotBlank()) {
                val path = restoredFiles["cutouts/$cutoutFileName"] ?: File(cutoutsDir, cutoutFileName).absolutePath
                if (File(path).exists()) {
                    cutoutsCount++
                    path
                } else ""
            } else ""

            val newItem = AccessoryItem(
                title = title,
                category = AccessoryCategory.fromString(catStr),
                metal = MetalType.fromString(metalStr),
                purity = purity,
                price = price,
                originalPrice = origPrice,
                weightGrams = weight,
                dimensions = dimensions,
                isSet = isSet,
                setIncludes = setIncludes,
                occasion = OccasionType.fromString(occStr),
                storageLocation = location,
                brand = brand,
                notes = notes,
                imageUris = remappedImages,
                dateAdded = System.currentTimeMillis(),
                isFavorite = isFavorite,
                isWishlist = isWishlist,
                priority = priority,
                targetPrice = targetPrice,
                storeUrl = storeUrl,
                timesWorn = timesWorn,
                lastWornDate = lastWorn,
                cutoutUri = remappedCutout,
                dominantColorHex = domColor,
                accentColorHex = accColor,
                colorName = colorName
            )

            dbHelper.insertItem(newItem)
            if (isWishlist) wishlistCount++ else itemsCount++
        }

        AdornImportResult(
            itemsCount = itemsCount,
            wishlistCount = wishlistCount,
            cutoutsRestored = cutoutsCount,
            message = "Restored $itemsCount wardrobe pieces, $wishlistCount wishlist goals, and $cutoutsCount transparent cutouts!"
        )
    }

    /**
     * Shares the .adorn backup via Android system sharesheet (Drive, Files, AirDrop/Nearby, Email).
     */
    fun shareBackupFile(context: Context, backupFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Adorn Wardrobe Backup")
            putExtra(Intent.EXTRA_TEXT, "Adorn Luxury Wardrobe Backup Archive (${backupFile.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Save or Share .adorn Backup").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
