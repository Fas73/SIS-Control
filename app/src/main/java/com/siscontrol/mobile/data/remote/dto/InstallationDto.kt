package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para recibir la información de la instalación desde el servidor.
 */
data class InstallationDto(
    val id: Long? = null,
    val name: String,
    val address: String,
    val clientName: String?,
    val location: String?,
    val status: Int? = 1, // 1 para Activa, 0 para Inactiva
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val radiusInMeters: Double? = null
)

/**
 * DTO para enviar la creación de una instalación al backend.
 * Debe coincidir exactamente con tu cuerpo de Postman.
 */
data class InstallationRequestDto(
    val name: String,
    val address: String,
    val clientName: String,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Double = 100.0
)
