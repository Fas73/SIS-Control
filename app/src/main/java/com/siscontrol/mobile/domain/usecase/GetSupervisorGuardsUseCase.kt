package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.SupervisorGuardApiService
import com.siscontrol.mobile.data.remote.dto.SupervisorGuardResponseDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import retrofit2.HttpException
import java.io.IOException

class GetSupervisorGuardsUseCase(
    private val api: SupervisorGuardApiService
) {
    suspend operator fun invoke(supervisorId: Long): Result<List<UserResponseDto>> {
        return try {
            val response = api.obtenerGuardiasDeSupervisor(supervisorId)
            // Extraemos solo el guardia de cada asignación
            val guards = response.map { it.guard }
            Result.success(guards)
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.code()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de red, revisa tu conexión."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error desconocido"))
        }
    }
}
