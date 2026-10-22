package com.adorn.wardrobe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = RoseGoldPrimary,
    onPrimary = WarmCardSurface,
    primaryContainer = SoftBlushSurface,
    onPrimaryContainer = VelvetPlum,
    secondary = ChampagneGold,
    onSecondary = WarmCardSurface,
    secondaryContainer = ChampagneGoldLight,
    onSecondaryContainer = VelvetPlum,
    background = WarmCreamBackground,
    onBackground = EspressoText,
    surface = WarmCardSurface,
    onSurface = EspressoText,
    surfaceVariant = SoftBlushSurface,
    onSurfaceVariant = MutedText,
    outline = SoftBlushBorder
)

@Composable
fun AdornWardrobeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally keep the luxurious warm blush palette as the signature look
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WarmCreamBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
