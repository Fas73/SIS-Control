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
}