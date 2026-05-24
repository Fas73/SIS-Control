package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.AuthApiService
import com.siscontrol.mobile.data.remote.dto.LoginRequest
import com.siscontrol.mobile.domain.repository.AuthRepository
import com.siscontrol.mobile.domain.usecase.LoginResult
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResult> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // IMPORTANTE: Tu Backend actual devuelve 'success' y 'message'.
                // Solo avanzamos si 'success' es true.
                if (body.success) {
                    android.util.Log.d("SIS_CONTROL_LOGIN", "Login exitoso. Body completo: $body")
                    
                    // Verificamos si el ID es nulo o 0 para loguear una advertencia
                    val receivedId = body.id ?: 0L
                    if (receivedId == 0L) {
                        android.util.Log.w("SIS_CONTROL_LOGIN", "ADVERTENCIA: El backend no envió un 'id' válido o envió 0.")
                    }

                    val result = LoginResult(
                        token    = body.token ?: "",
                        role     = body.role ?: "USER",
                        fullName = body.fullName ?: body.username ?: "Usuario",
                        username = body.username ?: "",
                        userId   = receivedId
                    )

                    android.util.Log.d("SIS_CONTROL_LOGIN", "Guardando en Room userId: ${result.userId}")

                    // Guardamos en Room
                    com.siscontrol.mobile.di.AppModule.getDatabase().userSessionDao().insertSession(
                        com.siscontrol.mobile.data.local.entities.UserSessionEntity(
                            id = result.userId,
                            username = result.username,
                            fullName = result.fullName,
                            role = result.role,
                            status = body.status ?: "1"
                        )
                    )

                    Result.success(result)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                Result.failure(Exception("Error de servidor. Código HTTP: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: revisa que tu backend esté corriendo y la IP sea correcta."))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }
}