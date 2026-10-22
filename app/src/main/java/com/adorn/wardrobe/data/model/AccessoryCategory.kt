package com.adorn.wardrobe.data.model

enum class AccessoryCategory(val displayName: String, val iconName: String) {
    ALL("All", "Sparkles"),
    SAREES_ETHNIC("Sarees & Ethnic", "Checkroom"),
    DRESSES_GOWNS("Dresses & Gowns", "DryCleaning"),
    KURTIS_TOPS("Kurtis & Tops", "WbSunny"),
    BOTTOMS_SKIRTS("Bottoms & Skirts", "Layers"),
    SPORTS_ATHLETIC("Sports & Activewear", "FitnessCenter"),
    MATCHING_SETS("Sets & Combos", "AutoAwesome"),
    EARRINGS("Earrings", "Sparkle"),
    CHAINS_NECKLACES("Chains & Necklaces", "Circle"),
    HANDBAGS("Handbags & Totes", "ShoppingBag"),
    CLUTCHES_SLINGS("Clutches & Slings", "WorkOutline"),
    WATCHES("Watches", "Watch"),
    BRACELETS_BANGLES("Bracelets & Bangles", "RadioChecked"),
    RINGS("Rings", "Star"),
    ANKLETS("Anklets", "Waves"),
    HAIR_BROOCHES("Hair & Brooches", "AutoAwesome"),
    OTHER("Other Accessories", "Category");

    val isClothing: Boolean
        get() = this == SAREES_ETHNIC || this == DRESSES_GOWNS || this == KURTIS_TOPS || this == BOTTOMS_SKIRTS || this == SPORTS_ATHLETIC

    companion object {
        fun fromString(value: String): AccessoryCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }

        fun selectableCategories(): List<AccessoryCategory> {
            return entries.filter { it != ALL }
        }

        fun clothingCategories(): List<AccessoryCategory> {
            return entries.filter { it.isClothing }
        }
    }
}
