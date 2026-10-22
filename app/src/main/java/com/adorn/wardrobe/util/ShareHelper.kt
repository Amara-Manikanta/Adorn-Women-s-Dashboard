package com.adorn.wardrobe.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.adorn.wardrobe.data.model.AccessoryItem
import java.io.File

object ShareHelper {

    fun shareProduct(context: Context, item: AccessoryItem) {
        val sb = StringBuilder()
        sb.append("✨ ${item.title}\n\n")

        if (item.brand.isNotBlank()) {
            sb.append("🏷️ Brand: ${item.brand}\n")
        }
        sb.append("📂 Category: ${item.category.displayName}\n")
        if (item.metal.displayName.isNotBlank()) {
            sb.append("💎 Material: ${item.metal.displayName}")
            if (item.purity.isNotBlank()) sb.append(" (${item.purity})")
            sb.append("\n")
        }
        if (item.dimensions.isNotBlank()) {
            sb.append("📏 Size / Dimensions: ${item.dimensions}\n")
        }
        val priceStr = if (item.isWishlist) item.formattedTargetPrice else item.formattedPrice
        if (priceStr != "Not specified") {
            val priceLabel = if (item.isWishlist) "Target Budget" else "Estimated Value"
            sb.append("💰 $priceLabel: $priceStr\n")
        }
        if (item.storeUrl.isNotBlank()) {
            sb.append("🛍️ Store Link: ${item.storeUrl}\n")
        }
        if (item.notes.isNotBlank()) {
            sb.append("📝 Notes: ${item.notes}\n")
        }
        sb.append("\nShared via Adorn — Luxury Wardrobe & Stylist ✨")

        val shareText = sb.toString()

        val primaryPath = item.primaryImageUri
        val imageFile = if (primaryPath.isNotBlank() && primaryPath.startsWith("/")) {
            File(primaryPath)
        } else null

        if (imageFile != null && imageFile.exists()) {
            try {
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, item.title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Share ${item.title}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Text share fallback
        try {
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, item.title)
            }
            val chooser = Intent.createChooser(textIntent, "Share ${item.title}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open share sheet: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
