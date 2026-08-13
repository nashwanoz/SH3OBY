package com.khamrnet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColors(
    primary = Color(0xFF2E7D32),
    primaryVariant = Color(0xFF1B5E20),
    secondary = Color(0xFFFFC107),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColors = darkColors(
    primary = Color(0xFF66BB6A),
    primaryVariant = Color(0xFF388E3C),
    secondary = Color(0xFFFFC107),
)

@Composable
fun KhamrNetTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colors = colors, typography = Typography(), shapes = Shapes(), content = content)
}
