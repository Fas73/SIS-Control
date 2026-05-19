package com.siscontrol.mobile.core

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utilidad para centralizar y mapear errores técnicos a mensajes amigables en español.
 */
object ErrorUtils {

    fun parse(error: Throwable?): String {
        if (error == null) return "Ocurrió un error inesperado."

        val message = error.message ?: ""

        // Manejo de códigos HTTP comunes
        return when {
            message.contains("400") -> "Datos inválidos. Verifique el RUT o formato."
            message.contains("401") -> "Sesión expirada. Inicie sesión nuevamente."
            message.contains("403") -> "Acceso denegado. Verifique si su usuario está ACTIVO."
            message.contains("404") -> "Instalación o Usuario no encontrado."
            message.contains("500") -> "Error interno del servidor. Reintente en unos minutos."
            
            // Excepciones de red
            error is UnknownHostException || error is ConnectException -> 
                "No hay conexión a internet o el servidor no está disponible."
            error is SocketTimeoutException -> 
                "El servidor está tardando mucho en responder. Inténtelo de nuevo."
            
            // Mensajes genéricos comunes
            message.contains("Unable to resolve host") -> 
                "No se pudo conectar con el servidor. Revise su conexión."
            
            // Errores de parseo JSON (Backend enviando formato inesperado)
            message.contains("BEGIN_ARRAY", ignoreCase = true) || 
            message.contains("BEGIN_OBJECT", ignoreCase = true) ||
            message.contains("Json", ignoreCase = true) ||
            message.contains("Malformed", ignoreCase = true) ||
            error is com.google.gson.JsonSyntaxException ->
                "No hay registros o el servidor entregó datos en un formato no válido."

            else -> "Ocurrió un problema: ${error.localizedMessage ?: "Inténtelo de nuevo"}"
        }
    }

    /**
     * Mapeo específico para el proceso de Login.
     */
    fun getLoginErrorMessage(error: Throwable): String {
        val msg = error.message ?: ""
        return if (msg.contains("401") || msg.contains("invalid", ignoreCase = true)) {
            "El usuario/correo o la contraseña (o ambos) son incorrectos."
        } else if (msg.contains("403")) {
            "Su cuenta está inactiva. Contacte al administrador."
        } else {
            parse(error)
        }
    }
}
