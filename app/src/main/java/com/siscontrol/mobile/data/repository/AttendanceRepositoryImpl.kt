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
            if (response.isSuccessful && response.body() != null) {
                // Extraemos el objeto 'jornada' del envoltorio verídico del backend
                Result.success(response.body()!!.jornada)
            } else {
                Result.failure(Exception("Error en check-in: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOut(request: AttendanceRequest): Result<AttendanceResponse> {
        return try {
            val response = api.checkOut(request)
            if (response.isSuccessful && response.body() != null) {
                // Extraemos el objeto 'jornada' del envoltorio verídico del backend
                Result.success(response.body()!!.jornada)
            } else {
                Result.failure(Exception("Error en check-out: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
