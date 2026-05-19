package com.siscontrol.mobile.data.remote.dto

data class AttendanceRequest(
    val userId: Long,
    val installationId: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class AttendanceResponse(
    val id: Long? = null,
    val userId: Long? = null,
    val installationId: Long? = null,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val status: String? = null
)

/**
 * DTO para la respuesta envoltorio del backend de asistencia.
 */
data class AttendanceWrapperResponse(
    val mensaje: String,
    val jornada: AttendanceResponse
)
