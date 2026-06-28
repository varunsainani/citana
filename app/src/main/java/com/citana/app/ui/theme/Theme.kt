package com.citana.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    primaryContainer = Peach,
    onPrimaryContainer = PeachDeep,
    secondary = Amber,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = Peach,
    onSurfaceVariant = MutedLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = DangerRed,
)

private val DarkColors = darkColorScheme(
    primary = CoralBright,
    onPrimary = Color(0xFF2A0F08),
    primaryContainer = PeachDeep,
    onPrimaryContainer = Peach,
    secondary = Amber,
    onSecondary = Color(0xFF2A0F08),
    background = BgDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = Color(0xFF2A211C),
    onSurfaceVariant = MutedDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = Color(0xFFF0635D),
)

@Composable
fun CitanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
