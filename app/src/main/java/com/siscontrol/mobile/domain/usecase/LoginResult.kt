package com.siscontrol.mobile.domain.usecase

/**
 * Modelo simple del Dominio que encapsula el resultado exitoso de un login.
 */
data class LoginResult(
    val token: String,
    val role: String,
    val fullName: String,
    val username: String,
    val userId: Long
)
