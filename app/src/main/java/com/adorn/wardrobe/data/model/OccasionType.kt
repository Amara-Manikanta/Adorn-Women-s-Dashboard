package com.adorn.wardrobe.data.model

enum class OccasionType(val displayName: String) {
    DAILY("Daily Wear"),
    OFFICE("Work & Office"),
    PARTY("Party & Evening"),
    WEDDING("Wedding & Festive"),
    CASUAL("Casual & Outings");

    companion object {
        fun fromString(value: String): OccasionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DAILY
        }
    }
}
