package com.adorn.wardrobe.data.model

data class Trip(
    val id: Long = 0,
    val name: String,
    val destination: String = "",
    val travelDate: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TripItemWithAccessory(
    val tripItemId: Long,
    val tripId: Long,
    val accessory: AccessoryItem,
    val isPacked: Boolean = false,
    val isReturned: Boolean = false,
    val notes: String = ""
)
