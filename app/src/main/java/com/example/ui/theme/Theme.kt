package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VsCodeBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF094771),
    onPrimaryContainer = Color(0xFFCCE8FF),
    secondary = VsCodeCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E3A3A),
    onSecondaryContainer = Color(0xFF88F2DE),
    tertiary = VsCodeFunction,
    onTertiary = Color.Black,
    background = VsCodeBackground,
    onBackground = VsCodeTextPrimary,
    surface = VsCodeSurface,
    onSurface = VsCodeTextPrimary,
    surfaceVariant = VsCodeSurfaceVariant,
    onSurfaceVariant = VsCodeTextSecondary,
    outline = VsCodeBorder,
    outlineVariant = Color(0xFF454545),
    error = VsCodeError,
    onError = Color.White
)

@Composable
fun CodeStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
