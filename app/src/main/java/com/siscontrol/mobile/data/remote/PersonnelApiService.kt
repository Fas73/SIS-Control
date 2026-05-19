package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio Retrofit para la gestión de usuarios (Personal).
 * Sincronizado con UserController.java del Backend.
 */
interface PersonnelApiService {

    @GET("api/usuarios")
    suspend fun getPersonnel(): List<UserResponseDto>

    @GET("api/usuarios/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserResponseDto

    @POST("api/usuarios")
    suspend fun createPersonnel(
        @Query("creatorId") creatorId: Long,
        @Body request: UserRequestDto
    ): UserResponseDto

    @PUT("api/usuarios/{id}")
    suspend fun updatePersonnel(
        @Path("id") id: Long,
        @Query("editorId") editorId: Long,
        @Body request: UserRequestDto
    ): UserResponseDto

    @PATCH("api/usuarios/{id}/toggle-status")
    suspend fun toggleUserStatus(
        @Path("id") id: Long,
        @Query("editorId") editorId: Long
    ): Response<Map<String, Any>>
}
