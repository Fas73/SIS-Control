package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.SupervisorGuardApiService
import retrofit2.HttpException
import java.io.IOException

class UnassignGuardUseCase(
    private val api: SupervisorGuardApiService
) {
    suspend operator fun invoke(supervisorId: Long, guardId: Long): Result<Boolean> {
        return try {
            val response = api.eliminarAsignacion(supervisorId, guardId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al desasignar guardia: ${response.message()}"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de red, revisa tu conexión."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error desconocido"))
        }
    }
}
