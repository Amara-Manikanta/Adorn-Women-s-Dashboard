package com.adorn.wardrobe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.ui.theme.BadgeDiamondBg
import com.adorn.wardrobe.ui.theme.BadgeDiamondText
import com.adorn.wardrobe.ui.theme.BadgeGoldBg
import com.adorn.wardrobe.ui.theme.BadgeGoldText
import com.adorn.wardrobe.ui.theme.BadgeRoseBg
import com.adorn.wardrobe.ui.theme.BadgeRoseText
import com.adorn.wardrobe.ui.theme.BadgeSilverBg
import com.adorn.wardrobe.ui.theme.BadgeSilverText
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import java.io.File

@Composable
fun AccessoryCard(
    item: AccessoryItem,
    onItemClick: (AccessoryItem) -> Unit,
    onFavoriteToggle: (AccessoryItem) -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: ((AccessoryItem) -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isSelected) {
            BorderStroke(2.dp, RoseGoldPrimary)
        } else {
            BorderStroke(1.dp, Color(0xFFF4EAEC))
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp,
            pressedElevation = 4.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) {
                    onSelectToggle?.invoke(item)
                } else {
                    onItemClick(item)
                }
            }
    ) {
        Column {
            // Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(if (item.hasCutout) Color(0xFFFBF4F5) else Color(0xFFF9F3F4))
            ) {
                val primaryPath = if (item.hasCutout) item.cutoutUri else item.primaryImageUri
                val imageModel = if (primaryPath.startsWith("/")) {
                    File(primaryPath)
                } else if (primaryPath.isNotBlank()) {
                    primaryPath
                } else null

                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = item.title,
                        contentScale = if (item.hasCutout) ContentScale.Fit else ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (item.hasCutout) 8.dp else 0.dp)
                    )
                } else {
                    // Fallback elegant placeholder
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFDE8EC), Color(0xFFF9D6DC))
                                )
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RoseGoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseGoldPrimary
                            )
                        }
                    }
                }

                // Selection Checkmark Badge or Category/Set Badge on top-start
                if (isSelectionMode) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(28.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) RoseGoldPrimary else Color.Black.copy(alpha = 0.4f)
                            )
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier.size(24.dp)
                            ) {}
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MetalBadge(metal = item.metal)
                        if (item.isSet) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF0F2),
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = "✨ Set",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RoseGoldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Tags row on bottom-start: Price Drop badge, Wishlist priority, or Wear count
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (item.hasPriceDrop) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC62828),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = "🔥 -${item.priceDropPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (item.isWishlist) {
                        val (prioBg, prioText, prioLabel) = when (item.priority.uppercase()) {
                            "HIGH" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "🔥 High Priority")
                            "LOW" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Aspirational")
                            else -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "⭐ Target")
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = prioBg,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = prioLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = prioText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        // Wardrobe Item: Show Wear Count & Aging diagnosis
                        val (wearBg, wearColor, wearText) = when {
                            item.wearCondition.isAging -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "⏳ Aging (${item.timesWorn}x)")
                            item.timesWorn > 0 -> Triple(Color(0xFFF3E5F5), Color(0xFF6A1B9A), "👗 Worn ${item.timesWorn}x")
                            else -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "✨ Brand New")
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = wearBg,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = wearText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = wearColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (item.imageUris.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${item.imageUris.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Favorite button on top-right (only in non-selection mode)
                if (!isSelectionMode) {
                    IconButton(
                        onClick = { onFavoriteToggle(item) },
                        modifier = Modifier
                            .padding(6.dp)
                            .size(36.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFE63946) else Color(0xFF8D7B83),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Info Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Category Tag
                Text(
                    text = item.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA17C88),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF2B1D25)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Weight row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (item.isWishlist) item.formattedTargetPrice else item.formattedPrice,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (item.hasPriceDrop) Color(0xFFC62828) else RoseGoldPrimary
                        )
                        if (item.hasPriceDrop && item.formattedOriginalPrice.isNotBlank()) {
                            Text(
                                text = item.formattedOriginalPrice,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = Color(0xFF9E8E95)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.dimensions.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF9ECEE)
                            ) {
                                Text(
                                    text = item.dimensions.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoseGoldPrimary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (item.weightGrams != null && item.weightGrams > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF7EFF2)
                            ) {
                                Text(
                                    text = "${item.weightGrams}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF6F525D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Storage location indicator if available
                if (item.storageLocation.isNotBlank() && !item.isWishlist) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF9E8E95),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = item.storageLocation,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8F7E86),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }
                }

                // Store App Link Button
                if (item.storeUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFBF0E6),
                        border = BorderStroke(1.dp, Color(0xFFEED2BA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                com.adorn.wardrobe.util.StoreRedirectHelper.openStore(context, item.storeUrl)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val storeName = item.storeDisplayName
                            val storeIcon = when (storeName) {
                                "Myntra" -> "🛍️"
                                "Amazon" -> "🛒"
                                "Nykaa" -> "💄"
                                "Ajio" -> "👗"
                                "Zara" -> "✨"
                                else -> "🌐"
                            }
                            Text(
                                text = "$storeIcon Open in $storeName",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC06A28)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetalBadge(metal: MetalType, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (metal) {
        MetalType.GOLD_22K, MetalType.GOLD_18K -> Pair(BadgeGoldBg, BadgeGoldText)
        MetalType.STERLING_SILVER, MetalType.PLATINUM, MetalType.WHITE_GOLD -> Pair(BadgeSilverBg, BadgeSilverText)
        MetalType.ROSE_GOLD -> Pair(BadgeRoseBg, BadgeRoseText)
        MetalType.DIAMONDS -> Pair(BadgeDiamondBg, BadgeDiamondText)
        MetalType.GENUINE_LEATHER, MetalType.VEGAN_LEATHER -> Pair(Color(0xFFF3ECE7), Color(0xFF6D4C41))
        MetalType.EMBELLISHED_ZARI -> Pair(Color(0xFFFCE4EC), Color(0xFFC2185B))
        MetalType.PURE_SILK -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        MetalType.COTTON_LINEN -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
        MetalType.GEORGETTE_CHIFFON -> Pair(Color(0xFFFCE4EC), Color(0xFFD81B60))
        MetalType.VELVET_SATIN -> Pair(Color(0xFFE8EAF6), Color(0xFF283593))
        else -> Pair(Color(0xFFF5EBEB), Color(0xFF755560))
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Text(
            text = metal.shortTag,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
