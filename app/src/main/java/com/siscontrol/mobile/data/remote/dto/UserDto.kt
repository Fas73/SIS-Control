package com.siscontrol.mobile.data.remote.dto

/**
 * Data Transfer Object que representa un Usuario en el sistema móvil.
 */
data class UserResponseDto(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val status: String,
    val installations: List<InstallationDto>? = emptyList()
)

data class UserRequestDto(
    val username: String,
    val email: String,
    val fullName: String,
    val password: String,
    val role: String,
    val installationIds: List<Long>? = emptyList()
)
