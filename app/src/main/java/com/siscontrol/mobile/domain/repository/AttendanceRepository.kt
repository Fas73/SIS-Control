package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse

interface AttendanceRepository {
    suspend fun checkIn(request: AttendanceRequest): Result<AttendanceResponse>
    suspend fun checkOut(userId: Long): Result<AttendanceResponse>
}
