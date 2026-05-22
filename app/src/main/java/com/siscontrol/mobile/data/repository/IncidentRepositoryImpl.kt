package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.IncidentApiService
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.repository.IncidentRepository

class IncidentRepositoryImpl(
    private val apiService: IncidentApiService
) : IncidentRepository {

    override suspend fun saveIncident(incident: IncidentDto): Result<IncidentDto> {
        return try {
            val response = apiService.createIncident(incident)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al guardar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllIncidents(): Result<List<IncidentDto>> {
        return try {
            val response = apiService.getAllIncidents()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerPanic(roundId: Long, descripcion: String?): Result<Unit> {
        return try {
            android.util.Log.d("PANIC_REPO", "Enviando pánico - RoundExecutionId: $roundId")
            val response = apiService.triggerPanic(roundId, descripcion)
            if (response.isSuccessful) {
                android.util.Log.d("PANIC_REPO", "✅ Pánico enviado exitosamente")
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string() ?: "Error desconocido"
                android.util.Log.e("PANIC_REPO", "❌ Falla en pánico: $error")
                Result.failure(Exception("Error ${response.code()}: $error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("PANIC_REPO", "❌ Crash al enviar pánico", e)
            Result.failure(e)
        }
    }
}
