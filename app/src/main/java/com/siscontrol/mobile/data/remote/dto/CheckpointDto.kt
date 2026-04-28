package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para la gestión de puntos de control.
 */
data class CheckpointDto(
    val id: Long,
    val installationId: Long,
    val name: String,
    val type: String, // "NFC" or "QR"
    val tagIdentifier: String,
    val lat: Double,
    val lng: Double,
    val sequenceOrder: Int? = null
)

data class CheckpointRequestDto(
    val name: String,
    val type: String,
    val tagIdentifier: String,
    val lat: Double,
    val lng: Double,
    val sequenceOrder: Int? = null
)
