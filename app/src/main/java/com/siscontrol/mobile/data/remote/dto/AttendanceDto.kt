package com.siscontrol.mobile.data.remote.dto

data class AttendanceRequest(
    val userId: Long,
    val installationId: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * DTO para la respuesta de asistencia, sincronizado con la entidad Shift.java
 */
data class AttendanceResponse(
    val id: Long? = null,
    val entryTime: String? = null,
    val exitTime: String? = null,
    val status: String? = null,
    val installation: InstallationDto? = null,
    val worker: UserResponseDto? = null
)

/**
 * DTO para la respuesta envoltorio del backend de asistencia.
 */
data class AttendanceWrapperResponse(
    val mensaje: String? = null,
    val jornada: AttendanceResponse? = null
)
