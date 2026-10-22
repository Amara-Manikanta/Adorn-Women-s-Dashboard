package com.adorn.wardrobe.data.model

import java.text.NumberFormat
import java.util.Locale

enum class WearCondition(
    val label: String,
    val description: String,
    val advice: String,
    val colorHex: Long,
    val isAging: Boolean
) {
    BRAND_NEW(
        label = "Brand New",
        description = "Unworn in wardrobe",
        advice = "Still has tags / fresh off rack. Time to style and take it out! ✨",
        colorHex = 0xFF2E7D32,
        isAging = false
    ),
    PRISTINE(
        label = "Like New",
        description = "Worn 1-3 times",
        advice = "In mint, pristine condition. Fabric & color are like day one.",
        colorHex = 0xFF1976D2,
        isAging = false
    ),
    IN_PRIME(
        label = "Loved / In Prime",
        description = "Worn 4-10 times",
        advice = "In peak wardrobe rotation. Looking fabulous with great cost-per-wear.",
        colorHex = 0xFF7B1FA2,
        isAging = false
    ),
    FREQUENT(
        label = "Staple Favorite",
        description = "Worn 11-25 times",
        advice = "A true classic staple! Check seams, gentle dry-clean recommended.",
        colorHex = 0xFFC2185B,
        isAging = false
    ),
    AGING(
        label = "Aging / Vintage",
        description = "Worn 25+ times",
        advice = "High mileage garment! Inspect for pilling, fading, or consider repurposing / styling with a fresh layer.",
        colorHex = 0xFFE65100,
        isAging = true
    );

    companion object {
        fun fromTimesWorn(times: Int): WearCondition {
            return when {
                times <= 0 -> BRAND_NEW
                times in 1..3 -> PRISTINE
                times in 4..10 -> IN_PRIME
                times in 11..25 -> FREQUENT
                else -> AGING
            }
        }
    }
}

data class AccessoryItem(
    val id: Long = 0,
    val title: String,
    val category: AccessoryCategory,
    val metal: MetalType,
    val purity: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0, // Initial / MRP price for Price Drop detection
    val weightGrams: Double? = null,
    val dimensions: String = "",
    val isSet: Boolean = false,
    val setIncludes: String = "",
    val occasion: OccasionType = OccasionType.DAILY,
    val storageLocation: String = "",
    val brand: String = "",
    val notes: String = "",
    val imageUris: List<String> = emptyList(),
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isWishlist: Boolean = false,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val targetPrice: Double = 0.0,
    val storeUrl: String = "",
    val timesWorn: Int = 0,
    val lastWornDate: Long = 0L
) {
    val hasStoreLink: Boolean
        get() = storeUrl.isNotBlank()

    val storeDisplayName: String
        get() {
            val lower = storeUrl.lowercase()
            return when {
                lower.contains("myntra.com") -> "Myntra"
                lower.contains("amazon.") -> "Amazon"
                lower.contains("nykaa.com") -> "Nykaa"
                lower.contains("ajio.com") -> "Ajio"
                lower.contains("zara.com") -> "Zara"
                lower.contains("tanishq.") -> "Tanishq"
                lower.contains("caratlane.") -> "CaratLane"
                lower.contains("tatacliq.") -> "Tata CLiQ"
                else -> "Store"
            }
        }

    val storeAppPackage: String?
        get() {
            val lower = storeUrl.lowercase()
            return when {
                lower.contains("myntra.com") -> "com.myntra.android"
                lower.contains("amazon.") -> "in.amazon.mShop.android.shopping"
                lower.contains("nykaa.com") -> "com.fsn.nykaa"
                lower.contains("ajio.com") -> "com.ril.ajio"
                lower.contains("tatacliq.") -> "com.tul.tatacliq"
                else -> null
            }
        }

    val primaryImageUri: String
        get() = imageUris.firstOrNull() ?: ""

    val imageUri: String
        get() = primaryImageUri

    /**
     * Confirms whether photos are stored safely offline in device storage
     * so that if online product links expire, are removed or deleted, images are not lost.
     */
    val isImageSavedLocally: Boolean
        get() = primaryImageUri.isNotBlank() && (primaryImageUri.startsWith("/") || primaryImageUri.startsWith("file://"))

    val displayPrice: Double
        get() = if (targetPrice > 0.0) targetPrice else price

    // ==========================================
    // PRICE DROP CALCULATION & FORMATTING
    // ==========================================

    val effectiveCurrentPrice: Double
        get() = if (price > 0.0) price else if (targetPrice > 0.0) targetPrice else 0.0

    val hasPriceDrop: Boolean
        get() = originalPrice > 0.0 && effectiveCurrentPrice > 0.0 && effectiveCurrentPrice < originalPrice

    val priceDropAmount: Double
        get() = if (hasPriceDrop) (originalPrice - effectiveCurrentPrice) else 0.0

    val priceDropPercent: Int
        get() = if (hasPriceDrop && originalPrice > 0.0) {
            (((originalPrice - effectiveCurrentPrice) / originalPrice) * 100).toInt()
        } else 0

    val formattedOriginalPrice: String
        get() = if (originalPrice > 0) formatCurrency(originalPrice) else ""

    val formattedPriceDropSavings: String
        get() = if (hasPriceDrop) formatCurrency(priceDropAmount) else ""

    // ==========================================
    // WEAR COUNT & AGING ANALYSIS
    // ==========================================

    val wearCondition: WearCondition
        get() = WearCondition.fromTimesWorn(timesWorn)

    val costPerWear: Double?
        get() = if (timesWorn > 0 && price > 0) (price / timesWorn) else null

    val formattedCostPerWear: String
        get() {
            val cpw = costPerWear ?: return "Unworn"
            return formatCurrency(cpw) + "/wear"
        }

    val formattedTargetPrice: String
        get() = if (targetPrice > 0) {
            formatCurrency(targetPrice)
        } else {
            formattedPrice
        }

    val formattedPrice: String
        get() = if (price > 0) {
            formatCurrency(price)
        } else {
            "Not specified"
        }

    private fun formatCurrency(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                maximumFractionDigits = 0
            }
            format.format(amount)
        } catch (e: Exception) {
            "₹" + String.format("%,.0f", amount)
        }
    }
}
