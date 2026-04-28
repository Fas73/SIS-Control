package com.siscontrol.mobile.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de colores claro — basado en la paleta de marca SIS Control (azul seguridad).
 */
private val LightColorScheme = lightColorScheme(
    primary          = PrimaryColor,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryVariant,
    secondary        = SecondaryColor,
    background       = BackgroundColor,
    surface          = SurfaceColor,
    error            = DangerColor
)

private val DarkColorScheme = darkColorScheme(
    primary          = PrimaryVariant,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryColor,
    secondary        = SecondaryColor,
    background       = androidx.compose.ui.graphics.Color(0xFF111827),
    surface          = androidx.compose.ui.graphics.Color(0xFF1F2937),
    error            = DangerColor
)

@Composable
fun SISControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
