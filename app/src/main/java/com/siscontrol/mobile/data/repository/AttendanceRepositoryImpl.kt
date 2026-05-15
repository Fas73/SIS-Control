package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.AttendanceApiService
import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import com.siscontrol.mobile.domain.repository.AttendanceRepository

class AttendanceRepositoryImpl(
    private val api: AttendanceApiService
) : AttendanceRepository {

    override suspend fun checkIn(request: AttendanceRequest): Result<AttendanceResponse> {
        return try {
            val response = api.checkIn(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en check-in: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOut(userId: Long): Result<AttendanceResponse> {
        return try {
            val response = api.checkOut(mapOf("userId" to userId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en check-out: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
