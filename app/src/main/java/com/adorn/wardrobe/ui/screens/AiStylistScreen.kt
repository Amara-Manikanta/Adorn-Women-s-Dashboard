package com.adorn.wardrobe.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.ui.components.AccessoryCard
import com.adorn.wardrobe.ui.components.LuxuryChip
import com.adorn.wardrobe.ui.theme.ChampagneGold
import com.adorn.wardrobe.ui.theme.RoseGoldDark
import com.adorn.wardrobe.ui.theme.RoseGoldLight
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import com.adorn.wardrobe.ui.viewmodel.WardrobeUiState
import java.io.File

@Composable
fun AiStylistScreen(
    uiState: WardrobeUiState,
    onRequestStyling: (occasion: String, outfitColor: String, neckline: String, selectedClothingItemId: Long?) -> Unit,
    onItemClick: (AccessoryItem) -> Unit,
    onFavoriteToggle: (AccessoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val occasions = listOf("Wedding & Festive", "Party & Evening", "Work & Office", "Casual & Outings", "Date Night")
    val outfitColors = listOf(
        Pair("Emerald Green", Color(0xFF0F5132)),
        Pair("Midnight Black", Color(0xFF1A1A1A)),
        Pair("Ruby Red", Color(0xFF842029)),
        Pair("Ivory & Gold", Color(0xFFD4AF37)),
        Pair("Royal Navy", Color(0xFF0D6EFD)),
        Pair("Pastel Rose", Color(0xFFD63384))
    )

    var selectedClothingId by remember { mutableStateOf<Long?>(null) }
    var selectedOccasion by remember { mutableStateOf(occasions.first()) }
    var selectedColor by remember { mutableStateOf(outfitColors.first().first) }
    var showApiDetails by remember { mutableStateOf(false) }

    val clothingItems = remember(uiState.items) {
        uiState.items.filter { it.category.isClothing }
    }
    val selectedClothingItem = remember(selectedClothingId, uiState.items) {
        uiState.items.firstOrNull { it.id == selectedClothingId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Header Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8B3A4F), Color(0xFFC47B89), Color(0xFFE8B4B8))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Stylist Studio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Coordinate sarees, gowns, kurtis, earrings, necklaces, handbags & watches from your private wardrobe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Pick an outfit from your wardrobe (Optional)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "1. Select Outfit from Wardrobe",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF381F2A)
            )

            if (selectedClothingId != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFBECEF),
                    modifier = Modifier.clickable { selectedClothingId = null }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = RoseGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear selection",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoseGoldPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Freeform / No outfit selection card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedClothingId == null) Color(0xFFFDF0F3) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (selectedClothingId == null) 2.dp else 1.dp,
                    color = if (selectedClothingId == null) RoseGoldPrimary else Color(0xFFEFE6EA)
                ),
                modifier = Modifier
                    .width(130.dp)
                    .clickable { selectedClothingId = null }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (selectedClothingId == null) RoseGoldPrimary else Color(0xFFF7ECEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = if (selectedClothingId == null) Color.White else RoseGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Any Outfit",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF381F2A)
                    )
                    Text(
                        text = "Custom Palette",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8A727C)
                    )
                }
            }

            // Uploaded Clothes (Sarees, Dresses, Kurtis, Gowns)
            clothingItems.forEach { clothing ->
                val isSelected = clothing.id == selectedClothingId
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) RoseGoldPrimary else Color(0xFFEFE6EA)
                    ),
                    modifier = Modifier
                        .width(135.dp)
                        .clickable {
                            selectedClothingId = clothing.id
                            // Auto align occasion if matching
                            val matchedOcc = occasions.firstOrNull {
                                it.contains(clothing.occasion.displayName, ignoreCase = true) ||
                                clothing.occasion.displayName.contains(it, ignoreCase = true)
                            }
                            if (matchedOcc != null) {
                                selectedOccasion = matchedOcc
                            }
                        }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(Color(0xFFF9F3F4))
                        ) {
                            val primaryPath = clothing.primaryImageUri
                            val imageModel = if (primaryPath.startsWith("/")) {
                                File(primaryPath)
                            } else if (primaryPath.isNotBlank()) {
                                primaryPath
                            } else null

                            if (imageModel != null) {
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = clothing.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DryCleaning,
                                        contentDescription = null,
                                        tint = RoseGoldPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = RoseGoldPrimary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Category pill at bottom
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = clothing.category.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = clothing.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF2E1C25)
                            )

                            Text(
                                text = clothing.metal.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF8A727C),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (clothing.dimensions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFBECEF)
                                ) {
                                    Text(
                                        text = clothing.dimensions,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = RoseGoldPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedClothingItem != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF0F3)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = RoseGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Styling around '${selectedClothingItem.title}' (${selectedClothingItem.metal.displayName})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF6B3A4F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 2: Occasion
        Text(
            text = "2. What is the occasion?",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF381F2A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            occasions.forEach { occ ->
                LuxuryChip(
                    label = occ,
                    isSelected = occ == selectedOccasion,
                    onClick = { selectedOccasion = occ }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 3: Outfit Color
        Text(
            text = "3. Outfit Color Palette",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF381F2A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            outfitColors.forEach { (name, color) ->
                val isSelected = name == selectedColor
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) RoseGoldPrimary else Color(0xFFF9ECEE),
                    modifier = Modifier.clickable { selectedColor = name }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else Color(0xFF4C3842)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Generate Button
        Button(
            onClick = {
                onRequestStyling(selectedOccasion, selectedColor, "V-Neck", selectedClothingId)
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
            enabled = !uiState.isStylistLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.isStylistLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Styling Your Look...", color = Color.White)
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedClothingItem != null) "Style Around This Outfit ✨" else "Style My Look ✨",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Suggested Look Result
        val look = uiState.suggestedLook
        if (look != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = look.lookTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF2E1C25),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "${look.harmonyScore}% Match",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Materials & Metals: ${look.metalPalette}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ChampagneGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF9F9)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RoseGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = look.stylistAdvice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF67525D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Curated Pieces from Your Wardrobe (${look.matchedItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF381F2A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    look.matchedItems.forEach { matchedItem ->
                        AccessoryCard(
                            item = matchedItem,
                            onItemClick = onItemClick,
                            onFavoriteToggle = onFavoriteToggle,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // API Readiness Documentation Card (Future Extensibility)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7EFF2)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showApiDetails = !showApiDetails }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = RoseGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Future API Connection Ready",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3B1E29)
                        )
                    }

                    Text(
                        text = if (showApiDetails) "Hide" else "Show Details",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoseGoldPrimary
                    )
                }

                if (showApiDetails) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The app architecture includes a dedicated `JewelrySuggestionApiService` interface. When you are ready to link your remote backend or LLM (e.g. Gemini Vision / OpenAI / FastAPI):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5D4652)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Send JSON array of user's uploaded wardrobe items (sarees, dresses, jewelry, bags)\n• Pass selected clothing item or outfit color & event\n• Return AI ranked recommendations & styling narrative",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6F5260)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
