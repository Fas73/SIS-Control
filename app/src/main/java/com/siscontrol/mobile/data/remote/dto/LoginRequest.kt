package com.siscontrol.mobile.data.remote.dto

/**
 * Data class que representa la solicitud de autenticación al servidor.
 * Se enviará como JSON en el cuerpo de la petición.
 */
data class LoginRequest(
    val username: String,
    val password: String
)
