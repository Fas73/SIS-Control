package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Servicio Retrofit para la gestión de usuarios (Personal).
 */
interface PersonnelApiService {

    @GET("/api/users")
    suspend fun getPersonnel(): List<UserResponseDto>

    @POST("/api/users")
    suspend fun createPersonnel(@Body request: UserRequestDto): UserResponseDto
}
