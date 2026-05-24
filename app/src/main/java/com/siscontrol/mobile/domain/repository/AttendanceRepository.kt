package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.Attendance
import com.siscontrol.mobile.domain.model.AttendanceParam

/**
 * Contrato de repositorio para el control de asistencia y turnos de guardia.
 * Define operaciones basadas en modelos de dominio puros.
 */
interface AttendanceRepository {
    suspend fun getAllShifts(): Result<List<Attendance>>
    suspend fun checkIn(request: AttendanceParam): Result<Attendance>
    suspend fun checkOut(request: AttendanceParam): Result<Attendance>
}

