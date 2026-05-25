package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para actualizar los datos básicos del propio perfil.
 * Correspondiente a: PUT /api/perfil/{id}/datos
 */
data class ProfileUpdateRequest(
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    
    @SerializedName("profileImageUrl")
    val imageUrl: String? = null
)

/**
 * DTO para cambiar la propia contraseña.
 * Correspondiente a: PUT /api/perfil/{id}/contrasena
 */
data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    
    @SerializedName("newPassword")
    val newPassword: String
)
