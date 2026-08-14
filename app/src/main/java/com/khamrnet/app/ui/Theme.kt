package com.khamrnet.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KhamrColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    secondary = Color(0xFFD99A2B),
    onSecondary = Color.White,
    tertiary = Color(0xFF102A43),
    background = Color(0xFFFFF9F0),
    surface = Color.White,
    error = Color(0xFFB3261E)
)

@Composable
fun KhamrTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KhamrColors, content = content)
}