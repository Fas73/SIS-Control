package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para actualizar los datos básicos del propio perfil.
 * Correspondiente a: PUT /api/perfil/{id}/datos
 */
data class ProfileUpdateRequest(
    val fullName: String,
    val username: String,
    val phoneNumber: String,
    val imageUrl: String? = null
)

/**
 * DTO para cambiar la propia contraseña.
 * Correspondiente a: PUT /api/perfil/{id}/contrasena
 */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
