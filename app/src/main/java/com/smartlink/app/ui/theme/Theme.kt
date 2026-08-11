package com.smartlink.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartLinkColors = lightColorScheme(
    primary = Color(0xFF176B87),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EDF5),
    onPrimaryContainer = Color(0xFF063746),
    secondary = Color(0xFFB36A35),
    secondaryContainer = Color(0xFFFFDEC7),
    background = Color(0xFFF5F7F8),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EFF1),
    onSurface = Color(0xFF17252B),
    onSurfaceVariant = Color(0xFF59666B),
    outline = Color(0xFFD5DFE2)
)

@Composable
fun SmartLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SmartLinkColors,
        typography = Typography(),
        content = content
    )
}