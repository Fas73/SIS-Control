package com.siscontrol.mobile.core

import java.text.SimpleDateFormat
import java.util.*

/**
 * Convierte una fecha en formato YYYY-MM-DD o ISO8601 a DD-MM-YYYY.
 */
fun String?.formatDateToDisplay(): String {
    if (this.isNullOrBlank()) return "N/A"
    return try {
        val inputFormat = if (this.contains("T")) {
            // El formato ISO suele incluir milisegundos o zona horaria,
            // pero para parsear la mayoría de los casos basta con:
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        }

        // CAMBIO AQUÍ: Agregamos HH:mm para la hora y los minutos
        val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        val date = inputFormat.parse(this)
        if (date != null) outputFormat.format(date) else this
    } catch (e: Exception) {
        this // Si falla el parseo, devolvemos la original
    }
}
/**
 * Limpia el prefijo +56 y devuelve solo los 9 dígitos.
 */
fun String?.toPhoneDigits(): String {
    if (this == null) return ""
    return this.replace("+56", "").filter { it.isDigit() }.takeLast(9)
}
