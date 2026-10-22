package com.adorn.wardrobe.data.local

import android.content.Context
import android.graphics.BitmapFactory
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.OccasionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

data class WebProductInfo(
    val title: String = "",
    val brand: String = "",
    val price: Double? = null,
    val category: AccessoryCategory? = null,
    val metalOrFabric: MetalType? = null,
    val purityOrDetail: String = "",
    val dimensions: String = "",
    val isSet: Boolean = false,
    val setIncludes: String = "",
    val occasion: OccasionType? = null,
    val notes: String = "",
    val imageLocalPaths: List<String> = emptyList(),
    val isProductPage: Boolean = false,
    val storeUrl: String = "",
    val errorMessage: String? = null
)

object WebProductImporter {

    private const val DIRECTORY_NAME = "wardrobe_images"
    private const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun importFromUrl(context: Context, rawInput: String): WebProductInfo {
        return withContext(Dispatchers.IO) {
            try {
                val cleanUrl = extractUrl(rawInput) ?: return@withContext WebProductInfo(
                    errorMessage = "Please enter a valid website or image link."
                )

                // 1. Direct Image URL check
                if (isDirectImageUrl(cleanUrl)) {
                    val localPath = downloadSingleImage(context, cleanUrl)
                    return@withContext if (localPath != null) {
                        WebProductInfo(
                            imageLocalPaths = listOf(localPath),
                            isProductPage = false
                        )
                    } else {
                        WebProductInfo(errorMessage = "Could not download image from the provided link.")
                    }
                }

                // 2. Web Product Page Fetch
                val connection = (URL(cleanUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 12000
                    readTimeout = 12000
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/*,*/*;q=0.8")
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                }
                connection.connect()

                val contentType = connection.contentType?.lowercase() ?: ""
                if (contentType.startsWith("image/")) {
                    val localPath = saveStreamToImageFile(context, connection.inputStream)
                    return@withContext if (localPath != null) {
                        WebProductInfo(
                            imageLocalPaths = listOf(localPath),
                            isProductPage = false
                        )
                    } else {
                        WebProductInfo(errorMessage = "Failed to save the image.")
                    }
                }

                // Read HTML up to 2MB
                val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    var count = 0
                    while (reader.readLine().also { line = it } != null && count < 2_000_000) {
                        sb.append(line).append("\n")
                        count += line!!.length
                    }
                    sb.toString()
                }

                // 3. Specialized Myntra PDP Parser
                if (cleanUrl.contains("myntra.com", ignoreCase = true) || html.contains("window.__myx")) {
                    val myntraInfo = parseMyntraPdp(context, cleanUrl, html)
                    if (myntraInfo != null && (myntraInfo.imageLocalPaths.isNotEmpty() || myntraInfo.title.isNotBlank())) {
                        return@withContext myntraInfo
                    }
                }

                // 4. Generic OpenGraph & Meta Tag Parser (Ajio, Nykaa, Zara, Amazon, etc.)
                val genericInfo = parseGenericProductPage(context, cleanUrl, html)
                if (genericInfo != null && (genericInfo.imageLocalPaths.isNotEmpty() || genericInfo.title.isNotBlank())) {
                    return@withContext genericInfo
                }

                WebProductInfo(errorMessage = "Could not detect product photos or details on this page.")
            } catch (e: Exception) {
                e.printStackTrace()
                WebProductInfo(errorMessage = "Error importing link: ${e.localizedMessage ?: "Network error"}")
            }
        }
    }

    private fun extractUrl(input: String): String? {
        val trimmed = input.trim()
        val matcher = Pattern.compile("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]+", Pattern.CASE_INSENSITIVE).matcher(trimmed)
        return if (matcher.find()) {
            matcher.group()
        } else if (trimmed.startsWith("www.")) {
            "https://$trimmed"
        } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else null
    }

    private fun isDirectImageUrl(url: String): Boolean {
        val clean = url.split("?").first().lowercase()
        return clean.endsWith(".jpg") || clean.endsWith(".jpeg") || clean.endsWith(".png") ||
               clean.endsWith(".webp") || clean.endsWith(".gif")
    }

    private suspend fun parseMyntraPdp(context: Context, url: String, html: String): WebProductInfo? {
        val marker = "window.__myx = "
        val startIdx = html.indexOf(marker)
        if (startIdx == -1) return null

        val endScript = html.indexOf("</script>", startIdx)
        if (endScript == -1) return null

        var jsonStr = html.substring(startIdx + marker.length, endScript).trim()
        if (jsonStr.endsWith(";")) jsonStr = jsonStr.dropLast(1).trim()

        return try {
            val root = JSONObject(jsonStr)
            val pdp = root.optJSONObject("pdpData") ?: return null

            val rawTitle = pdp.optString("name")
            val title = formatTitle(rawTitle)

            // Brand
            val brandObj = pdp.opt("brand")
            val brand = when (brandObj) {
                is JSONObject -> brandObj.optString("name")
                is String -> brandObj
                else -> ""
            }.replaceFirstChar { it.uppercase() }

            // Price (prefer discounted price over mrp)
            val priceObj = pdp.optJSONObject("price")
            val discPrice = priceObj?.optDouble("discounted", 0.0) ?: 0.0
            val mrp = pdp.optDouble("mrp", 0.0)
            val price = if (discPrice > 0.0) discPrice else if (mrp > 0.0) mrp else null

            // Article Attributes
            val attrs = pdp.optJSONObject("articleAttributes")
            val topFabric = attrs?.optString("Top Fabric") ?: ""
            val bottomFabric = attrs?.optString("Bottom Fabric") ?: ""
            val fabric = attrs?.optString("Fabric") ?: ""
            val occasionAttr = attrs?.optString("Occasions") ?: ""
            val packageContains = attrs?.optString("Package Contains") ?: ""
            val ornamentation = attrs?.optString("Ornamentation") ?: ""

            // Available Sizes
            val sizesArr = pdp.optJSONArray("sizes")
            val sizeList = mutableListOf<String>()
            if (sizesArr != null) {
                for (i in 0 until sizesArr.length()) {
                    val label = sizesArr.optJSONObject(i)?.optString("label")
                    if (!label.isNullOrBlank()) sizeList.add(label)
                }
            }
            val dimensions = if (sizeList.isNotEmpty()) {
                "Sizes: " + sizeList.joinToString(", ")
            } else ""

            // Category
            val urlLower = url.lowercase()
            val titleLower = title.lowercase()
            val category = inferCategory("$url $title")

            // Fabric / Metal
            val allText = "$topFabric $bottomFabric $fabric $titleLower $ornamentation".lowercase()
            val metalOrFabric = when {
                allText.contains("silk") -> MetalType.PURE_SILK
                allText.contains("cotton") || allText.contains("linen") -> MetalType.COTTON_LINEN
                allText.contains("georgette") || allText.contains("chiffon") || allText.contains("organza") -> MetalType.GEORGETTE_CHIFFON
                allText.contains("velvet") || allText.contains("satin") -> MetalType.VELVET_SATIN
                allText.contains("leather") -> MetalType.GENUINE_LEATHER
                allText.contains("gold") -> MetalType.GOLD_22K
                allText.contains("silver") -> MetalType.STERLING_SILVER
                allText.contains("diamond") -> MetalType.DIAMONDS
                else -> MetalType.PURE_SILK
            }

            // Matching Set detection
            val isSet = urlLower.contains("kurta-sets") || titleLower.contains("set") || packageContains.isNotBlank()
            val setIncludes = when {
                packageContains.isNotBlank() -> packageContains
                urlLower.contains("kurta-sets") -> "Kurta + Palazzos Set"
                isSet -> "Matching Coordinated Set"
                else -> ""
            }

            // Occasion
            val occasion = when {
                occasionAttr.contains("festive", ignoreCase = true) || occasionAttr.contains("wedding", ignoreCase = true) -> OccasionType.WEDDING
                occasionAttr.contains("party", ignoreCase = true) -> OccasionType.PARTY
                occasionAttr.contains("formal", ignoreCase = true) || occasionAttr.contains("office", ignoreCase = true) -> OccasionType.OFFICE
                else -> OccasionType.DAILY
            }

            // Extract high-res product photos
            val imageUrls = mutableListOf<String>()
            val albums = pdp.optJSONObject("media")?.optJSONArray("albums")
            if (albums != null) {
                for (i in 0 until albums.length()) {
                    val images = albums.optJSONObject(i)?.optJSONArray("images")
                    if (images != null) {
                        for (j in 0 until images.length()) {
                            val imgObj = images.optJSONObject(j)
                            val src = imgObj?.optString("src")?.ifBlank { null }
                                ?: imgObj?.optString("imageURL")
                            if (!src.isNullOrBlank()) {
                                val cleanSrc = src
                                    .replace("(\$height)", "1440")
                                    .replace("(\$qualityPercentage)", "85")
                                    .replace("(\$width)", "1080")
                                    .let { if (it.startsWith("http://")) "https://" + it.removePrefix("http://") else it }
                                if (!imageUrls.contains(cleanSrc)) {
                                    imageUrls.add(cleanSrc)
                                }
                            }
                        }
                    }
                }
            }

            // Download images (up to 6)
            val localPaths = mutableListOf<String>()
            imageUrls.take(6).forEach { imgUrl ->
                val local = downloadSingleImage(context, imgUrl)
                if (local != null) localPaths.add(local)
            }

            // Purity / details summary
            val purityDetail = listOfNotNull(
                topFabric.ifBlank { null },
                ornamentation.ifBlank { null }
            ).joinToString(" • ")

            val notes = "Imported from Myntra. ${packageContains.ifBlank { "" }}".trim()

            WebProductInfo(
                title = title,
                brand = brand,
                price = price,
                category = category,
                metalOrFabric = metalOrFabric,
                purityOrDetail = purityDetail,
                dimensions = dimensions,
                isSet = isSet,
                setIncludes = setIncludes,
                occasion = occasion,
                notes = notes,
                imageLocalPaths = localPaths,
                isProductPage = true,
                storeUrl = url
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun parseGenericProductPage(context: Context, url: String, html: String): WebProductInfo? {
        val title = extractMetaTag(html, "property", "og:title")
            ?: extractTagContent(html, "title")
            ?: ""

        val rawDescription = extractMetaTag(html, "property", "og:description")
            ?: extractMetaTag(html, "name", "description")
            ?: ""

        // Extract brand
        val brand = extractMetaTag(html, "property", "og:brand")
            ?: extractMetaTag(html, "name", "brand")
            ?: extractBrandFromUrl(url)

        // Extract price
        val priceStr = extractMetaTag(html, "property", "product:price:amount")
            ?: extractMetaTag(html, "property", "og:price:amount")
            ?: extractMetaTag(html, "itemprop", "price")
        val parsedPrice = priceStr?.toDoubleOrNull()

        // Extract category
        val category = inferCategory("$url $title $rawDescription")

        // Extract images
        val imageUrls = mutableListOf<String>()
        val imgMatches = listOfNotNull(
            extractMetaTag(html, "property", "og:image"),
            extractMetaTag(html, "name", "twitter:image"),
            extractMetaTag(html, "itemprop", "image")
        )
        imageUrls.addAll(imgMatches.distinct())

        if (imageUrls.isEmpty() && title.isBlank()) return null

        val localPaths = mutableListOf<String>()
        imageUrls.take(4).forEach { imgUrl ->
            val local = downloadSingleImage(context, imgUrl)
            if (local != null) localPaths.add(local)
        }

        return WebProductInfo(
            title = formatTitle(title),
            brand = brand,
            price = parsedPrice,
            category = category,
            notes = rawDescription.take(200),
            imageLocalPaths = localPaths,
            isProductPage = true,
            storeUrl = url
        )
    }

    fun inferCategory(rawText: String): AccessoryCategory {
        val text = rawText.lowercase()
        return when {
            // Sports & Activewear
            text.contains("sports") || text.contains("activewear") || text.contains("gym") ||
                text.contains("legging") || text.contains("yoga") || text.contains("workout") ||
                text.contains("dri-fit") || text.contains("athletic") || text.contains("running") -> AccessoryCategory.SPORTS_ATHLETIC

            // Sarees & Ethnic
            text.contains("saree") || text.contains("sari") || text.contains("lehenga") ||
                text.contains("anarkali") || text.contains("dupatta") || text.contains("ethnic") -> AccessoryCategory.SAREES_ETHNIC

            // Dresses & Gowns
            text.contains("dress") || text.contains("gown") || text.contains("frock") ||
                text.contains("jumpsuit") || text.contains("maxi") || text.contains("midi") -> AccessoryCategory.DRESSES_GOWNS

            // Kurtis & Tops
            text.contains("kurta") || text.contains("kurti") || text.contains("tunic") ||
                text.contains("top") || text.contains("shirt") || text.contains("blouse") ||
                text.contains("t-shirt") || text.contains("tee") -> AccessoryCategory.KURTIS_TOPS

            // Bottoms & Skirts
            text.contains("palazzo") || text.contains("skirt") || text.contains("trouser") ||
                text.contains("pant") || text.contains("jean") || text.contains("denim") -> AccessoryCategory.BOTTOMS_SKIRTS

            // Handbags
            text.contains("handbag") || text.contains("tote") || text.contains("shoulder bag") ||
                text.contains("satchel") -> AccessoryCategory.HANDBAGS

            // Clutches & Slings
            text.contains("clutch") || text.contains("sling") || text.contains("crossbody") ||
                text.contains("potli") || text.contains("pouch") || text.contains("wallet") -> AccessoryCategory.CLUTCHES_SLINGS

            // Watches
            text.contains("watch") || text.contains("timepiece") || text.contains("chronograph") -> AccessoryCategory.WATCHES

            // Earrings
            text.contains("earring") || text.contains("jhumka") || text.contains("stud") ||
                text.contains("hoop") || text.contains("chandbali") || text.contains("drop") -> AccessoryCategory.EARRINGS

            // Chains & Necklaces
            text.contains("necklace") || text.contains("chain") || text.contains("choker") ||
                text.contains("pendant") || text.contains("mangalsutra") || text.contains("haar") -> AccessoryCategory.CHAINS_NECKLACES

            // Bracelets & Bangles
            text.contains("bangle") || text.contains("bracelet") || text.contains("kada") ||
                text.contains("cuff") -> AccessoryCategory.BRACELETS_BANGLES

            // Rings
            text.contains("ring") || text.contains("solitaire") || text.contains("band") -> AccessoryCategory.RINGS

            // Anklets
            text.contains("anklet") || text.contains("payal") || text.contains("kolusu") -> AccessoryCategory.ANKLETS

            // Hair & Brooches
            text.contains("hair") || text.contains("brooch") || text.contains("pin") ||
                text.contains("clip") || text.contains("tiara") || text.contains("scrunchie") -> AccessoryCategory.HAIR_BROOCHES

            // Fallback: If it's a tongue cleaner, hygiene tool, or unknown product, safely assign OTHER!
            else -> AccessoryCategory.OTHER
        }
    }

    private fun extractBrandFromUrl(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("amazon.") -> "Amazon"
            lower.contains("myntra.com") -> "Myntra"
            lower.contains("nykaa.com") -> "Nykaa"
            lower.contains("ajio.com") -> "Ajio"
            lower.contains("zara.com") -> "Zara"
            lower.contains("h&m") || lower.contains("hm.com") -> "H&M"
            lower.contains("tanishq.") -> "Tanishq"
            lower.contains("caratlane.") -> "CaratLane"
            else -> ""
        }
    }

    private fun extractMetaTag(html: String, attrName: String, attrVal: String): String? {
        val pattern1 = Pattern.compile("""<meta[^>]+$attrName=["']$attrVal["'][^>]+content=["']([^"']+)["']""", Pattern.CASE_INSENSITIVE)
        val matcher1 = pattern1.matcher(html)
        if (matcher1.find()) return matcher1.group(1)

        val pattern2 = Pattern.compile("""<meta[^>]+content=["']([^"']+)["'][^>]+$attrName=["']$attrVal["']""", Pattern.CASE_INSENSITIVE)
        val matcher2 = pattern2.matcher(html)
        if (matcher2.find()) return matcher2.group(1)

        return null
    }

    private fun extractTagContent(html: String, tag: String): String? {
        val pattern = Pattern.compile("""<$tag[^>]*>(.*?)</$tag>""", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun formatTitle(raw: String): String {
        return raw.replace(Regex("""\s*\|\s*Myntra.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^Buy\s+""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*-\s*Apparel for Women.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*-\s*Kurta Sets for Women.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*:\s*Amazon\.[a-z.]+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*on\s*Amazon\.[a-z.]+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\|\s*Nykaa.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*Online\s+at\s+Best\s+Price.*$""", RegexOption.IGNORE_CASE), "")
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                if (word.isNotEmpty()) word.replaceFirstChar { it.uppercase() } else ""
            }
    }

    suspend fun downloadSingleImage(context: Context, imageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cleanUrl = imageUrl.trim()
                if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                    return@withContext null
                }

                val conn = (URL(cleanUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("User-Agent", USER_AGENT)
                }
                conn.connect()

                if (conn.responseCode in 200..299) {
                    saveStreamToImageFile(context, conn.inputStream)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun saveStreamToImageFile(context: Context, inputStream: java.io.InputStream): String? {
        return try {
            val dir = File(context.filesDir, DIRECTORY_NAME).apply {
                if (!exists()) mkdirs()
            }
            val fileName = "store_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val destFile = File(dir, fileName)

            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Verify that the file is actually a valid image and not an HTML error or text
            if (isValidImageFile(destFile)) {
                destFile.absolutePath
            } else {
                destFile.delete()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isValidImageFile(file: File): Boolean {
        if (!file.exists() || file.length() < 300) return false

        // Check header bytes (JPEG, PNG, WEBP, GIF)
        try {
            val header = ByteArray(12)
            FileInputStream(file).use { it.read(header) }

            // JPEG magic: FF D8 FF
            val isJpeg = header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()
            // PNG magic: 89 50 4E 47
            val isPng = header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()
            // WEBP: starts with "RIFF" and has "WEBP" at index 8
            val isWebp = header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() &&
                         header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
                         header[8] == 'W'.code.toByte() && header[9] == 'E'.code.toByte() &&
                         header[10] == 'B'.code.toByte() && header[11] == 'P'.code.toByte()
            // GIF: "GIF8"
            val isGif = header[0] == 'G'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte()

            if (isJpeg || isPng || isWebp || isGif) return true

            // Fallback: decode bounds
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            return options.outWidth > 0 && options.outHeight > 0
        } catch (e: Exception) {
            return false
        }
    }
}
