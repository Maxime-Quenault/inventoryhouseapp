package com.example.inventoryhouse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkLeaf,
    onPrimary = Color(0xFF07351C),
    primaryContainer = DarkMint,
    onPrimaryContainer = DarkInk,
    secondary = DarkBlue,
    onSecondary = Color(0xFF092F63),
    tertiary = DarkAmber,
    onTertiary = Color(0xFF3E2600),
    background = Color(0xFF0B120F),
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = Color(0xFF1B2A21),
    onSurfaceVariant = Color(0xFFB7C9BA),
    outline = Color(0xFF738575),
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = FreshMint,
    onPrimaryContainer = Color(0xFF08351C),
    secondary = OceanBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8FF),
    onSecondaryContainer = Color(0xFF0B2E60),
    tertiary = WarmAmber,
    onTertiary = Color(0xFF3D2A00),
    tertiaryContainer = Color(0xFFFFE6AC),
    onTertiaryContainer = Color(0xFF2B1B00),
    background = Mist,
    onBackground = Ink,
    surface = Porcelain,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9EFE7),
    onSurfaceVariant = Slate,
    outline = Color(0xFF8A998D),
    outlineVariant = Color(0xFFD6E0D8),
    error = TomatoRed
)

@Composable
fun InventoryHouseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
