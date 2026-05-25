package com.siscontrol.mobile.domain.model

data class Incident(
    val id: Long?,
    val title: String,
    val description: String,
    val severity: String,
    val type: String,
    val imageUrl: String?,
    val createdAt: String?,
    val status: Int?,
    val username: String?,
    val clientName: String?,
    val checkpointName: String?,
    val executionOrder: Int?,
    val roundExecutionId: Long?,
    val descripcionOriginal: String? = null,
    val tipoIncidenteIA: String? = null,
    val prioridadIA: String? = null,
    val resumenIA: String? = null,
    val accionSugeridaIA: String? = null,
    val requiereAtencionInmediata: Boolean? = null,
    val estadoAnalisisIA: String? = null,
    val fechaAnalisisIA: String? = null,
    val modeloIA: String? = null
)
