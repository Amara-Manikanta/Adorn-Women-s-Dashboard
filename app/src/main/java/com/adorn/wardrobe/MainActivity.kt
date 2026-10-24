package com.adorn.wardrobe

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adorn.wardrobe.ui.components.DetailBottomSheet
import com.adorn.wardrobe.ui.screens.AddEditItemDialog
import com.adorn.wardrobe.ui.screens.AiStylistScreen
import com.adorn.wardrobe.ui.screens.GalleryScreen
import com.adorn.wardrobe.ui.screens.TripChecklistScreen
import com.adorn.wardrobe.ui.screens.WishlistScreen
import com.adorn.wardrobe.ui.theme.AdornWardrobeTheme
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import com.adorn.wardrobe.ui.viewmodel.WardrobeTab
import com.adorn.wardrobe.ui.viewmodel.WardrobeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WardrobeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AdornWardrobeTheme {
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val addEditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // Document Picker Launcher for restoring .adorn backups
                val openDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        viewModel.restoreAdornBackup(context, uri)
                    }
                }

                // Show status toast notification
                LaunchedEffect(uiState.statusMessage) {
                    uiState.statusMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.dismissStatusMessage()
                    }
                }

                Scaffold(
                    bottomBar = {
                        LuxuryBottomNav(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    },
                    containerColor = Color(0xFFFFF9F8)
                ) { innerPadding ->
                    when (uiState.currentTab) {
                        WardrobeTab.GALLERY -> {
                            GalleryScreen(
                                uiState = uiState,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onCategorySelected = { viewModel.setCategory(it) },
                                onMetalSelected = { viewModel.setMetal(it) },
                                onSortChange = { viewModel.setSortOrder(it) },
                                onItemClick = { viewModel.selectItemForDetail(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onAddClick = { viewModel.openAddDialog() },
                                onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                                onSelectAll = { viewModel.selectAll() },
                                onClearSelection = { viewModel.clearSelection() },
                                onSelectToggle = { viewModel.toggleItemSelected(it.id) },
                                onDeleteSelected = { viewModel.deleteSelected() },
                                onRemoveDuplicates = { viewModel.removeDuplicates() },
                                onExportBackup = {
                                    viewModel.exportAdornBackup(context) { file ->
                                        com.adorn.wardrobe.data.local.AdornBackupHelper.shareBackupFile(context, file)
                                    }
                                },
                                onRestoreBackup = {
                                    openDocumentLauncher.launch(arrayOf("*/*"))
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        WardrobeTab.WISHLIST -> {
                            WishlistScreen(
                                uiState = uiState,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onPrioritySelected = { viewModel.setWishlistPriority(it) },
                                onSortChange = { viewModel.setSortOrder(it) },
                                onItemClick = { viewModel.selectItemForDetail(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onAddClick = { viewModel.openAddDialog() },
                                onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                                onSelectAll = { viewModel.selectAll() },
                                onClearSelection = { viewModel.clearSelection() },
                                onSelectToggle = { viewModel.toggleItemSelected(it.id) },
                                onDeleteSelected = { viewModel.deleteSelected() },
                                onMoveSelectedToWardrobe = { viewModel.moveSelectedWishlistToWardrobe() },
                                onRemoveDuplicates = { viewModel.removeDuplicates() },
                                onExportBackup = {
                                    viewModel.exportAdornBackup(context) { file ->
                                        com.adorn.wardrobe.data.local.AdornBackupHelper.shareBackupFile(context, file)
                                    }
                                },
                                onRestoreBackup = {
                                    openDocumentLauncher.launch(arrayOf("*/*"))
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        WardrobeTab.AI_STYLIST -> {
                            AiStylistScreen(
                                uiState = uiState,
                                onRequestStyling = { occ, col, neck, clothingId ->
                                    viewModel.requestAiStyling(occ, col, neck, clothingId)
                                },
                                onItemClick = { viewModel.selectItemForDetail(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        WardrobeTab.TRIP_CHECKLIST -> {
                            TripChecklistScreen(
                                uiState = uiState,
                                onSelectTrip = { viewModel.selectTrip(it) },
                                onOpenCreateTrip = { viewModel.openCreateTripDialog() },
                                onCloseCreateTrip = { viewModel.closeCreateTripDialog() },
                                onCreateTrip = { name, dest, date, itemIds ->
                                    viewModel.createTrip(name, dest, date, itemIds)
                                },
                                onDeleteTrip = { viewModel.deleteTrip(it) },
                                onTogglePacked = { viewModel.toggleItemPacked(it) },
                                onToggleReturned = { viewModel.toggleItemReturned(it) },
                                onOpenAddItems = { viewModel.openAddItemsToTripSheet() },
                                onCloseAddItems = { viewModel.closeAddItemsToTripSheet() },
                                onAddItemsToTrip = { viewModel.addItemsToCurrentTrip(it) },
                                onRemoveTripItem = { viewModel.removeTripItem(it) },
                                onItemClick = { viewModel.selectItemForDetail(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }

                    // Detail Bottom Sheet
                    if (uiState.selectedItemForDetail != null) {
                        DetailBottomSheet(
                            item = uiState.selectedItemForDetail!!,
                            sheetState = detailSheetState,
                            onDismiss = { viewModel.selectItemForDetail(null) },
                            onEditClick = {
                                viewModel.openEditDialog(it)
                            },
                            onDeleteClick = {
                                viewModel.deleteItem(it)
                            },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(it)
                            },
                            onMoveToWardrobe = {
                                viewModel.moveToWardrobe(it)
                            },
                            onMoveToWishlist = {
                                viewModel.moveToWishlist(it)
                            },
                            onIncrementTimesWorn = { item, delta ->
                                viewModel.incrementTimesWorn(item, delta)
                            },
                            onSimulatePriceDrop = { item ->
                                viewModel.simulatePriceDrop(item)
                            },
                            onCheckLivePrice = { item ->
                                viewModel.refreshLiveStorePrice(item)
                            }
                        )
                    }

                    // Add / Edit Dialog Sheet
                    if (uiState.isAddEditSheetOpen) {
                        AddEditItemDialog(
                            itemBeingEdited = uiState.itemBeingEdited,
                            sheetState = addEditSheetState,
                            onDismiss = { viewModel.closeAddEditDialog() },
                            onSave = { title, cat, metal, purity, price, weight, dimensions, isSet, setIncludes, occ, loc, brand, notes, imgUris, storeUrl, origPrice, timesWorn, cutoutUri, domColor, accColor, colName ->
                                viewModel.saveItem(
                                    title, cat, metal, purity, price, weight, dimensions, isSet, setIncludes, occ, loc, brand, notes, imgUris,
                                    storeUrl = storeUrl,
                                    originalPrice = origPrice,
                                    timesWorn = timesWorn,
                                    cutoutUri = cutoutUri,
                                    dominantColorHex = domColor,
                                    accentColorHex = accColor,
                                    colorName = colName
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LuxuryBottomNav(
    currentTab: WardrobeTab,
    onTabSelected: (WardrobeTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentTab == WardrobeTab.GALLERY,
            onClick = { onTabSelected(WardrobeTab.GALLERY) },
            icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = "Wardrobe") },
            label = { Text("Wardrobe", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoseGoldPrimary,
                selectedTextColor = RoseGoldPrimary,
                indicatorColor = Color(0xFFFBECEF),
                unselectedIconColor = Color(0xFF8A727C),
                unselectedTextColor = Color(0xFF8A727C)
            )
        )

        NavigationBarItem(
            selected = currentTab == WardrobeTab.WISHLIST,
            onClick = { onTabSelected(WardrobeTab.WISHLIST) },
            icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Wishlist") },
            label = { Text("Wishlist", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoseGoldPrimary,
                selectedTextColor = RoseGoldPrimary,
                indicatorColor = Color(0xFFFBECEF),
                unselectedIconColor = Color(0xFF8A727C),
                unselectedTextColor = Color(0xFF8A727C)
            )
        )

        NavigationBarItem(
            selected = currentTab == WardrobeTab.AI_STYLIST,
            onClick = { onTabSelected(WardrobeTab.AI_STYLIST) },
            icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Stylist") },
            label = { Text("Stylist", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoseGoldPrimary,
                selectedTextColor = RoseGoldPrimary,
                indicatorColor = Color(0xFFFBECEF),
                unselectedIconColor = Color(0xFF8A727C),
                unselectedTextColor = Color(0xFF8A727C)
            )
        )

        NavigationBarItem(
            selected = currentTab == WardrobeTab.TRIP_CHECKLIST,
            onClick = { onTabSelected(WardrobeTab.TRIP_CHECKLIST) },
            icon = { Icon(imageVector = Icons.Default.Luggage, contentDescription = "Travel") },
            label = { Text("Travel", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoseGoldPrimary,
                selectedTextColor = RoseGoldPrimary,
                indicatorColor = Color(0xFFFBECEF),
                unselectedIconColor = Color(0xFF8A727C),
                unselectedTextColor = Color(0xFF8A727C)
            )
        )
    }
}
