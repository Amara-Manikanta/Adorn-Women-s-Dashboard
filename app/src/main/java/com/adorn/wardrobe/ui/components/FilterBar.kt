package com.adorn.wardrobe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.ui.theme.RoseGoldLight
import com.adorn.wardrobe.ui.theme.RoseGoldPrimary

@Composable
fun FilterBar(
    selectedCategory: AccessoryCategory,
    onCategorySelected: (AccessoryCategory) -> Unit,
    selectedMetal: MetalType,
    onMetalSelected: (MetalType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessoryCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                LuxuryChip(
                    label = category.displayName,
                    isSelected = isSelected,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        // Metal Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetalType.entries.forEach { metal ->
                val isSelected = metal == selectedMetal
                LuxuryChip(
                    label = metal.shortTag,
                    isSelected = isSelected,
                    isSecondary = true,
                    onClick = { onMetalSelected(metal) }
                )
            }
        }
    }
}

@Composable
fun LuxuryChip(
    label: String,
    isSelected: Boolean,
    isSecondary: Boolean = false,
    onClick: () -> Unit
) {
    val activeBg = if (isSecondary) Color(0xFFC9A227) else RoseGoldPrimary
    val inactiveBg = Color(0xFFF7ECEE)
    val activeText = Color.White
    val inactiveText = Color(0xFF6B515C)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) activeBg else inactiveBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.2.sp
            ),
            color = if (isSelected) activeText else inactiveText
        )
    }
}
