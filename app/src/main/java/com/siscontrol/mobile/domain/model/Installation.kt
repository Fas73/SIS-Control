package com.siscontrol.mobile.domain.model

data class Installation(
    val id: Long,
    val name: String,
    val address: String,
    val clientName: String?,
    val location: String?,
    val status: Int,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Double
)

/**
 * Parámetros de dominio para la creación de una instalación.
 */
data class InstallationCreationParam(
    val name: String,
    val address: String,
    val clientName: String,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Double = 100.0
)

