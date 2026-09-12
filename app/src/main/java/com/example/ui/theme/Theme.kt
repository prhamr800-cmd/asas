package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrivoVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = PrivoPurpleDark,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = PrivoIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = PrivoCyanAccent,
    onTertiary = Color.Black,
    background = PrivoDarkBg,
    onBackground = PrivoDarkTextPrimary,
    surface = PrivoDarkSurface,
    onSurface = PrivoDarkTextPrimary,
    surfaceVariant = PrivoDarkCard,
    onSurfaceVariant = PrivoDarkTextSecondary,
    outline = PrivoDarkBorder,
    error = PrivoRoseAccent,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrivoVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDD6FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = PrivoIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7D2FE),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = PrivoCyanAccent,
    onTertiary = Color.White,
    background = PrivoLightBg,
    onBackground = PrivoLightTextPrimary,
    surface = PrivoLightSurface,
    onSurface = PrivoLightTextPrimary,
    surfaceVariant = PrivoLightCard,
    onSurfaceVariant = PrivoLightTextSecondary,
    outline = PrivoLightBorder,
    error = PrivoRoseAccent,
    onError = Color.White
)

@Composable
fun PrivoTheme(
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PrivoTheme(darkTheme = darkTheme, content = content)
}
