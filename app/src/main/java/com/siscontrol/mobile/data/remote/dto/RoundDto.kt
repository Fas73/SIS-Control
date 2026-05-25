package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta de una ronda (coincide con tu tabla round_executions).
 */
data class RoundResponseDto(
    @SerializedName("id")
    val id: Long? = 0L,
    @SerializedName("startTime")
    val startTime: String? = null,
    @SerializedName("endTime")
    val endTime: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("observations")
    val observations: String? = null,
    @SerializedName("workerId")
    val workerId: Long? = null,
    @SerializedName("worker")
    val worker: UserResponseDto? = null,
    @SerializedName("installationId")
    val installationId: Long? = null,
    @SerializedName("installation")
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
    @SerializedName("roundExecution")
    val roundExecution: IdRequest,
    @SerializedName("checkpoint")
    val checkpoint: IdRequest,
    @SerializedName("notes")
    val notes: String,
    @SerializedName("status")
    val status: Int = 1, // 1: Físico, 2: Omitido
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

data class IdRequest(val id: Long)

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
    @SerializedName("id")
    val id: Long? = 0L,
    @SerializedName("entryTime")
    val entryTime: String? = null,
    @SerializedName("exitTime")
    val exitTime: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("installation")
    val installation: InstallationDto? = null,
    @SerializedName("worker")
    val worker: UserResponseDto? = null
)

data class ChecklogDto(
    @SerializedName("id")
    val id: Long? = 0L,
    @SerializedName("scannedAt")
    val scannedAt: String? = null,
    @SerializedName("checkpoint")
    val checkpoint: CheckpointDto? = null,
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * DTO para el detalle completo de una ronda.
 */
data class RoundDetailResponseDto(
    @SerializedName("ronda") val ronda: RoundResponseDto? = null,
    @SerializedName("escaneos") val escaneos: List<ChecklogDto>? = emptyList(),
    @SerializedName("incidentes") val incidentes: List<IncidentDto>? = emptyList()
)
