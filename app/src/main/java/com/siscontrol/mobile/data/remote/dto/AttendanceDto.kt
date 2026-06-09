package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

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

/**
 * DTO consolidado para el Reporte de Jornada completo.
 * Proporcionado por el endpoint GET /api/asistencia/reporte-jornada/{shiftId}
 */
data class ShiftReportDto(
    @SerializedName("shiftId") val shiftId: Long,
    @SerializedName("workerName") val workerName: String,
    @SerializedName("installationName") val installationName: String,
    @SerializedName("entryTime") val entryTime: String,
    @SerializedName("exitTime") val exitTime: String?,
    @SerializedName("totalRoundsPlanned") val totalRoundsPlanned: Int,
    @SerializedName("totalRoundsExecuted") val totalRoundsExecuted: Int,
    @SerializedName("rondas") val rondas: List<RoundDetailDto>,
    @SerializedName("incidentes") val incidentes: List<IncidentDetailDto>,
    @SerializedName("metrics") val metrics: ShiftMetricsDto
)

data class RoundDetailDto(
    @SerializedName("roundId") val roundId: Long,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("status") val status: String,
    @SerializedName("checklogs") val checklogs: List<ChecklogDetailDto>
)

data class ChecklogDetailDto(
    @SerializedName("checkpointId") val checkpointId: Long,
    @SerializedName("checkpointName") val checkpointName: String,
    @SerializedName("executionOrder") val executionOrder: Int,
    @SerializedName("scannedAt") val scannedAt: String,
    @SerializedName("status") val status: Int, // 1: Físico NFC, 2: Omitido
    @SerializedName("imageUrl") val imageUrl: String?
)

data class IncidentDetailDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("status") val status: Int
)

data class ShiftMetricsDto(
    @SerializedName("totalCheckpoints") val totalCheckpoints: Int,
    @SerializedName("scannedCheckpoints") val scannedCheckpoints: Int,
    @SerializedName("omittedCheckpoints") val omittedCheckpoints: Int,
    @SerializedName("alertsCount") val alertsCount: Int
)
