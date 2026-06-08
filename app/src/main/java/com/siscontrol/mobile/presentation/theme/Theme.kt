package com.siscontrol.mobile.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = PrimaryColor,
    onPrimary          = Color.White,
    primaryContainer   = PrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary          = SecondaryColor,
    background         = BackgroundColor,
    surface            = Color.White,
    onSurface          = TextPrimary,
    onBackground       = TextPrimary,
    error              = DangerColor,
    onError            = Color.White,
    outline            = Color.DarkGray
)

@Composable
fun SISControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
