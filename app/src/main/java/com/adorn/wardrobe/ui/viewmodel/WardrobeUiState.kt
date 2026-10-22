package com.adorn.wardrobe.ui.viewmodel

import com.adorn.wardrobe.data.api.SuggestedLook
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.Trip
import com.adorn.wardrobe.data.model.TripItemWithAccessory

enum class WardrobeTab {
    GALLERY,
    WISHLIST,
    AI_STYLIST,
    TRIP_CHECKLIST
}

enum class SortOrder(val label: String) {
    RECENT("Recently Added"),
    PRICE_HIGH("Value: High to Low"),
    PRICE_LOW("Value: Low to High"),
    NAME("Name: A to Z")
}

data class WardrobeUiState(
    val items: List<AccessoryItem> = emptyList(),
    val filteredItems: List<AccessoryItem> = emptyList(),
    val selectedCategory: AccessoryCategory = AccessoryCategory.ALL,
    val selectedMetal: MetalType = MetalType.ALL,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.RECENT,
    val currentTab: WardrobeTab = WardrobeTab.GALLERY,
    val selectedItemForDetail: AccessoryItem? = null,
    val isAddEditSheetOpen: Boolean = false,
    val itemBeingEdited: AccessoryItem? = null,
    val isStylistLoading: Boolean = false,
    val suggestedLook: SuggestedLook? = null,
    val totalWardrobeValue: Double = 0.0,

    // Wishlist State
    val wishlistItems: List<AccessoryItem> = emptyList(),
    val filteredWishlistItems: List<AccessoryItem> = emptyList(),
    val selectedWishlistPriority: String = "ALL", // ALL, HIGH, MEDIUM, LOW
    val totalWishlistValue: Double = 0.0,

    // Multi-Select & Bulk Actions
    val isSelectionMode: Boolean = false,
    val selectedItemIds: Set<Long> = emptySet(),

    // Trips & Before/After Checklist State
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val selectedTripItems: List<TripItemWithAccessory> = emptyList(),
    val isCreateTripDialogOpen: Boolean = false,
    val isAddItemsToTripSheetOpen: Boolean = false,

    // Notice dialog or banner
    val statusMessage: String? = null
)
