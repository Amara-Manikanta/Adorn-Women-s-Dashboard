package com.adorn.wardrobe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.ui.components.AccessoryCard
import com.adorn.wardrobe.ui.components.WardrobeHeader
import com.adorn.wardrobe.ui.theme.ChampagneGold
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import com.adorn.wardrobe.ui.viewmodel.SortOrder
import com.adorn.wardrobe.ui.viewmodel.WardrobeUiState

@Composable
fun WishlistScreen(
    uiState: WardrobeUiState,
    onSearchChange: (String) -> Unit,
    onPrioritySelected: (String) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onItemClick: (AccessoryItem) -> Unit,
    onFavoriteToggle: (AccessoryItem) -> Unit,
    onAddClick: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectToggle: (AccessoryItem) -> Unit,
    onDeleteSelected: () -> Unit,
    onMoveSelectedToWardrobe: () -> Unit,
    onRemoveDuplicates: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header: Title, Search, Sort & Multi-Select
            item(span = { GridItemSpan(2) }) {
                WardrobeHeader(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = onSearchChange,
                    totalValue = uiState.totalWishlistValue,
                    itemCount = uiState.wishlistItems.size,
                    currentSort = uiState.sortOrder,
                    onSortChange = onSortChange,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedCount = uiState.selectedItemIds.size,
                    onToggleSelectionMode = onToggleSelectionMode,
                    onSelectAll = onSelectAll,
                    onClearSelection = onClearSelection,
                    onRemoveDuplicates = onRemoveDuplicates,
                    title = "Wishlist",
                    subtitle = "Target Acquisitions & High-End Upgrades"
                )
            }

            // Price Drop Notification Banner
            item(span = { GridItemSpan(2) }) {
                val priceDropCount = uiState.wishlistItems.count { it.hasPriceDrop }
                val totalSavings = uiState.wishlistItems.filter { it.hasPriceDrop }.sumOf { it.priceDropAmount }
                if (priceDropCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF0F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { onPrioritySelected("PRICE_DROPS") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$priceDropCount Item${if (priceDropCount > 1) "s" else ""} on Price Drop!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "Save ₹${String.format("%,.0f", totalSavings)} on wishlist targets. Tap to filter deals.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B454E)
                                )
                            }
                            Text(
                                text = "View Deals →",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // Priority Filter Chips
            item(span = { GridItemSpan(2) }) {
                WishlistPriorityFilterBar(
                    selectedPriority = uiState.selectedWishlistPriority,
                    onPrioritySelected = onPrioritySelected,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Wishlist Grid Items or Empty State
            if (uiState.filteredWishlistItems.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyWishlistState(onAddClick = onAddClick)
                }
            } else {
                items(uiState.filteredWishlistItems, key = { it.id }) { item ->
                    AccessoryCard(
                        item = item,
                        onItemClick = onItemClick,
                        onFavoriteToggle = onFavoriteToggle,
                        isSelectionMode = uiState.isSelectionMode,
                        isSelected = uiState.selectedItemIds.contains(item.id),
                        onSelectToggle = onSelectToggle
                    )
                }
            }
        }

        // Floating Action Button to Add Wishlist Piece
        if (!uiState.isSelectionMode) {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = RoseGoldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Wishlist Piece"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Wishlist Piece",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Floating Batch Action Bar for Wishlist (Promote or Delete)
        AnimatedVisibility(
            visible = uiState.isSelectionMode && uiState.selectedItemIds.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2B1D25),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${uiState.selectedItemIds.size} Selected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Wishlist Actions",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD4BCC4)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onMoveSelectedToWardrobe,
                            colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Acquired! Move",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Confirmation Alert Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        text = "Delete ${uiState.selectedItemIds.size} Wishlist Items?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF332028)
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove these ${uiState.selectedItemIds.size} pieces from your wishlist?",
                        color = Color(0xFF6B5560)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteSelected()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = Color(0xFF7E6672))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun WishlistPriorityFilterBar(
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val priorities = listOf(
        Pair("ALL", "All Wishlist"),
        Pair("PRICE_DROPS", "🔥 Price Drops"),
        Pair("HIGH", "🔥 High Priority"),
        Pair("MEDIUM", "⭐ Target"),
        Pair("LOW", "Aspirational")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        priorities.forEach { (prioCode, label) ->
            val isSelected = selectedPriority.equals(prioCode, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) RoseGoldPrimary else Color.White,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0DFE2)),
                shadowElevation = if (isSelected) 3.dp else 1.dp,
                modifier = Modifier.clickable { onPrioritySelected(prioCode) }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.White else Color(0xFF5A444E),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyWishlistState(onAddClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFFBF1F3), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = RoseGoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Wishlist is Clear",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF332028)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Track aspirational fine jewelry, couture lehengas, watches, or luxury bags you're planning to acquire.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF7E6672)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Target Piece")
            }
        }
    }
}
