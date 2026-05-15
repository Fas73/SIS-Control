package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import com.siscontrol.mobile.domain.repository.AttendanceRepository

class CheckInUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(request: AttendanceRequest): Result<AttendanceResponse> {
        return repository.checkIn(request)
    }
}

class CheckOutUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(userId: Long): Result<AttendanceResponse> {
        return repository.checkOut(userId)
    }
}
