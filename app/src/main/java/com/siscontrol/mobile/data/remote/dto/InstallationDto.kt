package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para recibir la información de la instalación desde el servidor.
 */
data class InstallationDto(
    @SerializedName("id")
    val id: Long? = 0L,
    
    @SerializedName("name")
    val name: String? = "Sin nombre",
    
    @SerializedName("address")
    val address: String? = "Sin dirección",
    
    @SerializedName("clientName")
    val clientName: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("status")
    val status: Int? = 1,
    
    @SerializedName("latitude")
    val latitude: Double? = 0.0,
    
    @SerializedName("longitude")
    val longitude: Double? = 0.0,
    
    @SerializedName("radiusInMeters")
    val radiusInMeters: Double? = 100.0
)

data class InstallationIdRequest(val id: Long)

/**
 * DTO para la creación/edición de un Checkpoint (Punto NFC).
 */
data class CheckpointDto(
    @SerializedName("id")
    val id: Long? = 0L,
    
    @SerializedName("name")
    val name: String? = "Punto sin nombre",
    
    @SerializedName("locationDescription")
    val locationDescription: String? = null,
    
    @SerializedName("nfcTagCode")
    val nfcTagCode: String? = "",
    
    @SerializedName("executionOrder")
    val executionOrder: Int? = 0,
    
    @SerializedName("instruction")
    val instruction: String? = null,
    
    @SerializedName("status")
    val status: Int? = 1,
    
    @SerializedName("installation")
    val installation: InstallationIdRequest? = null
)

/**
 * DTO para enviar la creación de una instalación al backend.
 */
data class InstallationRequestDto(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("clientName")
    val clientName: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("radiusInMeters")
    val radiusInMeters: Double = 100.0
)

/**
 * DTO para enviar la creación de un punto de control.
 */
data class CheckpointRequestDto(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("locationDescription")
    val locationDescription: String,
    
    @SerializedName("nfcTagCode")
    val nfcTagCode: String,
    
    @SerializedName("executionOrder")
    val executionOrder: Int,
    
    @SerializedName("installationId")
    val installationId: Long? = 0L,
    
    @SerializedName("installation")
    val installation: InstallationIdRequest? = null,
    
    @SerializedName("instruction")
    val instruction: String? = null
)
