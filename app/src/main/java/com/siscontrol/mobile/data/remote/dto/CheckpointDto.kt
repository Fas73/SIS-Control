package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para la gestión de puntos de control.
 */
data class CheckpointDto(
    val id: Long,
    val name: String,
    val executionOrder: Int = 0,
    val nfcTagCode: String,
    val locationDescription: String? = null,
    val instruction: String? = null,
    val status: Int? = 1,
    val installation: InstallationIdRequest? = null
)

/**
 * DTO para enviar la creación de un checkpoint al backend.
 */
data class CheckpointRequestDto(
    val name: String,
    val executionOrder: Int = 0,
    val nfcTagCode: String,
    val locationDescription: String? = null,
    val instruction: String? = null,
    val installation: InstallationIdRequest
)

data class InstallationIdRequest(
    val id: Long
)
