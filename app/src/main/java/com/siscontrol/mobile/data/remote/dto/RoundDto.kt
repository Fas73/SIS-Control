package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para la respuesta de una ronda (coincide con tu tabla round_executions).
 */
data class RoundResponseDto(
    val id: Long,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null,
    val observations: String? = null,
    val workerId: Long? = null,
    val installationId: Long? = null
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
    val notes: String
)

data class IdRequest(val id: Long)

/**
 * DTO para la respuesta envoltorio del inicio de ronda.
 */
data class RoundStartResponseDto(
    val mensaje: String,
    val ronda: RoundResponseDto
)

/**
 * DTO para la respuesta envoltorio del escaneo.
 */
data class ScanResponseWrapperDto(
    val mensaje: String,
    val escaneo: Map<String, Any>
)
