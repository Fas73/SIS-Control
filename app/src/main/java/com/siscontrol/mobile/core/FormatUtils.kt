package com.siscontrol.mobile.core

import java.text.SimpleDateFormat
import java.util.*

/**
 * Convierte una fecha en formato ISO8601 o YYYY-MM-DD a un formato legible con hora.
 * Resultado esperado: "20-05-2026 21:57 hrs"
 */
fun String?.formatDateToDisplay(): String {
    if (this.isNullOrBlank()) return "N/A"
    return try {
        // Limpiamos microsegundos y zonas horarias si existen
        val cleanDate = this.substringBefore(".")
            .replace("Z", "")
            .trim()
        
        val date: Date? = when {
            cleanDate.contains("T") && cleanDate.length > 10 -> {
                // Formato ISO: 2026-05-20T21:57:00
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(cleanDate)
            }
            cleanDate.length == 10 -> {
                // Formato solo fecha: 2026-05-20
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cleanDate)
            }
            else -> {
                // Intento genérico si nada coincide
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cleanDate.take(10))
            }
        }
        
        if (date != null) {
            SimpleDateFormat("dd-MM-yyyy HH:mm' hrs'", Locale.getDefault()).format(date)
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}

/**
 * Limpia el prefijo +56 y devuelve solo los 9 dígitos.
 */
fun String?.toPhoneDigits(): String {
    if (this == null) return ""
    return this.replace("+56", "").filter { it.isDigit() }.takeLast(9)
}
