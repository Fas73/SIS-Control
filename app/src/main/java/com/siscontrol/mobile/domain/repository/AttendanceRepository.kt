package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import com.siscontrol.mobile.data.remote.dto.ShiftReportDto

interface AttendanceRepository {
    suspend fun getAllShifts(): Result<List<AttendanceResponse>>
    suspend fun checkIn(request: AttendanceRequest): Result<AttendanceResponse>
    suspend fun checkOut(request: AttendanceRequest): Result<AttendanceResponse>
    suspend fun getShiftReport(shiftId: Long): Result<ShiftReportDto>
}
