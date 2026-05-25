package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfileApiService {

    @PUT("api/perfil/{id}/datos")
    suspend fun updateProfileData(
        @Path("id") id: Long,
        @Body request: ProfileUpdateRequest
    ): UserResponseDto

    @PUT("api/perfil/{id}/contrasena")
    suspend fun updatePassword(
        @Path("id") id: Long,
        @Body request: ChangePasswordRequest
    ): Map<String, Any>

    // Nuevo endpoint específico para la foto de perfil (Sincronizado con UserController.java del Backend)
    @retrofit2.http.PATCH("api/usuarios/{id}/profile-image")
    suspend fun updateProfileImage(
        @Path("id") id: Long,
        @retrofit2.http.Query("url") imageUrl: String
    ): UserResponseDto
}
