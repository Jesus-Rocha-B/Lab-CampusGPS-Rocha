package com.rocha.campusgps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CampusBlueLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = CampusBlueDark,
    onPrimaryContainer = CampusBlueContainer,
    secondary = CampusTeal
)

private val LightColorScheme = lightColorScheme(
    primary = CampusBlue,
    onPrimary = Color.White,
    primaryContainer = CampusBlueContainer,
    onPrimaryContainer = CampusBlueDark,
    secondary = CampusTeal,
    onSecondary = Color.White,
    errorContainer = CampusErrorBg,
    onErrorContainer = CampusErrorText
)

@Composable
fun CampusGPSTheme(
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
