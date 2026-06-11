package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.LoginRequest
import com.siscontrol.mobile.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    /**
     * Endpoint para inicio de sesión.
     * La ruta debe coincidir exactamente con el @PostMapping("/api/auth/login") de tu Backend.
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/recuperar-acceso")
    suspend fun solicitarRecuperacion(
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    @POST("api/auth/check-username")
    suspend fun checkUsername(
        @Body request: Map<String, String>
    ): Response<Map<String, Boolean>>
}