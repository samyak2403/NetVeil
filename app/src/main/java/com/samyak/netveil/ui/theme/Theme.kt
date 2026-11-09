package com.samyak.netveil.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VeilCoralPink,
    onPrimary = Color(0xFF111215),
    primaryContainer = Color(0xFF381F23),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = VeilPeach,
    onSecondary = Color(0xFF111215),
    secondaryContainer = Color(0xFF2C1D21),
    onSecondaryContainer = Color(0xFFFFDCD7),
    tertiary = VeilRose,
    background = VeilDarkBg,
    onBackground = Color.White,
    surface = VeilDarkBg,
    onSurface = Color.White,
    surfaceContainer = Color(0xFF17181E),
    surfaceContainerHigh = Color(0xFF20222A),
    surfaceContainerHighest = Color(0xFF282A34),
    onSurfaceVariant = VeilTextSubtle,
    outline = Color(0xFF383A48),
    outlineVariant = Color(0xFF242630)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE26557),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE9),
    onPrimaryContainer = Color(0xFF410E0A),
    secondary = Color(0xFF775651),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E4E2),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = Color(0xFF8F4C59),
    background = Color(0xFFF8F5F5),
    onBackground = Color(0xFF1D1B1B),
    surface = Color(0xFFF8F5F5),
    onSurface = Color(0xFF1D1B1B),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEFE8E7),
    surfaceContainerHighest = Color(0xFFE3DCDB),
    onSurfaceVariant = Color(0xFF6B5856),
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF)
)

@Composable
fun NetVeilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
