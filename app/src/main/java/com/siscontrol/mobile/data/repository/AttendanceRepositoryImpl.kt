package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.AttendanceApiService
import com.siscontrol.mobile.data.remote.dto.*
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
                val jornada = body.jornada
                if (jornada != null) {
                    Result.success(jornada)
                } else {
                    Result.failure(Exception("El servidor no devolvió los datos de la jornada."))
                }
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
                val jornada = body.jornada
                if (jornada != null) {
                    Result.success(jornada)
                } else {
                    Result.failure(Exception("Error al procesar salida."))
                }
            } else {
                Result.failure(Exception("Error en check-out: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getShiftReport(shiftId: Long): Result<ShiftReportDto> {
        return try {
            val response = api.getShiftReport(shiftId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener reporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
