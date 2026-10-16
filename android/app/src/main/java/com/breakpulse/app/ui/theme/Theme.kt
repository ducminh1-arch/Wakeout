package com.breakpulse.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WakeoutColorScheme = lightColorScheme(
    primary = WakeoutGreen,
    secondary = WakeoutOrange,
    tertiary = WakeoutBlue,
    background = BgPage,
    surface = BgCard,
    onPrimary = BgCard,
    onBackground = TextMain,
    onSurface = TextMain
)

@Composable
fun BreakPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WakeoutColorScheme,
        content = content
    )
}
