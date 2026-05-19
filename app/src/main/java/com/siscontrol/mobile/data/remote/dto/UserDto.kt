package com.siscontrol.mobile.data.remote.dto

/**
 * Data Transfer Object que representa un Usuario en el sistema móvil.
 * Sincronizado con UserResponseDTO.java del Backend.
 */
data class UserResponseDto(
    val id: Long,
    val rut: String? = null,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val phoneNumber: String? = null,
    val status: Int, // 1: Activo, 0: Inactivo
    val createdAt: String?
)

/**
 * DTO para la creación de un nuevo usuario.
 * Sincronizado con CreateUserRequestDTO del Backend.
 */
data class UserRequestDto(
    val rut: String,
    val username: String,
    val email: String,
    val fullName: String,
    val password: String,
    val phoneNumber: String,
    val role: String // ADMIN, SUPERVISOR, GUARD
)
