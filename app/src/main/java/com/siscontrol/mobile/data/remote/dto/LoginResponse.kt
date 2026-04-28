package com.siscontrol.mobile.data.remote.dto

/**
 * Data class que mapea la respuesta de login del servidor, 
 * extrayendo tanto el JWT como la información básica del usuario.
 */
data class LoginResponse(
    val token: String,
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String
)
