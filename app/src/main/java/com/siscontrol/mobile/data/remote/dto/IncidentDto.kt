package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO unificado para Incidentes y Alertas en tiempo real.
 * Sincronizado con AlertNotificationDTO.java del Backend.
 */
data class IncidentDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("severity")
    val severity: String, // "Alta", "Media", "Baja"

    @SerializedName("type")
    val type: String,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("status")
    val status: Int? = 0,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("clientName")
    val clientName: String? = null,

    @SerializedName("checkpointName")
    val checkpointName: String? = null,

    @SerializedName("checkpointOrder")
    val checkpointOrder: Int? = null,

    @SerializedName("executionOrder")
    val executionOrder: Int? = null,

    @SerializedName("roundExecutionId")
    val roundExecutionId: Long? = null,

    @SerializedName("checklogId")
    val checklogId: Long? = null,

    @SerializedName("roundExecution")
    val roundExecution: RoundResponseDto? = null
)
