package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.Attendance
import com.siscontrol.mobile.domain.model.AttendanceParam
import com.siscontrol.mobile.domain.repository.AttendanceRepository

class GetAllShiftsUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(): Result<List<Attendance>> = repository.getAllShifts()
}

class CheckInUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(request: AttendanceParam): Result<Attendance> {
        return repository.checkIn(request)
    }
}

class CheckOutUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(request: AttendanceParam): Result<Attendance> {
        return repository.checkOut(request)
    }
}

