package com.adorn.wardrobe.ml

import android.graphics.Color
import com.adorn.wardrobe.data.model.AccessoryItem
import kotlin.math.abs
import kotlin.math.min

enum class ColorHarmonyType(
    val title: String,
    val badge: String,
    val description: String
) {
    MONOCHROME(
        title = "Monochrome Sophistication",
        badge = "✨ Tone on Tone",
        description = "Unified tonal elegance with subtle texture variation."
    ),
    ANALOGOUS(
        title = "Analogous Serenity",
        badge = "🌿 Gentle Harmony",
        description = "Harmonious adjacent colors that naturally blend and soothe."
    ),
    COMPLEMENTARY(
        title = "Complementary Contrast",
        badge = "🔥 Bold Contrast",
        description = "High-impact, striking contrast that commands the spotlight."
    ),
    TRIADIC(
        title = "Triadic Royal Balance",
        badge = "👑 Festive Vibrance",
        description = "Balanced, rich color wheel triad for celebrations & weddings."
    ),
    METALLIC_NEUTRAL(
        title = "Luxe Metallic Accent",
        badge = "💎 Precious Metal",
        description = "Precious gold, diamonds, or silver effortlessly pairing with any fabric."
    ),
    UNIVERSAL(
        title = "Timeless Palette",
        badge = "⭐ Classic Balance",
        description = "Versatile styling balance designed for day-to-night transitions."
    )
}

data class ColorMatchResult(
    val harmonyType: ColorHarmonyType,
    val score: Int,
    val explanation: String
)

object LocalColorMatcher {

    fun evaluateHarmony(hexColor1: String, hexColor2: String): ColorMatchResult {
        val c1 = parseHexOrNull(hexColor1)
        val c2 = parseHexOrNull(hexColor2)

        if (c1 == null || c2 == null) {
            return ColorMatchResult(
                harmonyType = ColorHarmonyType.UNIVERSAL,
                score = 88,
                explanation = "Classic pairing with versatile neutral undertones."
            )
        }

        val hsv1 = FloatArray(3)
        val hsv2 = FloatArray(3)
        Color.colorToHSV(c1, hsv1)
        Color.colorToHSV(c2, hsv2)

        val sat1 = hsv1[1]
        val sat2 = hsv2[1]
        val val1 = hsv1[2]
        val val2 = hsv2[2]

        // Neutral / Metallic check (low saturation or black/white)
        val isNeutral1 = sat1 < 0.18f || val1 < 0.20f || val1 > 0.88f
        val isNeutral2 = sat2 < 0.18f || val2 < 0.20f || val2 > 0.88f

        if (isNeutral1 || isNeutral2) {
            return ColorMatchResult(
                harmonyType = ColorHarmonyType.METALLIC_NEUTRAL,
                score = 96,
                explanation = "Neutral & metallic undertones elevate the vibrant fabric without competing for attention."
            )
        }

        // Calculate angular hue difference on the 360° color wheel
        val hue1 = hsv1[0]
        val hue2 = hsv2[0]
        var diff = abs(hue1 - hue2)
        if (diff > 180f) {
            diff = 360f - diff
        }

        return when {
            diff < 18f -> {
                ColorMatchResult(
                    harmonyType = ColorHarmonyType.MONOCHROME,
                    score = 94,
                    explanation = "Pure tonal monochrome — sophisticated, elongating, and polished."
                )
            }
            diff in 18f..48f -> {
                ColorMatchResult(
                    harmonyType = ColorHarmonyType.ANALOGOUS,
                    score = 92,
                    explanation = "Adjacent spectrum pairing providing an organic, soothing aesthetic."
                )
            }
            diff in 145f..205f -> {
                ColorMatchResult(
                    harmonyType = ColorHarmonyType.COMPLEMENTARY,
                    score = 98,
                    explanation = "Direct complementary contrast creates an electrifying high-fashion focal point."
                )
            }
            diff in 100f..140f -> {
                ColorMatchResult(
                    harmonyType = ColorHarmonyType.TRIADIC,
                    score = 95,
                    explanation = "Triadic balance — rich, lively, and perfectly suited for festive occasions."
                )
            }
            else -> {
                ColorMatchResult(
                    harmonyType = ColorHarmonyType.UNIVERSAL,
                    score = 89,
                    explanation = "Balanced harmony between color saturation and luminosity."
                )
            }
        }
    }

    fun evaluateOutfitHarmony(anchor: AccessoryItem?, accessories: List<AccessoryItem>): ColorMatchResult {
        val anchorHex = anchor?.dominantColorHex?.ifBlank { null }
            ?: return ColorMatchResult(
                harmonyType = ColorHarmonyType.METALLIC_NEUTRAL,
                score = 94,
                explanation = "Orchestrated around precious metal tones and coordinated silhouette."
            )

        val accessoryColors = accessories.mapNotNull { it.dominantColorHex.ifBlank { null } }
        if (accessoryColors.isEmpty()) {
            return ColorMatchResult(
                harmonyType = ColorHarmonyType.UNIVERSAL,
                score = 90,
                explanation = "Clean focal arrangement."
            )
        }

        // Compare anchor with primary accessory color
        val primaryAccColor = accessoryColors.first()
        return evaluateHarmony(anchorHex, primaryAccColor)
    }

    private fun parseHexOrNull(hex: String): Int? {
        val clean = hex.trim().removePrefix("#")
        return try {
            if (clean.length == 6) {
                Color.parseColor("#FF$clean")
            } else if (clean.length == 8) {
                Color.parseColor("#$clean")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
