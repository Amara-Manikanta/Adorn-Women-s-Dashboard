package com.adorn.wardrobe.data.api

import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.OccasionType
import kotlinx.coroutines.delay

data class OutfitStylingRequest(
    val occasion: String = "Wedding & Festive",
    val outfitColor: String = "Emerald Green",
    val neckline: String = "V-Neck",
    val mood: String = "Royal & Elegant",
    val selectedClothingItemId: Long? = null
)

data class SuggestedLook(
    val lookTitle: String,
    val stylistAdvice: String,
    val matchedItems: List<AccessoryItem>,
    val harmonyScore: Int = 98,
    val metalPalette: String = "Gold & Diamonds",
    val anchorClothing: AccessoryItem? = null,
    val colorHarmonyBadge: String = "✨ Harmonious Pairing"
)

/**
 * Service contract for the future API connection.
 * Can be connected to a REST API, Gemini / OpenAI Vision endpoint, or custom Python backend.
 */
interface JewelrySuggestionApiService {
    suspend fun getStylingSuggestions(
        request: OutfitStylingRequest,
        wardrobeItems: List<AccessoryItem>
    ): SuggestedLook
}

/**
 * Intelligent client-side stylist fallback while waiting for the remote backend API.
 * Uses color-harmony rules, metal matching, occasion filtering, and clothes pairing
 * from the user's actual uploaded wardrobe!
 */
class SmartStylistService : JewelrySuggestionApiService {

    override suspend fun getStylingSuggestions(
        request: OutfitStylingRequest,
        wardrobeItems: List<AccessoryItem>
    ): SuggestedLook {
        // Simulating network latency for realistic AI loading feel
        delay(600)

        if (wardrobeItems.isEmpty()) {
            return SuggestedLook(
                lookTitle = "Wardrobe is Empty",
                stylistAdvice = "Add your outfits and accessories (sarees, dresses, earrings, neckwear, bags, watch) to your gallery first so our AI Stylist can coordinate your look!",
                matchedItems = emptyList(),
                harmonyScore = 0
            )
        }

        // 1. Identify Anchor Clothing (Dress, Saree, Kurti, Gown)
        val anchorClothing = if (request.selectedClothingItemId != null) {
            wardrobeItems.firstOrNull { it.id == request.selectedClothingItemId }
        } else {
            // Pick a clothing item matching the occasion if available
            wardrobeItems.firstOrNull { it.category.isClothing && it.occasion.displayName.contains(request.occasion, ignoreCase = true) }
                ?: wardrobeItems.firstOrNull { it.category.isClothing }
        }

        // Prioritize pieces matching requested occasion or versatile pieces
        val occasionMatch = wardrobeItems.filter {
            it.occasion.displayName.contains(request.occasion, ignoreCase = true) ||
            request.occasion.contains(it.occasion.displayName, ignoreCase = true)
        }.ifEmpty { wardrobeItems }

        // Exclude the clothing item itself from accessory search so it doesn't duplicate
        val accessoryPool = if (anchorClothing != null) {
            occasionMatch.filter { it.id != anchorClothing.id }
        } else {
            occasionMatch
        }

        val allAccessoryPool = if (anchorClothing != null) {
            wardrobeItems.filter { it.id != anchorClothing.id }
        } else {
            wardrobeItems
        }

        // Check for matching jewelry sets (e.g., Chain + Earrings Set)
        val bestSet = accessoryPool.firstOrNull { it.isSet || it.category == AccessoryCategory.MATCHING_SETS }
            ?: allAccessoryPool.firstOrNull { it.isSet || it.category == AccessoryCategory.MATCHING_SETS }

        // Find primary earring (only if not already wearing a set with earrings)
        val bestEarring = if (bestSet != null) null else {
            accessoryPool.firstOrNull { it.category == AccessoryCategory.EARRINGS }
                ?: allAccessoryPool.firstOrNull { it.category == AccessoryCategory.EARRINGS }
        }

        // Find necklace / chain (only if not already wearing a set with necklace)
        val bestNecklace = if (bestSet != null) null else {
            accessoryPool.firstOrNull { it.category == AccessoryCategory.CHAINS_NECKLACES }
                ?: allAccessoryPool.firstOrNull { it.category == AccessoryCategory.CHAINS_NECKLACES }
        }

        // Find bag / clutch (Crucial accessory pairing!)
        val bestBag = accessoryPool.firstOrNull {
            it.category == AccessoryCategory.HANDBAGS || it.category == AccessoryCategory.CLUTCHES_SLINGS
        } ?: allAccessoryPool.firstOrNull {
            it.category == AccessoryCategory.HANDBAGS || it.category == AccessoryCategory.CLUTCHES_SLINGS
        }

        // Find watch or bracelet
        val bestWrist = accessoryPool.firstOrNull {
            it.category == AccessoryCategory.WATCHES || it.category == AccessoryCategory.BRACELETS_BANGLES
        } ?: allAccessoryPool.firstOrNull {
            it.category == AccessoryCategory.WATCHES || it.category == AccessoryCategory.BRACELETS_BANGLES
        }

        // Find ring
        val bestRing = accessoryPool.firstOrNull { it.category == AccessoryCategory.RINGS }
            ?: allAccessoryPool.firstOrNull { it.category == AccessoryCategory.RINGS }

        val curatedList = listOfNotNull(anchorClothing, bestSet, bestEarring, bestNecklace, bestBag, bestWrist, bestRing).distinctBy { it.id }

        val stylistAdvice = when {
            anchorClothing != null && anchorClothing.category == AccessoryCategory.SAREES_ETHNIC -> {
                val setNote = if (bestSet != null) "paired with your ${bestSet.title}" else "accented with your traditional neckwear"
                "Centering around your '${anchorClothing.title}' (${anchorClothing.metal.displayName}), we've orchestrated a majestic festive ensemble. The rich weaves are $setNote and a luxe clutch for timeless royal opulence."
            }
            anchorClothing != null && anchorClothing.category == AccessoryCategory.DRESSES_GOWNS -> {
                "Styling around your chic '${anchorClothing.title}': The silhouette commands refined elegance. We've paired it with sparkling solitaires, a luxury handbag, and delicate wrist accents for an effortless red-carpet glow."
            }
            anchorClothing != null -> {
                "Coordinated with your '${anchorClothing.title}': Balanced tones between your outfit's ${anchorClothing.metal.displayName} and matched accessories bring a polished, high-fashion statement."
            }
            bestSet != null ->
                "A coordinated matching set (${bestSet.setIncludes.ifBlank { bestSet.title }}) creates a flawless, regal harmony with your ${request.outfitColor} outfit. Paired with your matching bag, the look is pure celebration perfection."
            request.outfitColor.contains("Green", ignoreCase = true) ->
                "Warm yellow gold and radiant solitaire diamonds pop breathtakingly against rich ${request.outfitColor}. Pair with an embroidered clutch or structured leather handbag to elevate the royal silhouette."
            request.outfitColor.contains("Black", ignoreCase = true) ->
                "Monochrome noir pairs stunningly with platinum, white gold, or sparkling diamonds. A sleek leather bag and delicate watch complete the effortless Parisian chic look."
            request.outfitColor.contains("Red", ignoreCase = true) || request.outfitColor.contains("Pink", ignoreCase = true) ->
                "Rose gold and traditional 22K yellow gold create an exquisite festive harmony with warm crimson and blush tones. A classic bridal clutch completes the celebration ensemble."
            else ->
                "A balanced harmony: coordinating metallic tones across your jewelry and handbag hardware gives your ${request.occasion} look a polished, boutique finish."
        }

        val title = if (anchorClothing != null) {
            "Styled: ${anchorClothing.title}"
        } else {
            "Curated ${request.occasion} Ensemble"
        }

        // On-Device Local Color Theory Harmony Analysis
        val colorEval = com.adorn.wardrobe.ml.LocalColorMatcher.evaluateOutfitHarmony(
            anchorClothing,
            curatedList.filter { it.id != anchorClothing?.id }
        )

        val combinedAdvice = if (anchorClothing?.dominantColorHex?.isNotBlank() == true) {
            "$stylistAdvice\n\n🎨 Color Harmony (${colorEval.harmonyType.title}): ${colorEval.explanation}"
        } else {
            stylistAdvice
        }

        val calculatedScore = if (anchorClothing?.dominantColorHex?.isNotBlank() == true) {
            ((colorEval.score + (if (curatedList.size >= 4) 98 else if (curatedList.size >= 2) 92 else 85)) / 2)
        } else {
            if (curatedList.size >= 4) 98 else if (curatedList.size >= 2) 92 else 85
        }

        return SuggestedLook(
            lookTitle = title,
            stylistAdvice = combinedAdvice,
            matchedItems = curatedList,
            harmonyScore = calculatedScore,
            metalPalette = curatedList.map { it.metal.shortTag }.distinct().joinToString(" + "),
            anchorClothing = anchorClothing,
            colorHarmonyBadge = colorEval.harmonyType.badge
        )
    }
}
