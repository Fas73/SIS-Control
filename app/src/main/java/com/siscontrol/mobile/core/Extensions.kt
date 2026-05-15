package com.siscontrol.mobile.core

import java.util.Locale

/**
 * Convierte un String a Title Case (Primera letra de cada palabra en mayúscula).
 * Ej: "JUAN PEREZ" -> "Juan Perez"
 */
fun String.toTitleCase(): String {
    if (this.isBlank()) return this
    return this.lowercase()
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}
