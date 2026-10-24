package com.adorn.wardrobe.ui.screens

import android.net.Uri
import android.widget.Toast
import com.adorn.wardrobe.data.local.WebProductImporter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adorn.wardrobe.data.local.ImageStorageHelper
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.OccasionType
import com.adorn.wardrobe.ui.components.LuxuryChip
import com.adorn.wardrobe.ui.theme.RoseGoldLight
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    itemBeingEdited: AccessoryItem?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: AccessoryCategory,
        metal: MetalType,
        purity: String,
        price: Double,
        weightGrams: Double?,
        dimensions: String,
        isSet: Boolean,
        setIncludes: String,
        occasion: OccasionType,
        location: String,
        brand: String,
        notes: String,
        imageUris: List<String>,
        storeUrl: String,
        originalPrice: Double,
        timesWorn: Int,
        cutoutUri: String,
        dominantColorHex: String,
        accentColorHex: String,
        colorName: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(itemBeingEdited?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(itemBeingEdited?.category ?: AccessoryCategory.EARRINGS) }
    var selectedMetal by remember { mutableStateOf(itemBeingEdited?.metal ?: MetalType.ROSE_GOLD) }
    var purity by remember { mutableStateOf(itemBeingEdited?.purity ?: "") }
    var priceText by remember { mutableStateOf(if (itemBeingEdited != null && itemBeingEdited.price > 0) itemBeingEdited.price.toString() else "") }
    var originalPriceText by remember { mutableStateOf(if (itemBeingEdited != null && itemBeingEdited.originalPrice > 0) itemBeingEdited.originalPrice.toString() else "") }
    var timesWornText by remember { mutableStateOf(if (itemBeingEdited != null) itemBeingEdited.timesWorn.toString() else "0") }
    var weightText by remember { mutableStateOf(itemBeingEdited?.weightGrams?.toString() ?: "") }
    var dimensions by remember { mutableStateOf(itemBeingEdited?.dimensions ?: "") }
    var isSet by remember { mutableStateOf(itemBeingEdited?.isSet ?: (itemBeingEdited?.category == AccessoryCategory.MATCHING_SETS)) }
    var setIncludes by remember { mutableStateOf(itemBeingEdited?.setIncludes ?: "") }
    var selectedOccasion by remember { mutableStateOf(itemBeingEdited?.occasion ?: OccasionType.DAILY) }
    var storageLocation by remember { mutableStateOf(itemBeingEdited?.storageLocation ?: "") }
    var brand by remember { mutableStateOf(itemBeingEdited?.brand ?: "") }
    var isBrandDropdownExpanded by remember { mutableStateOf(false) }
    var storeUrl by remember { mutableStateOf(itemBeingEdited?.storeUrl ?: "") }
    var notes by remember { mutableStateOf(itemBeingEdited?.notes ?: "") }

    // Google AI Edge LiteRT Background Removal & Color Analysis
    var cutoutUri by remember { mutableStateOf(itemBeingEdited?.cutoutUri ?: "") }
    var dominantColorHex by remember { mutableStateOf(itemBeingEdited?.dominantColorHex ?: "") }
    var accentColorHex by remember { mutableStateOf(itemBeingEdited?.accentColorHex ?: "") }
    var colorName by remember { mutableStateOf(itemBeingEdited?.colorName ?: "") }
    var isSegmenting by remember { mutableStateOf(false) }

    // Multi-image list
    val imageList = remember {
        mutableStateListOf<String>().apply {
            if (itemBeingEdited != null) {
                addAll(itemBeingEdited.imageUris)
            }
        }
    }

    // Web image URL dialog state
    var showWebUrlDialog by remember { mutableStateOf(false) }
    var webUrlInput by remember { mutableStateOf("") }
    var isDownloadingWebImage by remember { mutableStateOf(false) }

    // Temporary camera capture URI
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Multi-photo picker (allows selecting multiple images at once)
    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val copied = ImageStorageHelper.copyUriToInternalStorage(context, uri)
            imageList.add(copied ?: uri.toString())
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraUri != null) {
            val copied = ImageStorageHelper.copyUriToInternalStorage(context, cameraUri!!)
            imageList.add(copied ?: cameraUri.toString())
        }
    }

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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (itemBeingEdited == null) "Add to Wardrobe" else "Edit Item Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF351F2A)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF6B535F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-Photo Upload Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product Photos (${imageList.size})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF4A343F)
                )

                if (imageList.isNotEmpty()) {
                    Text(
                        text = "First image is cover",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8C737E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Photos Preview Strip
            if (imageList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    imageList.forEachIndexed { index, path ->
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF9ECEE))
                                .border(
                                    1.dp,
                                    if (index == 0) RoseGoldPrimary else Color(0xFFEEDBDD),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            val imgModel = if (path.startsWith("/")) File(path) else path
                            AsyncImage(
                                model = imgModel,
                                contentDescription = "Photo $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // "Cover" badge for index 0
                            if (index == 0) {
                                Surface(
                                    shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 10.dp),
                                    color = RoseGoldPrimary,
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "Cover",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Delete Image Button
                            IconButton(
                                onClick = { imageList.removeAt(index) },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // "+ Add More" Card Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF3F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D0D4)),
                        modifier = Modifier
                            .size(115.dp)
                            .clickable {
                                multiPhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = RoseGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+ Add More",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = RoseGoldPrimary
                            )
                        }
                    }
                }
            } else {
                // Empty photo placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFBF1F3))
                        .border(1.dp, Color(0xFFF2DADB), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = RoseGoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add multiple angles, close-ups or wearing photos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8C737E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo Action Source Buttons (Gallery, Camera, Myntra/Web Link)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gallery (Multi-select)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF7ECEE),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            multiPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = RoseGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gallery",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RoseGoldPrimary
                        )
                    }
                }

                // Camera
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF7ECEE),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                val (tempUri, _) = ImageStorageHelper.createTempCameraUri(context)
                                cameraUri = tempUri
                                cameraLauncher.launch(tempUri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = RoseGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Camera",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RoseGoldPrimary
                        )
                    }
                }

                // Web / Myntra Link Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFBF0E6),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEED2BA)),
                    modifier = Modifier
                        .weight(1.1f)
                        .clickable { showWebUrlDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFFC06A28),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Web Link",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFC06A28)
                        )
                    }
                }

                // Barcode / QR Scanner Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5EDF9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2CDEB)),
                    modifier = Modifier
                        .weight(1.1f)
                        .clickable {
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                com.adorn.wardrobe.util.BarcodeScannerHelper.scan(
                                    activity = activity,
                                    onCodeScanned = { rawCode, isUrl ->
                                        if (isUrl) {
                                            storeUrl = rawCode
                                            webUrlInput = rawCode
                                            showWebUrlDialog = true
                                            android.widget.Toast.makeText(context, "QR Link Scanned! ✨", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            notes = if (notes.isBlank()) "Barcode / SKU: $rawCode" else "$notes\nBarcode / SKU: $rawCode"
                                            android.widget.Toast.makeText(context, "Tag Scanned: $rawCode ✨", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onError = { err ->
                                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF7B2CBF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Barcode",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF7B2CBF)
                        )
                    }
                }
            }

            // Google AI Edge LiteRT Background Removal Card
            if (imageList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFBF4F6),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D0D5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = RoseGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google AI Edge LiteRT",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4A343F)
                                )
                            }

                            if (cutoutUri.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "Cutout Ready ✨",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (cutoutUri.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEEEEEE))
                                        .border(1.dp, Color(0xFFD6D6D6), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = cutoutUri,
                                        contentDescription = "Cutout",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    if (colorName.isNotBlank() || dominantColorHex.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (dominantColorHex.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            try {
                                                                Color(android.graphics.Color.parseColor(dominantColorHex))
                                                            } catch (e: Exception) { RoseGoldPrimary }
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = colorName.ifBlank { "Palette Analyzed" },
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF381F2A)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Transparent PNG saved in app storage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8C737E)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Re-run Cutout",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = RoseGoldPrimary,
                                            modifier = Modifier.clickable {
                                                coroutineScope.launch {
                                                    isSegmenting = true
                                                    try {
                                                        val res = com.adorn.wardrobe.ml.LiteRtSegmenter.removeBackgroundAndExtractColors(
                                                            context,
                                                            imageList.first()
                                                        )
                                                        cutoutUri = res.cutoutPath
                                                        dominantColorHex = res.dominantColorHex
                                                        accentColorHex = res.accentColorHex
                                                        colorName = res.colorName
                                                        android.widget.Toast.makeText(context, "Cutout & Palette Generated! ✨", android.widget.Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        android.widget.Toast.makeText(context, "Cutout failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                                    } finally {
                                                        isSegmenting = false
                                                    }
                                                }
                                            }
                                        )

                                        Text(
                                            text = "Clear Cutout",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            modifier = Modifier.clickable {
                                                cutoutUri = ""
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Isolate garment with alpha transparency and extract color palette on-device with LiteRT.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF67525D)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isSegmenting = true
                                        try {
                                            val res = com.adorn.wardrobe.ml.LiteRtSegmenter.removeBackgroundAndExtractColors(
                                                context,
                                                imageList.first()
                                            )
                                            cutoutUri = res.cutoutPath
                                            dominantColorHex = res.dominantColorHex
                                            accentColorHex = res.accentColorHex
                                            colorName = res.colorName
                                            android.widget.Toast.makeText(context, "Cutout & ${res.colorName} extracted! ✨", android.widget.Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Segmentation failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSegmenting = false
                                        }
                                    }
                                },
                                enabled = !isSegmenting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                if (isSegmenting) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Processing on-device with LiteRT...", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Isolate Garment / Remove Background ✨", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Accessory Title
            Text(
                text = "Item Name *",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g., Kanjeevaram Saree, Floral Dress, Gold Studs, Flap Bag") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = defaultTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection Chips
            Text(
                text = "Category *",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccessoryCategory.selectableCategories().forEach { cat ->
                    LuxuryChip(
                        label = cat.displayName,
                        isSelected = cat == selectedCategory,
                        onClick = {
                            selectedCategory = cat
                            if (cat == AccessoryCategory.MATCHING_SETS) {
                                isSet = true
                                if (setIncludes.isBlank()) setIncludes = "Necklace + Earrings"
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Matching Jewelry Set / Combo Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSet) Color(0xFFFDF0F2) else Color(0xFFFBF6F7),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSet) RoseGoldPrimary else Color(0xFFEADBDE)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isSet = !isSet
                        if (isSet && setIncludes.isBlank()) {
                            setIncludes = "Necklace + Earrings"
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = isSet,
                            onCheckedChange = {
                                isSet = it
                                if (it && setIncludes.isBlank()) setIncludes = "Necklace + Earrings"
                            },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = RoseGoldPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Matching Jewelry Set / Combo ✨",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF381F2A)
                            )
                            Text(
                                text = "e.g., Chain + Matching Earrings, Choker + Jhumkas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8B717D)
                            )
                        }
                    }

                    if (isSet) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Quick Combo Options:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF6C4F5C)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Necklace + Earrings",
                                "Chain + Studs",
                                "Choker + Jhumkas",
                                "Chain + Bracelet",
                                "Full Bridal Set"
                            ).forEach { combo ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (setIncludes == combo) RoseGoldPrimary else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D0D4)),
                                    modifier = Modifier.clickable { setIncludes = combo }
                                ) {
                                    Text(
                                        text = combo,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (setIncludes == combo) Color.White else Color(0xFF4C3642),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = setIncludes,
                            onValueChange = { setIncludes = it },
                            placeholder = { Text("e.g., Necklace + Matching Earrings") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = defaultTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metal / Material Selection Chips
            Text(
                text = "Material / Metal *",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetalType.selectableMetals().forEach { metal ->
                    LuxuryChip(
                        label = metal.shortTag,
                        isSelected = metal == selectedMetal,
                        isSecondary = true,
                        onClick = { selectedMetal = metal }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price & Purity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Price (₹)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4A343F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        placeholder = { Text("e.g., 4500") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Original / MRP (₹)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4A343F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = originalPriceText,
                        onValueChange = { originalPriceText = it },
                        placeholder = { Text("For Price Drop") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Times Worn Counter 👗",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4A343F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = timesWornText,
                        onValueChange = { timesWornText = it },
                        placeholder = { Text("0 = Brand new") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Purity / Detail",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4A343F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = purity,
                        onValueChange = { purity = it },
                        placeholder = { Text("e.g., 22K, 925, Silk") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight & Occasion Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weight (grams, optional)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4A343F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        placeholder = { Text("e.g., 14.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Occasion",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4A343F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = selectedOccasion.displayName,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            Text(
                                "Next",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseGoldPrimary,
                                modifier = Modifier
                                    .clickable {
                                        val nextIdx = (selectedOccasion.ordinal + 1) % OccasionType.entries.size
                                        selectedOccasion = OccasionType.entries[nextIdx]
                                    }
                                    .padding(end = 12.dp)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dimensions / Sizing Section
            val dimLabel = when {
                selectedCategory.isClothing -> "Clothing Size / Fit / Length"
                selectedCategory == AccessoryCategory.CHAINS_NECKLACES -> "Chain / Necklace Length"
                selectedCategory == AccessoryCategory.MATCHING_SETS -> "Set Dimensions / Chain Length"
                selectedCategory == AccessoryCategory.BRACELETS_BANGLES -> "Bangle Size / Wrist Circumference"
                selectedCategory == AccessoryCategory.HANDBAGS || selectedCategory == AccessoryCategory.CLUTCHES_SLINGS -> "Bag Dimensions (L x W x H)"
                selectedCategory == AccessoryCategory.RINGS -> "Ring Size (Indian / US)"
                selectedCategory == AccessoryCategory.EARRINGS -> "Earring Drop Length / Stud Size"
                else -> "Dimensions / Size"
            }

            val dimPlaceholder = when {
                selectedCategory.isClothing -> "e.g., S (36), M (38), L (40), Free Size, 6.3m Saree"
                selectedCategory == AccessoryCategory.CHAINS_NECKLACES -> "e.g., 18\" (Princess), 22\" (Matinee), 45 cm"
                selectedCategory == AccessoryCategory.MATCHING_SETS -> "e.g., Choker 16\" adjustable, Earrings 2.5\" drop"
                selectedCategory == AccessoryCategory.BRACELETS_BANGLES -> "e.g., Size 2.6 / 60mm diameter, 7 inches wrist"
                selectedCategory == AccessoryCategory.HANDBAGS || selectedCategory == AccessoryCategory.CLUTCHES_SLINGS -> "e.g., 28cm x 18cm x 8cm"
                selectedCategory == AccessoryCategory.RINGS -> "e.g., Size 14 (Indian) / 7 (US)"
                selectedCategory == AccessoryCategory.EARRINGS -> "e.g., 2.5\" drop length, 8mm stud"
                else -> "e.g., Length 18 inches, Width 2mm"
            }

            Text(
                text = dimLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dimensions,
                onValueChange = { dimensions = it },
                placeholder = { Text(dimPlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = defaultTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Sizing Pills for Clothes, Chains or Bangles
            if (selectedCategory.isClothing) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("XS (34)", "S (36)", "M (38)", "L (40)", "XL (42)", "Free Size", "6.3m Saree", "Tailored").forEach { sizeHint ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF9ECEE),
                            modifier = Modifier.clickable { dimensions = sizeHint }
                        ) {
                            Text(
                                text = sizeHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseGoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else if (selectedCategory == AccessoryCategory.CHAINS_NECKLACES || selectedCategory == AccessoryCategory.MATCHING_SETS) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("16\" Choker", "18\" Princess", "20\" Matinee", "24\" Opera").forEach { lengthHint ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF9ECEE),
                            modifier = Modifier.clickable { dimensions = lengthHint }
                        ) {
                            Text(
                                text = lengthHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseGoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else if (selectedCategory == AccessoryCategory.BRACELETS_BANGLES) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Size 2.4", "Size 2.6", "Size 2.8", "7.0\" Wrist").forEach { sizeHint ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF9ECEE),
                            modifier = Modifier.clickable { dimensions = sizeHint }
                        ) {
                            Text(
                                text = sizeHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseGoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Location
            Text(
                text = "Where is it kept? (Storage Location)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = storageLocation,
                onValueChange = { storageLocation = it },
                placeholder = { Text("e.g., Wardrobe Bag Shelf, Velvet Tray #2, Safe Locker") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = defaultTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Dropdown & Quick Selection Pills
            val popularBrands = remember {
                listOf(
                    "Tanishq",
                    "CaratLane",
                    "Mia by Tanishq",
                    "Malabar Gold",
                    "Kalyan Jewellers",
                    "Giva",
                    "Bluestone",
                    "Swarovski",
                    "Tiffany & Co.",
                    "Cartier",
                    "Bvlgari",
                    "Sabyasachi",
                    "FabIndia",
                    "Biba",
                    "W for Woman",
                    "Nalli Silks",
                    "Global Desi",
                    "Ritu Kumar",
                    "Kalki Fashion",
                    "Manyavar Mohey",
                    "Mango",
                    "Forever New",
                    "AND",
                    "Tribe Amrapali",
                    "Kushal's Fashion",
                    "Titan / Raga",
                    "Fossil",
                    "Michael Kors",
                    "Coach",
                    "Kate Spade",
                    "Charles & Keith",
                    "Miraggio",
                    "Da Milano",
                    "Lavie",
                    "Caprese",
                    "Baggit",
                    "Zara",
                    "H&M",
                    "Louis Vuitton",
                    "Chanel",
                    "Gucci",
                    "Family Heirloom",
                    "Local Jeweler / Goldsmith",
                    "Custom Handmade"
                )
            }

            val filteredBrands = remember(brand) {
                if (brand.isBlank()) popularBrands
                else popularBrands.filter { it.contains(brand, ignoreCase = true) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Brand / Designer / Jeweler",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF4A343F)
                )

                Text(
                    text = "Select or type",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8C737E)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = {
                        brand = it
                        isBrandDropdownExpanded = true
                    },
                    placeholder = { Text("Select from dropdown or type...") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { isBrandDropdownExpanded = !isBrandDropdownExpanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Open Brands Dropdown",
                                tint = RoseGoldPrimary
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = defaultTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = isBrandDropdownExpanded,
                    onDismissRequest = { isBrandDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .heightIn(max = 260.dp)
                        .background(Color.White)
                ) {
                    if (filteredBrands.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Custom: \"$brand\"",
                                    fontWeight = FontWeight.Bold,
                                    color = RoseGoldPrimary
                                )
                            },
                            onClick = { isBrandDropdownExpanded = false }
                        )
                    } else {
                        filteredBrands.forEach { bName ->
                            val isSelected = bName.equals(brand, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = bName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) RoseGoldPrimary else Color(0xFF332028)
                                    )
                                },
                                onClick = {
                                    brand = bName
                                    isBrandDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick Brand Selection Pills
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Tanishq",
                    "CaratLane",
                    "Zara",
                    "Biba",
                    "Nalli Silks",
                    "FabIndia",
                    "Giva",
                    "Michael Kors",
                    "Coach",
                    "Titan Raga",
                    "Swarovski",
                    "Heirloom"
                ).forEach { quickBrand ->
                    val isChipSelected = brand.equals(quickBrand, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChipSelected) RoseGoldPrimary else Color(0xFFF9ECEE),
                        modifier = Modifier.clickable { brand = quickBrand }
                    ) {
                        Text(
                            text = quickBrand,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isChipSelected) Color.White else Color(0xFF6E5661),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Store / Product URL
            Text(
                text = "Store / Product Web Link",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = storeUrl,
                onValueChange = { storeUrl = it },
                placeholder = { Text("e.g., https://www.myntra.com/... or amazon.in/dp/...") },
                shape = RoundedCornerShape(14.dp),
                colors = defaultTextFieldColors(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Personal Story / Notes",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4A343F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("e.g., Ordered from Myntra festive sale, matches emerald silk saree") },
                shape = RoundedCornerShape(14.dp),
                colors = defaultTextFieldColors(),
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    val origPriceVal = originalPriceText.toDoubleOrNull() ?: 0.0
                    val timesWornVal = timesWornText.toIntOrNull() ?: 0
                    val weightVal = weightText.toDoubleOrNull()
                    val finalTitle = title.ifBlank { "${selectedMetal.shortTag} ${selectedCategory.displayName}" }

                    onSave(
                        finalTitle,
                        selectedCategory,
                        selectedMetal,
                        purity,
                        priceVal,
                        weightVal,
                        dimensions,
                        isSet,
                        setIncludes,
                        selectedOccasion,
                        storageLocation,
                        brand,
                        notes,
                        imageList.toList(),
                        storeUrl.trim(),
                        origPriceVal,
                        timesWornVal,
                        cutoutUri,
                        dominantColorHex,
                        accentColorHex,
                        colorName
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (itemBeingEdited == null) "Add to My Wardrobe ✨" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog: Add Image from Myntra / Web Store Link
    if (showWebUrlDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDownloadingWebImage) showWebUrlDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFFC06A28),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add from Myntra or Web",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Paste a product link from Myntra, Ajio, Nykaa, Zara, Amazon or any image URL. We'll automatically download high-resolution photos and auto-fill product details!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B5562)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = webUrlInput,
                        onValueChange = { webUrlInput = it },
                        placeholder = { Text("https://www.myntra.com/kurta-sets/... or image URL") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = defaultTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val url = webUrlInput.trim()
                        if (url.isNotBlank()) {
                            isDownloadingWebImage = true
                            coroutineScope.launch {
                                val result = WebProductImporter.importFromUrl(context, url)
                                isDownloadingWebImage = false

                                if (result.errorMessage != null && result.imageLocalPaths.isEmpty()) {
                                    Toast.makeText(context, result.errorMessage, Toast.LENGTH_LONG).show()
                                } else {
                                    // Add downloaded photos (filtering out duplicates)
                                    result.imageLocalPaths.forEach { imgPath ->
                                        if (!imageList.contains(imgPath)) {
                                            imageList.add(imgPath)
                                        }
                                    }

                                    // If it's a product page, auto-fill the form fields!
                                    if (result.isProductPage) {
                                        if (title.isBlank() && result.title.isNotBlank()) {
                                            title = result.title
                                        }
                                        if (brand.isBlank() && result.brand.isNotBlank()) {
                                            brand = result.brand
                                        }
                                        if (priceText.isBlank() && result.price != null && result.price > 0.0) {
                                            priceText = result.price.toInt().toString()
                                        }
                                        if (result.category != null) {
                                            selectedCategory = result.category
                                        }
                                        if (result.metalOrFabric != null) {
                                            selectedMetal = result.metalOrFabric
                                        }
                                        if (purity.isBlank() && result.purityOrDetail.isNotBlank()) {
                                            purity = result.purityOrDetail
                                        }
                                        if (dimensions.isBlank() && result.dimensions.isNotBlank()) {
                                            dimensions = result.dimensions
                                        }
                                        if (result.isSet) {
                                            isSet = true
                                            if (result.setIncludes.isNotBlank()) {
                                                setIncludes = result.setIncludes
                                            }
                                        }
                                        if (result.occasion != null) {
                                            selectedOccasion = result.occasion
                                        }
                                        if (notes.isBlank() && result.notes.isNotBlank()) {
                                            notes = result.notes
                                        }
                                        if (storeUrl.isBlank()) {
                                            storeUrl = if (result.storeUrl.isNotBlank()) result.storeUrl else url
                                        }

                                        val storeName = if (url.contains("amazon", ignoreCase = true)) "Amazon" else "Myntra / Web"
                                        Toast.makeText(
                                            context,
                                            "Imported from $storeName: ${result.imageLocalPaths.size} photos & details auto-filled! ✨",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Photo saved to product gallery! ✨",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    webUrlInput = ""
                                    showWebUrlDialog = false
                                }
                            }
                        }
                    },
                    enabled = webUrlInput.isNotBlank() && !isDownloadingWebImage,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldPrimary)
                ) {
                    if (isDownloadingWebImage) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importing...", color = Color.White)
                    } else {
                        Text("Import & Add Photos", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWebUrlDialog = false },
                    enabled = !isDownloadingWebImage
                ) {
                    Text("Cancel", color = Color(0xFF6B5562))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun defaultTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFFAF4F5),
    unfocusedContainerColor = Color(0xFFFAF4F5),
    focusedIndicatorColor = RoseGoldPrimary,
    unfocusedIndicatorColor = Color(0xFFEEDADB)
)
