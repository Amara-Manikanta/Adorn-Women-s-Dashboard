package com.adorn.wardrobe.data.local

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.adorn.wardrobe.data.model.AccessoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    private const val DIRECTORY_NAME = "wardrobe_images"
    private const val CAMERA_TEMP_DIR = "camera"

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, DIRECTORY_NAME).apply {
                if (!exists()) mkdirs()
            }
            val fileName = "accessory_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val destFile = File(dir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadWebImage(context: Context, urlString: String): String {
        return WebProductImporter.downloadSingleImage(context, urlString) ?: urlString.trim()
    }

    /**
     * Guarantees offline image safety:
     * If the input is already a local file path, verifies and returns it.
     * If it is a content URI, copies to internal app storage.
     * If it is a remote web URL, downloads and stores it locally so that even if
     * the product page or store link is later deleted or removed, the photo remains 100% available offline!
     */
    suspend fun saveOrCacheImageOffline(context: Context, rawUriOrUrl: String): String {
        val trimmed = rawUriOrUrl.trim()
        if (trimmed.isBlank()) return ""

        // 1. Already a local file in internal storage
        if (trimmed.startsWith("/")) {
            val file = File(trimmed)
            if (file.exists()) return file.absolutePath
        }

        // 2. Content URI from Photo Picker or Gallery
        if (trimmed.startsWith("content://")) {
            val local = copyUriToInternalStorage(context, Uri.parse(trimmed))
            if (local != null) return local
        }

        // 3. Web URL from online retailer / CDN: download to internal device disk
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val downloaded = downloadWebImage(context, trimmed)
            if (downloaded.isNotBlank() && downloaded.startsWith("/")) {
                return downloaded
            }
        }

        return trimmed
    }

    /**
     * Scans items and downloads any remote web images to internal flash storage,
     * ensuring that deleted/removed store links never cause missing images.
     */
    suspend fun cacheAllRemoteImages(
        context: Context,
        items: List<AccessoryItem>,
        onItemCached: suspend (AccessoryItem) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            items.forEach { item ->
                var changed = false
                val updatedUris = item.imageUris.map { uri ->
                    if (uri.startsWith("http://") || uri.startsWith("https://")) {
                        val localPath = downloadWebImage(context, uri)
                        if (localPath.startsWith("/")) {
                            changed = true
                            localPath
                        } else {
                            uri
                        }
                    } else {
                        uri
                    }
                }
                if (changed) {
                    val updatedItem = item.copy(imageUris = updatedUris)
                    onItemCached(updatedItem)
                }
            }
        }
    }

    fun createTempCameraUri(context: Context): Pair<Uri, File> {
        val cacheDir = File(context.cacheDir, CAMERA_TEMP_DIR).apply {
            if (!exists()) mkdirs()
        }
        val tempFile = File(cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(uri, tempFile)
    }

    fun saveSampleImageFromResources(context: Context, sampleName: String, bitmap: Bitmap): String {
        val dir = File(context.filesDir, DIRECTORY_NAME).apply {
            if (!exists()) mkdirs()
        }
        val file = File(dir, "$sampleName.jpg")
        if (!file.exists()) {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }
        return file.absolutePath
    }
}
