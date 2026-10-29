package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = CyberBlack,
    primaryContainer = CyberCardElevated,
    onPrimaryContainer = NeonMintGlow,
    secondary = NeonCyan,
    onSecondary = CyberBlack,
    secondaryContainer = CyberCardBg,
    onSecondaryContainer = TextWhite,
    tertiary = StatusEmerald,
    background = CyberBlack,
    onBackground = TextWhite,
    surface = CyberCardBg,
    onSurface = TextWhite,
    surfaceVariant = CyberCardElevated,
    onSurfaceVariant = TextMuted,
    outline = CyberBorder,
    outlineVariant = CyberBorderFocused,
    error = StatusCoral,
    onError = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = NeonMintDark,
    onPrimary = TextWhite,
    primaryContainer = LightBorder,
    onPrimaryContainer = NeonMintDark,
    secondary = NeonCyan,
    onSecondary = TextWhite,
    secondaryContainer = LightBorder,
    onSecondaryContainer = LightText,
    tertiary = StatusEmerald,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightBorder,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = StatusCoral,
    onError = TextWhite
)

@Composable
fun RMAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
