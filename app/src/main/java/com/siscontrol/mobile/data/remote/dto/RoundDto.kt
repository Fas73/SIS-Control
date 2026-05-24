package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para la respuesta de una ronda (coincide con tu tabla round_executions).
 */
data class RoundResponseDto(
    val id: Long? = 0L,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null,
    val observations: String? = null,
    val workerId: Long? = null,
    val worker: UserResponseDto? = null,
    val installationId: Long? = null,
    val installation: InstallationDto? = null
)

/**
 * DTO para la petición de finalización de ronda.
 */
data class EndRoundRequest(
    val observations: String
)

/**
 * DTO para iniciar una ronda.
 */
data class StartRoundRequest(
    val userId: Long,
    val installationId: Long
)

/**
 * DTO para el escaneo de un checkpoint por NFC.
 */
data class ScanCheckpointRequest(
    val roundExecution: IdRequest,
    val checkpoint: IdRequest,
    val notes: String,
    val status: Int = 1, // 1: Físico, 2: Omitido
    val imageUrl: String? = null
)

data class IdRequest(val id: Long)

/**
 * DTO para la respuesta del escaneo de checkpoint.
 * Permite capturar scannedAt oficial generado por el backend.
 */
data class ScanCheckpointResponseDto(
    val mensaje: String? = null,
    val escaneo: ChecklogDto? = null
)

/**
 * DTO para la respuesta envoltorio del inicio de ronda.
 */
data class RoundStartResponseDto(
    val mensaje: String? = null,
    val ronda: RoundResponseDto? = null
)

/**
 * DTO para la respuesta envoltorio de finalización de ronda.
 */
data class RoundEndResponseDto(
    val mensaje: String? = null,
    val ronda: RoundResponseDto? = null
)

/**
 * DTO para la respuesta del estado actual del guardia (Sincronizado con RoundService.java)
 */
data class CurrentStateResponseDto(
    val jornadaActiva: Boolean? = false,
    val rondaActiva: Boolean? = false,
    val jornada: ShiftDto? = null,
    val ronda: RoundResponseDto? = null,
    val escaneosCompletados: List<ChecklogDto>? = emptyList()
)

data class ShiftDto(
    val id: Long? = 0L,
    val entryTime: String? = null,
    val exitTime: String? = null,
    val status: String? = null,
    val installation: InstallationDto? = null,
    val worker: UserResponseDto? = null
)

data class ChecklogDto(
    val id: Long? = 0L,
    val scannedAt: String? = null,
    val checkpoint: CheckpointDto? = null,
    val notes: String? = null
)

/**
 * DTO para el detalle completo de una ronda.
 */
data class RoundDetailResponseDto(
    val ronda: RoundResponseDto? = null,
    val escaneos: List<ChecklogDto>? = emptyList(),
    val incidentes: List<Map<String, Any>>? = emptyList()
)
