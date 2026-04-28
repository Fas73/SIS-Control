package com.siscontrol.mobile.data.remote.dto

/**
 * DTO para las dependencias (recintos) asignados o disponibles.
 */
data class InstallationDto(
    val id: Long,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val createdAt: String? = null
)

data class InstallationRequestDto(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)
