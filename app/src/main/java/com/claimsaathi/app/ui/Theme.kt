package com.claimsaathi.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Colors = lightColorScheme(
    primary = Primary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Navy,
    onSecondary = Navy,
    background = AppBackground,
    onBackground = Ink,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Ink,
    error = Danger
)

@Composable
fun ClaimSaathiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
