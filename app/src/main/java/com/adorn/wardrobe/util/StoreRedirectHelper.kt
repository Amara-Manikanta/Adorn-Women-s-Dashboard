package com.adorn.wardrobe.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object StoreRedirectHelper {

    fun openStore(context: Context, rawUrl: String) {
        val cleanUrl = rawUrl.trim()
        if (cleanUrl.isBlank()) {
            Toast.makeText(context, "No store link available", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedUrl = if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            "https://$cleanUrl"
        } else {
            cleanUrl
        }

        val uri = try {
            Uri.parse(formattedUrl)
        } catch (e: Exception) {
            Toast.makeText(context, "Invalid link: $cleanUrl", Toast.LENGTH_SHORT).show()
            return
        }

        val host = uri.host?.lowercase() ?: ""

        val targetPackage = when {
            host.contains("myntra.com") -> "com.myntra.android"
            host.contains("amazon.in") -> "in.amazon.mShop.android.shopping"
            host.contains("amazon.") -> "com.amazon.mShop.android.shopping"
            host.contains("nykaa.com") -> "com.fsn.nykaa"
            host.contains("ajio.com") -> "com.ril.ajio"
            host.contains("tatacliq.com") -> "com.tul.tatacliq"
            host.contains("zara.com") -> "com.inditex.zara"
            else -> null
        }

        // 1. Try launching native app first if package is known
        if (targetPackage != null) {
            try {
                val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(targetPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(appIntent)
                return
            } catch (e: ActivityNotFoundException) {
                // Native app not installed on this device: fall through to browser!
            } catch (e: Exception) {
                // Ignore and fall through to browser
            }
        }

        // 2. Fallback to default browser or Android App Link resolver
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
