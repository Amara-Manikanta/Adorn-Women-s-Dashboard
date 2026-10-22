package com.adorn.wardrobe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.Trip
import com.adorn.wardrobe.data.model.TripItemWithAccessory
import com.adorn.wardrobe.ui.components.MetalBadge
import com.adorn.wardrobe.ui.theme.ChampagneGold
import com.adorn.wardrobe.ui.theme.RoseGoldLight
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import com.adorn.wardrobe.ui.viewmodel.WardrobeUiState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripChecklistScreen(
    uiState: WardrobeUiState,
    onSelectTrip: (Trip) -> Unit,
    onOpenCreateTrip: () -> Unit,
    onCloseCreateTrip: () -> Unit,
    onCreateTrip: (name: String, dest: String, date: String, itemIds: List<Long>) -> Unit,
    onDeleteTrip: (Trip) -> Unit,
    onTogglePacked: (TripItemWithAccessory) -> Unit,
    onToggleReturned: (TripItemWithAccessory) -> Unit,
    onOpenAddItems: () -> Unit,
    onCloseAddItems: () -> Unit,
    onAddItemsToTrip: (List<Long>) -> Unit,
    onRemoveTripItem: (TripItemWithAccessory) -> Unit,
    onItemClick: (AccessoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabPhase by remember { mutableIntStateOf(0) } // 0 = Before Trip (Packing), 1 = After Trip (Safe Return)
    val addItemsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F8))
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trip Checklist",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF381F2A)
                    )
                    Text(
                        text = "Before Packing & After-Trip Safe Return",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7E646F)
                    )
                }

                Button(
                    onClick = onOpenCreateTrip,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Trip", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trip Horizontal Scroll Selector
            if (uiState.trips.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.trips.forEach { trip ->
                        val isSelected = trip.id == uiState.selectedTrip?.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) RoseGoldPrimary else Color.White,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1DDE0)) else null,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.clickable { onSelectTrip(trip) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Luggage,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else RoseGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = trip.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else Color(0xFF4C3642)
                                )
                            }
                        }
                    }
                }
            }
        }

        val currentTrip = uiState.selectedTrip
        if (currentTrip == null) {
            // Empty Trips State
            EmptyTripsView(onOpenCreateTrip = onOpenCreateTrip)
        } else {
            val items = uiState.selectedTripItems
            val packedCount = items.count { it.isPacked }
            val returnedCount = items.count { it.isReturned }
            val totalCount = items.size

            // Trip Info & Progress Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrip.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF2C1923)
                            )
                            if (currentTrip.destination.isNotBlank() || currentTrip.travelDate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = RoseGoldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = listOfNotNull(
                                            currentTrip.destination.takeIf { it.isNotBlank() },
                                            currentTrip.travelDate.takeIf { it.isNotBlank() }
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF7E6672)
                                    )
                                }
                            }
                        }

                        // Delete Trip Button
                        IconButton(
                            onClick = { onDeleteTrip(currentTrip) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Trip",
                                tint = Color(0xFFA08892),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress indicators
                    val currentProgress = if (selectedTabPhase == 0) {
                        if (totalCount > 0) packedCount.toFloat() / totalCount else 0f
                    } else {
                        if (totalCount > 0) returnedCount.toFloat() / totalCount else 0f
                    }

                    val progressText = if (selectedTabPhase == 0) {
                        "$packedCount of $totalCount pieces packed for travel"
                    } else {
                        "$returnedCount of $totalCount pieces safely returned home"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (selectedTabPhase == 0) "Packing Progress" else "Safe Return Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7E6672)
                        )
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTabPhase == 0) RoseGoldPrimary else Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (selectedTabPhase == 0) RoseGoldPrimary else Color(0xFF2E7D32),
                        trackColor = Color(0xFFF7ECF0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two-Phase Tab Switcher
            TabRow(
                selectedTabIndex = selectedTabPhase,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabPhase]),
                        color = if (selectedTabPhase == 0) RoseGoldPrimary else Color(0xFF2E7D32)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTabPhase == 0,
                    onClick = { selectedTabPhase = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Luggage,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Before Trip (Packing)",
                                fontWeight = if (selectedTabPhase == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    selectedContentColor = RoseGoldPrimary,
                    unselectedContentColor = Color(0xFF8A727C)
                )

                Tab(
                    selected = selectedTabPhase == 1,
                    onClick = { selectedTabPhase = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. After Trip (Safe Return)",
                                fontWeight = if (selectedTabPhase == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    selectedContentColor = Color(0xFF2E7D32),
                    unselectedContentColor = Color(0xFF8A727C)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Safe Return Warning / Celebration Banner (In Phase 2)
            if (selectedTabPhase == 1 && totalCount > 0) {
                if (returnedCount == totalCount) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "All Pieces Safely Back Home! ✨",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Nothing was left behind in hotel safes or bags. Everything is returned to its rightful drawer.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF3E0),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${totalCount - returnedCount} Piece(s) Still Unpacked!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFBF360C)
                                )
                                Text(
                                    text = "Double check hotel room safe, bathroom tray & travel pouches before checking out.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }

            // Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTabPhase == 0) "Pieces Packed for Trip ($totalCount)" else "Pieces to Return Home ($totalCount)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3B1E29)
                        )

                        Text(
                            text = "+ Add Pieces",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoseGoldPrimary,
                            modifier = Modifier
                                .clickable { onOpenAddItems() }
                                .padding(4.dp)
                        )
                    }
                }

                if (items.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Items in this Trip",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF332028)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Select earrings, chains, or handbags from your wardrobe to pack for this trip.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF7E6672)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onOpenAddItems,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Accessories & Bags")
                                }
                            }
                        }
                    }
                } else {
                    items(items, key = { it.tripItemId }) { tripItem ->
                        TripItemCheckCard(
                            tripItem = tripItem,
                            isPackingPhase = selectedTabPhase == 0,
                            onTogglePacked = { onTogglePacked(tripItem) },
                            onToggleReturned = { onToggleReturned(tripItem) },
                            onRemove = { onRemoveTripItem(tripItem) },
                            onItemClick = { onItemClick(tripItem.accessory) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Modal: Add Items to Selected Trip
    if (uiState.isAddItemsToTripSheetOpen) {
        val selectedTrip = uiState.selectedTrip
        val existingItemIds = uiState.selectedTripItems.map { it.accessory.id }.toSet()
        val availableWardrobeItems = uiState.items.filter { it.id !in existingItemIds }
        val checkedItemIds = remember { mutableStateListOf<Long>() }

        ModalBottomSheet(
            onDismissRequest = onCloseAddItems,
            sheetState = addItemsSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add to Trip Checklist",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF332028)
                        )
                        Text(
                            text = "Select items to take to ${selectedTrip?.name ?: "trip"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7E6672)
                        )
                    }

                    IconButton(onClick = onCloseAddItems) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (availableWardrobeItems.isEmpty()) {
                    Text(
                        text = "All items in your wardrobe are already added to this trip!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7E6672),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableWardrobeItems) { acc ->
                            val isChecked = acc.id in checkedItemIds
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isChecked) Color(0xFFFBF1F3) else Color(0xFFF9F5F6),
                                border = if (isChecked) androidx.compose.foundation.BorderStroke(1.dp, RoseGoldPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) checkedItemIds.remove(acc.id) else checkedItemIds.add(acc.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            if (it) checkedItemIds.add(acc.id) else checkedItemIds.remove(acc.id)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = RoseGoldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = acc.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF311F28)
                                        )
                                        Text(
                                            text = "${acc.category.displayName} • ${acc.metal.shortTag}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF866E7A)
                                        )
                                    }
                                    Text(
                                        text = acc.formattedPrice,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RoseGoldPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onAddItemsToTrip(checkedItemIds.toList()) },
                        enabled = checkedItemIds.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Add ${checkedItemIds.size} Items to Trip",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Dialog: Create New Trip
    if (uiState.isCreateTripDialogOpen) {
        CreateTripModal(
            wardrobeItems = uiState.items,
            onDismiss = onCloseCreateTrip,
            onCreate = onCreateTrip
        )
    }
}

@Composable
fun TripItemCheckCard(
    tripItem: TripItemWithAccessory,
    isPackingPhase: Boolean,
    onTogglePacked: () -> Unit,
    onToggleReturned: () -> Unit,
    onRemove: () -> Unit,
    onItemClick: () -> Unit
) {
    val acc = tripItem.accessory
    val isChecked = if (isPackingPhase) tripItem.isPacked else tripItem.isReturned

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) Color(0xFFFDF7F8) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 1.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = { if (isPackingPhase) onTogglePacked() else onToggleReturned() },
                colors = CheckboxDefaults.colors(
                    checkedColor = if (isPackingPhase) RoseGoldPrimary else Color(0xFF2E7D32)
                )
            )

            // Accessory Thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3ECEE))
            ) {
                val imageModel = if (acc.imageUri.startsWith("/")) {
                    File(acc.imageUri)
                } else if (acc.imageUri.isNotBlank()) {
                    acc.imageUri
                } else null

                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = acc.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = acc.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isChecked) Color(0xFF8D7883) else Color(0xFF2C1923),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = acc.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA17C88)
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA17C88)
                    )
                    Text(
                        text = acc.metal.shortTag,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF6E5661)
                    )
                }

                // In Safe Return Phase: Show home storage location reminder!
                if (!isPackingPhase) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = if (tripItem.isReturned) Color(0xFF2E7D32) else RoseGoldPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (acc.storageLocation.isNotBlank()) "Return to: ${acc.storageLocation}" else "Return to: Jewelry Box / Closet",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (tripItem.isReturned) Color(0xFF2E7D32) else Color(0xFF8B4D5D),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick Status Chip / Remove
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove from Trip",
                    tint = Color(0xFFB4A1A9),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripModal(
    wardrobeItems: List<AccessoryItem>,
    onDismiss: () -> Unit,
    onCreate: (name: String, dest: String, date: String, itemIds: List<Long>) -> Unit
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tripName by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var travelDate by remember { mutableStateOf("") }
    val selectedItemIds = remember { mutableStateListOf<Long>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan New Trip / Event",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF332028)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Trip / Event Name *",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4C3642)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                placeholder = { Text("e.g., Udaipur Destination Wedding, Goa Beach Trip") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Destination",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4C3642)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        placeholder = { Text("e.g., Jaipur, Rajasthan") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Travel Dates",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4C3642)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = travelDate,
                        onValueChange = { travelDate = it },
                        placeholder = { Text("e.g., Nov 14 - 18") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Pieces to Pack (${selectedItemIds.size} selected)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4C3642)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(wardrobeItems) { acc ->
                    val isChecked = acc.id in selectedItemIds
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isChecked) Color(0xFFFBF1F3) else Color(0xFFFAF6F7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedItemIds.remove(acc.id) else selectedItemIds.add(acc.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedItemIds.add(acc.id) else selectedItemIds.remove(acc.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = RoseGoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acc.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF2F1D27),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${acc.category.displayName} • ${acc.metal.shortTag}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF876F7C)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (tripName.isNotBlank()) {
                        onCreate(tripName, destination, travelDate, selectedItemIds.toList())
                    }
                },
                enabled = tripName.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Create Trip Checklist 🧳",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyTripsView(onOpenCreateTrip: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(Color(0xFFFBF1F3), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = RoseGoldPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Trips Planned Yet",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF332028)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Going to a wedding, family function, or holiday? Create a trip checklist to track which jewelry & bags you pack before leaving and ensure every single piece returns home safely!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color(0xFF7E6672)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onOpenCreateTrip,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Plan Your First Trip")
            }
        }
    }
}

@Composable
private fun defaultTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFFAF4F5),
    unfocusedContainerColor = Color(0xFFFAF4F5),
    focusedIndicatorColor = RoseGoldPrimary,
    unfocusedIndicatorColor = Color(0xFFEEDADB)
)
