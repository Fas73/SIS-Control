package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.LoginRequest
import com.siscontrol.mobile.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz Retrofit encargada de definir la firma de red para temas de autenticación.
 */
interface AuthApiService {

    /**
     * Endpoint responsable del inicio de sesión.
     * Retorna una [Response] encapsulando la respuesta, para manejar códigos HTTP manualmente si se requiere.
     */
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
