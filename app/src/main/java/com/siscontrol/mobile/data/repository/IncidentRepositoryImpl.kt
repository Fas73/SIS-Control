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
                android.util.Log.d("INCIDENT_REPO", "✅ ÉXITO: Registro guardado en MySQL")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin detalle"
                android.util.Log.e("INCIDENT_REPO", "❌ ERROR 400/500: $errorBody")
                Result.failure(Exception("Servidor rechazó datos: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("INCIDENT_REPO", "❌ CRASH CONEXIÓN: ${e.message}")
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

    override suspend fun triggerPanic(roundId: Long?, shiftId: Long?, descripcion: String?): Result<Unit> {
        return try {
            android.util.Log.d("PANIC_REPO", "Enviando pánico - RoundExecutionId: $roundId, ShiftId: $shiftId")
            val response = apiService.triggerPanic(roundId, shiftId, descripcion)
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
