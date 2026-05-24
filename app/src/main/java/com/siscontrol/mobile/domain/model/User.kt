package com.siscontrol.mobile.domain.model

data class User(
    val id: Long,
    val rut: String,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val phoneNumber: String?,
    val imageUrl: String?,
    val status: Int,
    val createdAt: String?
)

/**
 * Parámetros de dominio para la creación y edición de usuarios.
 * Evita el uso directo de UserRequestDto en las capas internas.
 */
data class UserCreationParam(
    val rut: String,
    val username: String,
    val email: String,
    val fullName: String,
    val password: String,
    val phoneNumber: String,
    val role: String
)

/**
 * Parámetros de dominio para actualizar los datos de perfil de un usuario.
 */
data class ProfileUpdateParam(
    val fullName: String,
    val username: String,
    val phoneNumber: String
)

/**
 * Parámetros de dominio para cambiar la contraseña del perfil del usuario.
 */
data class ChangePasswordParam(
    val currentPassword: String,
    val newPassword: String
)


