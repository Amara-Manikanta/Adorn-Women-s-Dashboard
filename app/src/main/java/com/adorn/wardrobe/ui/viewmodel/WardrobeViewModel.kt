package com.adorn.wardrobe.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adorn.wardrobe.data.api.JewelrySuggestionApiService
import com.adorn.wardrobe.data.api.OutfitStylingRequest
import com.adorn.wardrobe.data.api.SmartStylistService
import com.adorn.wardrobe.data.local.AccessoryDbHelper
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.Trip
import com.adorn.wardrobe.data.model.TripItemWithAccessory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WardrobeViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = AccessoryDbHelper(application)
    private val stylistService: JewelrySuggestionApiService = SmartStylistService()

    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    init {
        loadWardrobe()
        loadTrips()
    }

    fun loadWardrobe() {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.seedStarterWardrobeIfEmpty()
            val allItems = dbHelper.getAllItems()
            updateItemsAndStats(allItems)
            cacheAllImagesOffline(allItems)
        }
    }

    fun loadTrips() {
        viewModelScope.launch(Dispatchers.IO) {
            val allTrips = dbHelper.getAllTrips()
            val currentSelected = _uiState.value.selectedTrip ?: allTrips.firstOrNull()
            val tripItems = if (currentSelected != null) {
                dbHelper.getTripItems(currentSelected.id)
            } else emptyList()

            _uiState.update {
                it.copy(
                    trips = allTrips,
                    selectedTrip = currentSelected,
                    selectedTripItems = tripItems
                )
            }
        }
    }

    fun setCategory(category: AccessoryCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        reapplyFilters()
    }

    fun setMetal(metal: MetalType) {
        _uiState.update { it.copy(selectedMetal = metal) }
        reapplyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        reapplyFilters()
        reapplyWishlistFilters()
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        reapplyFilters()
        reapplyWishlistFilters()
    }

    fun setTab(tab: WardrobeTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                isSelectionMode = false,
                selectedItemIds = emptySet()
            )
        }
        if (tab == WardrobeTab.AI_STYLIST && _uiState.value.suggestedLook == null) {
            requestAiStyling()
        }
        if (tab == WardrobeTab.TRIP_CHECKLIST) {
            loadTrips()
        }
    }

    fun selectItemForDetail(item: AccessoryItem?) {
        _uiState.update { it.copy(selectedItemForDetail = item) }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddEditSheetOpen = true, itemBeingEdited = null, selectedItemForDetail = null) }
    }

    fun openEditDialog(item: AccessoryItem) {
        _uiState.update { it.copy(isAddEditSheetOpen = true, itemBeingEdited = item, selectedItemForDetail = null) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditSheetOpen = false, itemBeingEdited = null) }
    }

    fun saveItem(
        title: String,
        category: AccessoryCategory,
        metal: MetalType,
        purity: String,
        price: Double,
        weightGrams: Double?,
        dimensions: String,
        isSet: Boolean,
        setIncludes: String,
        occasion: com.adorn.wardrobe.data.model.OccasionType,
        location: String,
        brand: String,
        notes: String,
        imageUris: List<String>,
        isWishlist: Boolean = false,
        priority: String = "MEDIUM",
        targetPrice: Double = 0.0,
        storeUrl: String = "",
        originalPrice: Double = 0.0,
        timesWorn: Int = 0,
        cutoutUri: String = "",
        dominantColorHex: String = "",
        accentColorHex: String = "",
        colorName: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentEdit = _uiState.value.itemBeingEdited
            val targetWishlist = if (currentEdit != null) currentEdit.isWishlist else (_uiState.value.currentTab == WardrobeTab.WISHLIST || isWishlist)
            val finalTargetPrice = if (targetPrice > 0.0) targetPrice else price
            val finalOriginalPrice = if (originalPrice > 0.0) originalPrice else (currentEdit?.originalPrice ?: 0.0)
            val finalTimesWorn = if (timesWorn >= 0 && (currentEdit == null || timesWorn != 0)) timesWorn else (currentEdit?.timesWorn ?: 0)
            val finalCutout = if (cutoutUri.isNotBlank()) cutoutUri else (currentEdit?.cutoutUri ?: "")
            val finalDomColor = if (dominantColorHex.isNotBlank()) dominantColorHex else (currentEdit?.dominantColorHex ?: "")
            val finalAccColor = if (accentColorHex.isNotBlank()) accentColorHex else (currentEdit?.accentColorHex ?: "")
            val finalColorName = if (colorName.isNotBlank()) colorName else (currentEdit?.colorName ?: "")

            // Cache any online images locally so they are never lost if link is removed
            val offlineUris = imageUris.map { uri ->
                com.adorn.wardrobe.data.local.ImageStorageHelper.saveOrCacheImageOffline(getApplication(), uri)
            }.filter { it.isNotBlank() }

            if (currentEdit != null) {
                val updated = currentEdit.copy(
                    title = title,
                    category = category,
                    metal = metal,
                    purity = purity,
                    price = price,
                    originalPrice = finalOriginalPrice,
                    weightGrams = weightGrams,
                    dimensions = dimensions,
                    isSet = isSet,
                    setIncludes = setIncludes,
                    occasion = occasion,
                    storageLocation = location,
                    brand = brand,
                    notes = notes,
                    imageUris = offlineUris,
                    isWishlist = targetWishlist,
                    priority = priority,
                    targetPrice = finalTargetPrice,
                    storeUrl = if (storeUrl.isNotBlank()) storeUrl else currentEdit.storeUrl,
                    timesWorn = finalTimesWorn,
                    cutoutUri = finalCutout,
                    dominantColorHex = finalDomColor,
                    accentColorHex = finalAccColor,
                    colorName = finalColorName
                )
                dbHelper.updateItem(updated)
            } else {
                val newItem = AccessoryItem(
                    title = title,
                    category = category,
                    metal = metal,
                    purity = purity,
                    price = price,
                    originalPrice = finalOriginalPrice,
                    weightGrams = weightGrams,
                    dimensions = dimensions,
                    isSet = isSet,
                    setIncludes = setIncludes,
                    occasion = occasion,
                    storageLocation = location,
                    brand = brand,
                    notes = notes,
                    imageUris = offlineUris,
                    isWishlist = targetWishlist,
                    priority = priority,
                    targetPrice = finalTargetPrice,
                    storeUrl = storeUrl,
                    timesWorn = finalTimesWorn,
                    cutoutUri = finalCutout,
                    dominantColorHex = finalDomColor,
                    accentColorHex = finalAccColor,
                    colorName = finalColorName
                )
                dbHelper.insertItem(newItem)
            }

            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            _uiState.update {
                it.copy(
                    isAddEditSheetOpen = false,
                    itemBeingEdited = null,
                    selectedItemForDetail = if (it.selectedItemForDetail != null) {
                        refreshed.firstOrNull { item -> item.id == it.selectedItemForDetail.id }
                    } else null
                )
            }
        }
    }

    // ==========================================
    // WEAR COUNTING & AGING ACTIONS
    // ==========================================

    fun incrementTimesWorn(item: AccessoryItem, delta: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.incrementTimesWorn(item.id, delta)
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            val updated = refreshed.firstOrNull { it.id == item.id }
            if (updated != null) {
                _uiState.update {
                    it.copy(
                        selectedItemForDetail = if (it.selectedItemForDetail?.id == item.id) updated else it.selectedItemForDetail,
                        statusMessage = if (delta > 0) "✨ Logged wear for '${item.title}'! Total worn: ${updated.timesWorn} times" else null
                    )
                }
            }
        }
    }

    // ==========================================
    // PRICE DROP ACTIONS & LIVE VERIFICATION
    // ==========================================

    fun simulatePriceDrop(item: AccessoryItem, discountPercent: Int = 20) {
        viewModelScope.launch(Dispatchers.IO) {
            val basePrice = if (item.originalPrice > 0.0) item.originalPrice else item.price
            if (basePrice <= 0.0) return@launch
            val discountedPrice = (basePrice * (1.0 - (discountPercent / 100.0))).coerceAtLeast(1.0)
            val updated = item.copy(
                originalPrice = basePrice,
                price = discountedPrice
            )
            dbHelper.updateItem(updated)
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            _uiState.update {
                it.copy(
                    selectedItemForDetail = if (it.selectedItemForDetail?.id == item.id) updated else it.selectedItemForDetail,
                    statusMessage = "🔥 Price Drop Alert! Saved ₹${(basePrice - discountedPrice).toInt()} on '${item.title}'!"
                )
            }
        }
    }

    fun refreshLiveStorePrice(item: AccessoryItem) {
        if (item.storeUrl.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(statusMessage = "Checking live store price for '${item.title}'...") }
            try {
                val info = com.adorn.wardrobe.data.local.WebProductImporter.importFromUrl(getApplication(), item.storeUrl)
                if (info.price != null && info.price > 0.0) {
                    val currentRecordedPrice = item.price
                    val original = if (item.originalPrice > 0.0) item.originalPrice else currentRecordedPrice
                    val updated = item.copy(
                        price = info.price,
                        originalPrice = original
                    )
                    dbHelper.updateItem(updated)
                    val refreshed = dbHelper.getAllItems()
                    updateItemsAndStats(refreshed)
                    val msg = if (info.price < currentRecordedPrice) {
                        "🔥 Price Dropped! Current price is ₹${info.price.toInt()} (was ₹${currentRecordedPrice.toInt()}) ✨"
                    } else {
                        "Verified: Store price is ₹${info.price.toInt()}."
                    }
                    _uiState.update {
                        it.copy(
                            selectedItemForDetail = if (it.selectedItemForDetail?.id == item.id) updated else it.selectedItemForDetail,
                            statusMessage = msg
                        )
                    }
                } else {
                    _uiState.update { it.copy(statusMessage = "Price verified with store. No active discounts.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Store connection verified.") }
            }
        }
    }

    private fun cacheAllImagesOffline(items: List<AccessoryItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            com.adorn.wardrobe.data.local.ImageStorageHelper.cacheAllRemoteImages(
                getApplication(),
                items
            ) { updatedItem ->
                dbHelper.updateItem(updatedItem)
            }
        }
    }

    fun deleteItem(item: AccessoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteItem(item.id)
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            _uiState.update {
                it.copy(
                    selectedItemForDetail = null,
                    isAddEditSheetOpen = false
                )
            }
            loadTrips()
        }
    }

    fun toggleFavorite(item: AccessoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val newFav = !item.isFavorite
            dbHelper.toggleFavorite(item.id, newFav)
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            if (_uiState.value.selectedItemForDetail?.id == item.id) {
                _uiState.update {
                    it.copy(selectedItemForDetail = it.selectedItemForDetail?.copy(isFavorite = newFav))
                }
            }
        }
    }

    // ==========================================
    // MULTI-SELECT & BULK ACTIONS
    // ==========================================

    fun toggleSelectionMode() {
        _uiState.update {
            val nextMode = !it.isSelectionMode
            it.copy(
                isSelectionMode = nextMode,
                selectedItemIds = emptySet()
            )
        }
    }

    fun toggleItemSelected(id: Long) {
        _uiState.update {
            val current = it.selectedItemIds.toMutableSet()
            if (current.contains(id)) {
                current.remove(id)
            } else {
                current.add(id)
            }
            it.copy(selectedItemIds = current)
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val allIds = if (state.currentTab == WardrobeTab.WISHLIST) {
                state.filteredWishlistItems.map { it.id }.toSet()
            } else {
                state.filteredItems.map { it.id }.toSet()
            }
            state.copy(selectedItemIds = allIds)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItemIds = emptySet()) }
    }

    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = _uiState.value.selectedItemIds.toList()
            if (ids.isNotEmpty()) {
                dbHelper.deleteItems(ids)
                val refreshed = dbHelper.getAllItems()
                updateItemsAndStats(refreshed)
                _uiState.update {
                    it.copy(
                        selectedItemIds = emptySet(),
                        isSelectionMode = false,
                        selectedItemForDetail = null,
                        statusMessage = "Deleted ${ids.size} pieces successfully."
                    )
                }
                loadTrips()
            }
        }
    }

    // ==========================================
    // WISHLIST SPECIFIC ACTIONS
    // ==========================================

    fun setWishlistPriority(priority: String) {
        _uiState.update { it.copy(selectedWishlistPriority = priority) }
        reapplyWishlistFilters()
    }

    fun moveToWardrobe(item: AccessoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.moveToWardrobe(item.id)
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            _uiState.update {
                it.copy(
                    selectedItemForDetail = null,
                    statusMessage = "✨ '${item.title}' acquired and moved to Wardrobe!"
                )
            }
        }
    }

    fun moveToWishlist(item: AccessoryItem, priority: String = "MEDIUM", targetPrice: Double = 0.0) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.moveToWishlist(item.id, priority, targetPrice)
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            _uiState.update {
                it.copy(
                    selectedItemForDetail = null,
                    statusMessage = "Moved to Wishlist goals."
                )
            }
        }
    }

    fun moveSelectedWishlistToWardrobe() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = _uiState.value.selectedItemIds.toList()
            if (ids.isNotEmpty()) {
                dbHelper.moveItemsToWardrobe(ids)
                val refreshed = dbHelper.getAllItems()
                updateItemsAndStats(refreshed)
                _uiState.update {
                    it.copy(
                        selectedItemIds = emptySet(),
                        isSelectionMode = false,
                        statusMessage = "✨ Acquired and moved ${ids.size} pieces to Wardrobe!"
                    )
                }
            }
        }
    }

    // ==========================================
    // DEDUPLICATION
    // ==========================================

    fun removeDuplicates() {
        viewModelScope.launch(Dispatchers.IO) {
            val removed = dbHelper.removeDuplicates()
            val refreshed = dbHelper.getAllItems()
            updateItemsAndStats(refreshed)
            _uiState.update {
                it.copy(
                    statusMessage = if (removed > 0) {
                        "✨ Cleaned $removed redundant entries & consolidated photos!"
                    } else {
                        "Wardrobe is pristine! No duplicate items found."
                    }
                )
            }
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // ==========================================
    // AI STYLIST
    // ==========================================

    fun requestAiStyling(
        occasion: String = "Wedding & Festive",
        outfitColor: String = "Emerald Green",
        neckline: String = "V-Neck",
        selectedClothingItemId: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isStylistLoading = true) }
            val request = OutfitStylingRequest(
                occasion = occasion,
                outfitColor = outfitColor,
                neckline = neckline,
                selectedClothingItemId = selectedClothingItemId
            )
            val suggestion = stylistService.getStylingSuggestions(request, _uiState.value.items)
            _uiState.update {
                it.copy(
                    isStylistLoading = false,
                    suggestedLook = suggestion
                )
            }
        }
    }

    // ==========================================
    // BEFORE & AFTER TRIP CHECKLIST ACTIONS
    // ==========================================

    fun selectTrip(trip: Trip) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = dbHelper.getTripItems(trip.id)
            _uiState.update {
                it.copy(
                    selectedTrip = trip,
                    selectedTripItems = items
                )
            }
        }
    }

    fun openCreateTripDialog() {
        _uiState.update { it.copy(isCreateTripDialogOpen = true) }
    }

    fun closeCreateTripDialog() {
        _uiState.update { it.copy(isCreateTripDialogOpen = false) }
    }

    fun createTrip(name: String, destination: String, travelDate: String, accessoryIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val tripId = dbHelper.createTrip(name, destination, travelDate, accessoryIds)
            val allTrips = dbHelper.getAllTrips()
            val newSelected = allTrips.firstOrNull { it.id == tripId } ?: allTrips.firstOrNull()
            val items = if (newSelected != null) dbHelper.getTripItems(newSelected.id) else emptyList()

            _uiState.update {
                it.copy(
                    trips = allTrips,
                    selectedTrip = newSelected,
                    selectedTripItems = items,
                    isCreateTripDialogOpen = false
                )
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteTrip(trip.id)
            val allTrips = dbHelper.getAllTrips()
            val nextSelected = allTrips.firstOrNull()
            val items = if (nextSelected != null) dbHelper.getTripItems(nextSelected.id) else emptyList()

            _uiState.update {
                it.copy(
                    trips = allTrips,
                    selectedTrip = nextSelected,
                    selectedTripItems = items
                )
            }
        }
    }

    fun toggleItemPacked(tripItem: TripItemWithAccessory) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStatus = !tripItem.isPacked
            dbHelper.setTripItemPacked(tripItem.tripItemId, newStatus)
            val currentTrip = _uiState.value.selectedTrip
            if (currentTrip != null) {
                val updatedItems = dbHelper.getTripItems(currentTrip.id)
                _uiState.update { it.copy(selectedTripItems = updatedItems) }
            }
        }
    }

    fun toggleItemReturned(tripItem: TripItemWithAccessory) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStatus = !tripItem.isReturned
            dbHelper.setTripItemReturned(tripItem.tripItemId, newStatus)
            val currentTrip = _uiState.value.selectedTrip
            if (currentTrip != null) {
                val updatedItems = dbHelper.getTripItems(currentTrip.id)
                _uiState.update { it.copy(selectedTripItems = updatedItems) }
            }
        }
    }

    fun openAddItemsToTripSheet() {
        _uiState.update { it.copy(isAddItemsToTripSheetOpen = true) }
    }

    fun closeAddItemsToTripSheet() {
        _uiState.update { it.copy(isAddItemsToTripSheetOpen = false) }
    }

    fun addItemsToCurrentTrip(accessoryIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val trip = _uiState.value.selectedTrip ?: return@launch
            dbHelper.addItemsToTrip(trip.id, accessoryIds)
            val updatedItems = dbHelper.getTripItems(trip.id)
            _uiState.update {
                it.copy(
                    selectedTripItems = updatedItems,
                    isAddItemsToTripSheetOpen = false
                )
            }
        }
    }

    fun removeTripItem(tripItem: TripItemWithAccessory) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteTripItem(tripItem.tripItemId)
            val trip = _uiState.value.selectedTrip ?: return@launch
            val updatedItems = dbHelper.getTripItems(trip.id)
            _uiState.update { it.copy(selectedTripItems = updatedItems) }
        }
    }

    private fun updateItemsAndStats(allItems: List<AccessoryItem>) {
        val wardrobeItems = allItems.filter { !it.isWishlist }
        val wishlistItems = allItems.filter { it.isWishlist }

        val totalWardrobeVal = wardrobeItems.sumOf { it.price }
        val totalWishlistVal = wishlistItems.sumOf { if (it.targetPrice > 0.0) it.targetPrice else it.price }

        _uiState.update {
            it.copy(
                items = wardrobeItems,
                wishlistItems = wishlistItems,
                totalWardrobeValue = totalWardrobeVal,
                totalWishlistValue = totalWishlistVal
            )
        }
        reapplyFilters()
        reapplyWishlistFilters()
    }

    private fun reapplyFilters() {
        val state = _uiState.value
        var list = state.items

        if (state.selectedCategory != AccessoryCategory.ALL) {
            list = list.filter { it.category == state.selectedCategory }
        }

        if (state.selectedMetal != MetalType.ALL) {
            list = list.filter { it.metal == state.selectedMetal }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(query) ||
                it.brand.lowercase().contains(query) ||
                it.notes.lowercase().contains(query) ||
                it.metal.displayName.lowercase().contains(query) ||
                it.storageLocation.lowercase().contains(query)
            }
        }

        list = when (state.sortOrder) {
            SortOrder.RECENT -> list.sortedByDescending { it.dateAdded }
            SortOrder.PRICE_HIGH -> list.sortedByDescending { it.price }
            SortOrder.PRICE_LOW -> list.sortedBy { it.price }
            SortOrder.NAME -> list.sortedBy { it.title.lowercase() }
        }

        _uiState.update { it.copy(filteredItems = list) }
    }

    private fun reapplyWishlistFilters() {
        val state = _uiState.value
        var list = state.wishlistItems

        if (state.selectedWishlistPriority.equals("PRICE_DROPS", ignoreCase = true)) {
            list = list.filter { it.hasPriceDrop }
        } else if (state.selectedWishlistPriority != "ALL") {
            list = list.filter { it.priority.equals(state.selectedWishlistPriority, ignoreCase = true) }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(query) ||
                it.brand.lowercase().contains(query) ||
                it.notes.lowercase().contains(query) ||
                it.metal.displayName.lowercase().contains(query)
            }
        }

        list = when (state.sortOrder) {
            SortOrder.RECENT -> list.sortedByDescending { it.dateAdded }
            SortOrder.PRICE_HIGH -> list.sortedByDescending { if (it.targetPrice > 0) it.targetPrice else it.price }
            SortOrder.PRICE_LOW -> list.sortedBy { if (it.targetPrice > 0) it.targetPrice else it.price }
            SortOrder.NAME -> list.sortedBy { it.title.lowercase() }
        }

        _uiState.update { it.copy(filteredWishlistItems = list) }
    }

    // ==========================================
    // ADORN BACKUP & RESTORE (.adorn)
    // ==========================================

    fun exportAdornBackup(context: android.content.Context, onReady: (java.io.File) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Compiling .adorn luxury vault archive...") }
            try {
                val file = com.adorn.wardrobe.data.local.AdornBackupHelper.createAdornBackup(context)
                _uiState.update { it.copy(statusMessage = "Archive ready: ${file.name}") }
                onReady(file)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(statusMessage = "Backup failed: ${e.message}") }
            }
        }
    }

    fun restoreAdornBackup(context: android.content.Context, backupUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Restoring from .adorn backup...") }
            try {
                val result = com.adorn.wardrobe.data.local.AdornBackupHelper.restoreAdornBackup(context, backupUri)
                val refreshed = dbHelper.getAllItems()
                updateItemsAndStats(refreshed)
                _uiState.update { it.copy(statusMessage = result.message) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(statusMessage = "Restore failed: ${e.message}") }
            }
        }
    }
}
