package com.smartlink.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SmartLinkColors = lightColorScheme(
    primary = Color(0xFF0B7285),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9F0F1),
    onPrimaryContainer = Color(0xFF063D46),
    secondary = Color(0xFFE07A5F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF3A0A03),
    tertiary = Color(0xFF6C63A8),
    tertiaryContainer = Color(0xFFE8DEFF),
    background = Color(0xFFF8FAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7F0F0),
    onSurface = Color(0xFF17252B),
    onSurfaceVariant = Color(0xFF536568),
    outline = Color(0xFFB7C8C9)
)

private val SmartLinkTypography = Typography(
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 25.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 22.sp
    )
)

@Composable
fun SmartLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SmartLinkColors,
        typography = SmartLinkTypography,
        content = content
    )
}