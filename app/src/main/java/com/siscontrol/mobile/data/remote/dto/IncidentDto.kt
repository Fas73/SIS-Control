package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Mapeo exacto de IncidentDTO.java del Backend.
 * Permite enviar reportes de incidentes desde la App.
 */
data class IncidentDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("severity")
    val severity: String, // "BAJA", "MEDIA", "ALTA"

    @SerializedName("type")
    val type: String, // "ROBO", "VANDALISMO", "HALLAZGO", etc.

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("roundExecutionId")
    val roundExecutionId: Long,

    @SerializedName("checklogId")
    val checklogId: Long? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("status")
    val status: Int = 0 // 0: Pendiente, 1: Atendido
)