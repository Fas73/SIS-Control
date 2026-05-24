package com.siscontrol.mobile.domain.model

data class Attendance(
    val id: Long,
    val entryTime: String?,
    val exitTime: String?,
    val status: String?,
    val installation: Installation?,
    val worker: User?
)

/**
 * Parámetros de dominio para marcar asistencia (check-in / check-out).
 */
data class AttendanceParam(
    val userId: Long,
    val installationId: Long,
    val latitude: Double,
    val longitude: Double
)

