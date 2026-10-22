package com.adorn.wardrobe.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBottomSheet(
    item: AccessoryItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEditClick: (AccessoryItem) -> Unit,
    onDeleteClick: (AccessoryItem) -> Unit,
    onFavoriteToggle: (AccessoryItem) -> Unit,
    modifier: Modifier = Modifier,
    onMoveToWardrobe: ((AccessoryItem) -> Unit)? = null,
    onMoveToWishlist: ((AccessoryItem) -> Unit)? = null,
    onIncrementTimesWorn: ((AccessoryItem, Int) -> Unit)? = null,
    onSimulatePriceDrop: ((AccessoryItem) -> Unit)? = null,
    onCheckLivePrice: ((AccessoryItem) -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header with image / multi-image pager
            val photos = item.imageUris.filter { it.isNotBlank() }
            val pagerState = rememberPagerState(pageCount = { if (photos.isNotEmpty()) photos.size else 1 })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(Color(0xFFF9F3F4))
            ) {
                if (photos.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val currentPath = photos[pageIndex]
                        val imageModel = if (currentPath.startsWith("/")) {
                            File(currentPath)
                        } else {
                            currentPath
                        }

                        AsyncImage(
                            model = imageModel,
                            contentDescription = "${item.title} Photo $pageIndex",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Dot Indicators & Photo Counter (if multiple photos)
                    if (photos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(photos.size) { dotIdx ->
                                Box(
                                    modifier = Modifier
                                        .size(if (pagerState.currentPage == dotIdx) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == dotIdx) RoseGoldPrimary else Color.White.copy(alpha = 0.7f)
                                        )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${photos.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFCE9EC))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = RoseGoldPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Close Button Top-Start
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF332028),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Top-End Actions: Share & Favorite
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = { com.adorn.wardrobe.util.ShareHelper.shareProduct(context, item) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF332028),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onFavoriteToggle(item) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFE63946) else Color(0xFF635058),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Body Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Category & Metal Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA17C88),
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    MetalBadge(metal = item.metal)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF281A22)
                )

                // If it's a Matching Set Banner
                if (item.isSet) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFDF0F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseGoldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RoseGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MATCHING JEWELRY SET",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RoseGoldPrimary
                                )
                                if (item.setIncludes.isNotBlank()) {
                                    Text(
                                        text = "Includes: ${item.setIncludes}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF332028)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Value Tag with Strikethrough & Savings
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (item.isWishlist) item.formattedTargetPrice else item.formattedPrice,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (item.hasPriceDrop) Color(0xFFC62828) else RoseGoldPrimary
                    )
                    if (item.hasPriceDrop && item.formattedOriginalPrice.isNotBlank()) {
                        Text(
                            text = item.formattedOriginalPrice,
                            style = MaterialTheme.typography.titleMedium.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = Color(0xFF8F7A85),
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                    Text(
                        text = if (item.isWishlist) " (target budget)" else " (est. value)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8F7A85),
                        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                    )
                }

                // Price Drop Alert Banner
                if (item.hasPriceDrop) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF0F2),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PRICE DROP: ${item.priceDropPercent}% OFF!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "Dropped from ${item.formattedOriginalPrice} to ${item.formattedPrice}. You save ${item.formattedPriceDropSavings}! ✨",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5A3C44)
                                )
                            }
                        }
                    }
                }

                // Wear Tracking & Aging Analysis Card (For Dresses, Saree & Wardrobe pieces)
                if (!item.isWishlist) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF4F7)),
                        border = BorderStroke(1.dp, Color(0xFFEADBDF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👗", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Times Worn Counter",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF332028)
                                        )
                                        Text(
                                            text = item.wearCondition.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(item.wearCondition.colorHex)
                                        )
                                    }
                                }

                                // Wear Stepper [-] count [+]
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onIncrementTimesWorn?.invoke(item, -1) },
                                        enabled = item.timesWorn > 0,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.White, CircleShape)
                                    ) {
                                        Text("–", fontWeight = FontWeight.Bold, color = RoseGoldPrimary)
                                    }
                                    Text(
                                        text = "${item.timesWorn}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF332028),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { onIncrementTimesWorn?.invoke(item, 1) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(RoseGoldPrimary, CircleShape)
                                    ) {
                                        Text("+", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF0E2E5))
                            Spacer(modifier = Modifier.height(8.dp))

                            // "Have they become old?" Analysis
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = if (item.wearCondition.isAging) "⏳" else "✨",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (item.wearCondition.isAging) {
                                            "Has it become old? Yes — Aging / Vintage"
                                        } else {
                                            "Has it become old? No — Fresh & In Prime Rotation"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (item.wearCondition.isAging) Color(0xFFE65100) else Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = item.wearCondition.advice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B5862)
                                    )
                                }
                            }

                            if (item.costPerWear != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💰", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Realized Value: ${item.formattedCostPerWear}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF7B2CBF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onIncrementTimesWorn?.invoke(item, 1) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF7ECEE),
                                    contentColor = RoseGoldPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✨ +1 Wore This Today", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Offline Image Safe Confirmation
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F8F2),
                    border = BorderStroke(1.dp, Color(0xFFDCEDC8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💾", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Images Stored Offline on Device",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Photos are permanently cached on this phone. Even if the store link is removed, deleted, or 404s, your images are safely preserved.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4C6B43),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF3E4E7))
                Spacer(modifier = Modifier.height(16.dp))

                // Specifications Grid Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SpecRow(label = "Material / Metal", value = item.metal.displayName)
                        if (item.purity.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Purity / Detail", value = item.purity)
                        }
                        if (item.weightGrams != null && item.weightGrams > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Weight", value = "${item.weightGrams} grams")
                        }
                        if (item.dimensions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Dimensions / Size", value = item.dimensions)
                        }
                        if (item.isSet && item.setIncludes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Set Includes", value = item.setIncludes)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SpecRow(label = "Occasion", value = item.occasion.displayName)

                        if (item.brand.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Brand / Designer", value = item.brand)
                        }
                    }
                }

                // Storage Location Card
                if (item.storageLocation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAF1F3),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = RoseGoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kept In / Storage Location",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8F7580)
                                )
                                Text(
                                    text = item.storageLocation,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF332028)
                                )
                            }
                        }
                    }
                }

                // Notes / Story
                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Notes & Story",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3B2731)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6B5862)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Store App Link Button
                if (item.storeUrl.isNotBlank()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    OutlinedButton(
                        onClick = {
                            com.adorn.wardrobe.util.StoreRedirectHelper.openStore(context, item.storeUrl)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFBF0E6),
                            contentColor = Color(0xFFC06A28)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEED2BA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
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
                        Text("$storeIcon Open in $storeName App", fontWeight = FontWeight.Bold)
                    }

                    // Check Live Store Price button
                    OutlinedButton(
                        onClick = { onCheckLivePrice?.invoke(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF0F4F8),
                            contentColor = Color(0xFF1565C0)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBDEFB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check Live Store Price 🔍", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Simulate Price Drop button
                OutlinedButton(
                    onClick = { onSimulatePriceDrop?.invoke(item) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFF4F5),
                        contentColor = Color(0xFFC62828)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Price Drop Alert 🔥 (-20%)", fontWeight = FontWeight.SemiBold)
                }

                if (item.isWishlist && onMoveToWardrobe != null) {
                    Button(
                        onClick = {
                            onMoveToWardrobe(item)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ Acquired! Move to Wardrobe", fontWeight = FontWeight.Bold)
                    }
                }

                // Share Product Look Button
                val context = androidx.compose.ui.platform.LocalContext.current
                OutlinedButton(
                    onClick = { com.adorn.wardrobe.util.ShareHelper.shareProduct(context, item) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFBF4F6),
                        contentColor = RoseGoldPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseGoldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Product Look ✨", fontWeight = FontWeight.SemiBold)
                }

                // Actions: Edit and Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEditClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Details")
                    }

                    OutlinedButton(
                        onClick = { onDeleteClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC93B4E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Item")
                    }
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7E6672)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF2E1C25)
        )
    }
}
