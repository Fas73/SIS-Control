package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object que representa un Usuario en el sistema móvil.
 * Sincronizado con UserResponseDTO.java del Backend.
 */
data class UserResponseDto(
    @SerializedName("id")
    val id: Long? = 0L,
    
    @SerializedName("rut")
    val rut: String? = null,
    
    @SerializedName("username")
    val username: String? = "",
    
    @SerializedName("email")
    val email: String? = "",
    
    @SerializedName("fullName")
    val fullName: String? = "Usuario",
    
    @SerializedName("role")
    val role: String? = "GUARD",
    
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    
    @SerializedName("profileImageUrl")
    val imageUrl: String? = null,

    @SerializedName("status")
    val status: Int? = 1,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * DTO para la creación de un nuevo usuario.
 */
data class UserRequestDto(
    @SerializedName("rut")
    val rut: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    
    @SerializedName("role")
    val role: String
)
