package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.AuthApiService
import com.siscontrol.mobile.data.remote.dto.LoginRequest
import com.siscontrol.mobile.domain.repository.AuthRepository
import com.siscontrol.mobile.domain.usecase.LoginResult
import java.io.IOException

/**
 * Implementación concreta del [AuthRepository] definido en la capa de Dominio.
 * Aquí manejamos el puente con Retrofit y los mapeos de la API.
 */
class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResult> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Mapeamos el DTO de red al modelo de Dominio
                Result.success(
                    LoginResult(
                        token    = body.token,
                        role     = body.role,
                        fullName = body.fullName,
                        username = body.username
                    )
                )
            } else {
                Result.failure(Exception("Credenciales inválidas o error de servidor. Código HTTP: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red, por favor revise su conexión a internet."))
        } catch (e: Exception) {
            Result.failure(Exception("Ha ocurrido un error inesperado al intentar iniciar sesión."))
        }
    }
}
