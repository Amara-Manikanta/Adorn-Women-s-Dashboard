package com.adorn.wardrobe.data.model

enum class MetalType(
    val displayName: String,
    val shortTag: String,
    val badgeHex: Long
) {
    ALL("All Materials", "All", 0xFF665560),
    // Precious Metals
    GOLD_22K("22K Yellow Gold", "22K Gold", 0xFFD4AF37),
    GOLD_18K("18K Yellow Gold", "18K Gold", 0xFFE0C068),
    ROSE_GOLD("Rose Gold", "Rose Gold", 0xFFC47B89),
    WHITE_GOLD("White Gold", "White Gold", 0xFF8E9AAF),
    STERLING_SILVER("925 Sterling Silver", "925 Silver", 0xFF9E9E9E),
    PLATINUM("Platinum (Pt950)", "Platinum", 0xFF607D8B),
    DIAMONDS("Diamonds & Gems", "Diamond", 0xFF4A90E2),
    PEARL("Freshwater Pearl", "Pearl", 0xFFB39D82),

    // Fabrics & Clothing Materials
    PURE_SILK("Pure Silk / Kanjeevaram", "Pure Silk", 0xFF8E24AA),
    COTTON_LINEN("Cotton & Linen", "Cotton", 0xFF00897B),
    GEORGETTE_CHIFFON("Georgette / Organza", "Georgette", 0xFFD81B60),
    VELVET_SATIN("Velvet & Satin", "Velvet", 0xFF3949AB),

    // Bag & Handbag Materials
    GENUINE_LEATHER("Genuine Leather", "Leather", 0xFF8D6E63),
    VEGAN_LEATHER("Vegan Leather", "Vegan Leather", 0xFF5D4037),
    EMBELLISHED_ZARI("Embellished / Silk / Zari", "Embellished", 0xFFAD1457),

    // Fashion / Other
    FASHION_JEWELRY("Fashion / Canvas / Crepe", "Fashion", 0xFF795548);

    val isFabric: Boolean
        get() = this == PURE_SILK || this == COTTON_LINEN || this == GEORGETTE_CHIFFON || this == VELVET_SATIN

    companion object {
        fun fromString(value: String): MetalType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: FASHION_JEWELRY
        }

        fun selectableMetals(): List<MetalType> {
            return entries.filter { it != ALL }
        }
    }
}
