package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = PureWhite,
    primaryContainer = PrimaryIndigoDark,
    onPrimaryContainer = PureWhite,
    secondary = VioletLight,
    onSecondary = RichBlack,
    secondaryContainer = Slate700,
    onSecondaryContainer = PureWhite,
    tertiary = EmeraldSuccess,
    background = RichBlack,
    onBackground = SoftWhite,
    surface = Slate800,
    onSurface = SoftWhite,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Slate700,
    error = CoralError,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = PureWhite,
    primaryContainer = Slate100,
    onPrimaryContainer = PrimaryIndigoDark,
    secondary = ElectricViolet,
    onSecondary = PureWhite,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate800,
    tertiary = EmeraldSuccess,
    background = SoftWhite,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = CoralError,
    onError = PureWhite
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
