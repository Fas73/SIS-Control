package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.AttendanceApiService
import com.siscontrol.mobile.domain.model.Attendance
import com.siscontrol.mobile.domain.model.AttendanceParam
import com.siscontrol.mobile.domain.repository.AttendanceRepository
import com.siscontrol.mobile.data.mapper.toDomain
import com.siscontrol.mobile.data.mapper.toDto

class AttendanceRepositoryImpl(
    private val api: AttendanceApiService
) : AttendanceRepository {

    override suspend fun getAllShifts(): Result<List<Attendance>> {
        return try {
            val response = api.getAllShifts()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkIn(request: AttendanceParam): Result<Attendance> {
        return try {
            val response = api.checkIn(request.toDto())
            val body = response.body()
            if (response.isSuccessful && body != null) {
                body.jornada?.let { Result.success(it.toDomain()) } ?: Result.failure(Exception("El servidor no devolvió los datos de la jornada."))
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

    override suspend fun checkOut(request: AttendanceParam): Result<Attendance> {
        return try {
            val response = api.checkOut(request.toDto())
            val body = response.body()
            if (response.isSuccessful && body != null) {
                body.jornada?.let { Result.success(it.toDomain()) } ?: Result.failure(Exception("Error al procesar salida."))
            } else {
                Result.failure(Exception("Error en check-out: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
