package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para recibir la información de la instalación desde el servidor.
 */
data class InstallationDto(
    val id: Long? = 0L,
    val name: String? = "Sin nombre",
    val address: String? = "Sin dirección",
    val clientName: String? = null,
    val location: String? = null,
    val status: Int? = 1,
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val radiusInMeters: Double? = 100.0
)

data class InstallationIdRequest(val id: Long)

/**
 * DTO para la creación/edición de un Checkpoint (Punto NFC).
 */
data class CheckpointDto(
    val id: Long? = 0L,
    val name: String? = "Punto sin nombre",
    val locationDescription: String? = null,
    val nfcTagCode: String? = "",
    val executionOrder: Int? = 0,
    val instruction: String? = null,
    val status: Int? = 1,
    val installation: InstallationIdRequest? = null
)

/**
 * DTO para enviar la creación de una instalación al backend.
 */
data class InstallationRequestDto(
    val name: String,
    val address: String,
    val clientName: String,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Double = 100.0
)

/**
 * DTO para enviar la creación de un punto de control.
 */
data class CheckpointRequestDto(
    val name: String,
    val locationDescription: String,
    val nfcTagCode: String,
    val executionOrder: Int,
    val installationId: Long? = 0L,
    val installation: InstallationIdRequest? = null,
    val instruction: String? = null
)
