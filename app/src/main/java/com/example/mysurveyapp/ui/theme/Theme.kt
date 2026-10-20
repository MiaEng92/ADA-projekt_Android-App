package com.example.mysurveyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AdaColors = lightColorScheme(
    primary = AdaBlue,
    onPrimary = Color.White,
    primaryContainer = AdaBlueLight,
    onPrimaryContainer = AdaBlueDark,
    secondary = AdaSlate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6F2),
    onSecondaryContainer = Color(0xFF14283D),
    tertiary = AdaTeal,
    background = AdaBackground,
    onBackground = AdaText,
    surface = Color.White,
    onSurface = AdaText,
    surfaceVariant = Color(0xFFE3E9F1),
    onSurfaceVariant = Color(0xFF45546A),
    surfaceContainerHighest = AdaCard,
    outline = AdaOutline,
    outlineVariant = Color(0xFFC9D3E0),
    error = AdaError
)

@Composable
fun MySurveyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdaColors,
        typography = Typography,
        content = content
    )
}