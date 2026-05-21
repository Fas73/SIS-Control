package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.AttendanceApiService
import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import com.siscontrol.mobile.domain.repository.AttendanceRepository

class AttendanceRepositoryImpl(
    private val api: AttendanceApiService
) : AttendanceRepository {

    override suspend fun getAllShifts(): Result<List<AttendanceResponse>> {
        return try {
            val response = api.getAllShifts()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkIn(request: AttendanceRequest): Result<AttendanceResponse> {
        return try {
            val response = api.checkIn(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                // Extraemos la jornada del objeto envoltorio
                body.jornada?.let { 
                    Result.success(it) 
                } ?: Result.failure(Exception("El servidor no devolvió los datos de la jornada."))
            } else {
                val errorJson = response.errorBody()?.string() ?: ""
                val msg = if (errorJson.contains("\"message\":\"")) {
                    errorJson.substringAfter("\"message\":\"").substringBefore("\"")
                } else "Error ${response.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOut(request: AttendanceRequest): Result<AttendanceResponse> {
        return try {
            val response = api.checkOut(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                body.jornada?.let { 
                    Result.success(it) 
                } ?: Result.failure(Exception("Error al procesar salida."))
            } else {
                Result.failure(Exception("Error en check-out: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
