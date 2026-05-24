package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object que representa un Usuario en el sistema móvil.
 * Sincronizado con UserResponseDTO.java del Backend.
 */
data class UserResponseDto(
    val id: Long? = 0L,
    val rut: String? = null,
    val username: String? = "",
    val email: String? = "",
    val fullName: String? = "Usuario",
    val role: String? = "GUARD",
    val phoneNumber: String? = null,
    @SerializedName("profileImageUrl") val imageUrl: String? = null,
    val status: Int? = 1,
    val createdAt: String? = null
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
