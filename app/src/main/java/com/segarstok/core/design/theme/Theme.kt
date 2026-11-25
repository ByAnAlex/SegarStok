package com.segarstok.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = PrimaryTealContainer,
    onPrimaryContainer = PrimaryTealDark,
    secondary = PrimaryTeal,
    onSecondary = Color.White,
    secondaryContainer = PrimaryTealLight,
    background = CanvasBackground,
    onBackground = Slate900,
    surface = CardSurface,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate500,
    outline = CardBorder,
    outlineVariant = DividerColor
)

@Composable
fun SegarStokTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
