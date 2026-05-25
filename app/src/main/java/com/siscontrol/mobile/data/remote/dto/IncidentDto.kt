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

    // --- NUEVOS CAMPOS APLANADOS (Desde AlertNotificationDTO) ---
    @SerializedName("username")
    val username: String? = null,

    @SerializedName("clientName")
    val clientName: String? = null,

    @SerializedName("checkpointName")
    val checkpointName: String? = null,

    @SerializedName("executionOrder")
    val executionOrder: Int? = null,

    @SerializedName("roundExecutionId")
    val roundExecutionId: Long? = null,

    // Compatibilidad con objeto anidado (si el GET aún lo envía así)
    @SerializedName("roundExecution")
    val roundExecution: RoundResponseDto? = null,

    // --- NUEVOS CAMPOS PARA ANALISIS IA GEMINI (MVP) ---
    @SerializedName("descripcionOriginal")
    val descripcionOriginal: String? = null,

    @SerializedName("tipoIncidenteIA")
    val tipoIncidenteIA: String? = null,

    @SerializedName("prioridadIA")
    val prioridadIA: String? = null,

    @SerializedName("resumenIA")
    val resumenIA: String? = null,

    @SerializedName("accionSugeridaIA")
    val accionSugeridaIA: String? = null,

    @SerializedName("requiereAtencionInmediata")
    val requiereAtencionInmediata: Boolean? = null,

    @SerializedName("estadoAnalisisIA")
    val estadoAnalisisIA: String? = null,

    @SerializedName("fechaAnalisisIA")
    val fechaAnalisisIA: String? = null,

    @SerializedName("modeloIA")
    val modeloIA: String? = null
)
