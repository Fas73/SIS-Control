package com.siscontrol.mobile.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema ÚNICO de alta visibilidad para SIS Control.
 * Se fuerza el fondo blanco y texto negro para evitar problemas con el modo oscuro del celular.
 */
private val HighContrastColorScheme = lightColorScheme(
    primary            = PrimaryColor,
    onPrimary          = Color.White,
    primaryContainer   = PrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary          = SecondaryColor,
    background         = BackgroundColor, // F3F4F6
    surface            = Color.White,
    onSurface          = TextPrimary,     // Negro Absoluto
    onBackground       = TextPrimary,
    error              = DangerColor,
    onError            = Color.White,
    outline            = Color.DarkGray
)

@Composable
fun SISControlTheme(
    darkTheme: Boolean = false, // IGNORAMOS EL MODO OSCURO DEL SISTEMA
    content: @Composable () -> Unit
) {
    // Forzamos el esquema de alto contraste siempre
    MaterialTheme(
        colorScheme = HighContrastColorScheme,
        content = content
    )
}
