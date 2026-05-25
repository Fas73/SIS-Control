package com.siscontrol.mobile.data.mapper

import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.model.Incident

fun IncidentDto.toDomain(): Incident {
    return Incident(
        id = this.id,
        title = this.title,
        description = this.description,
        severity = this.severity,
        type = this.type,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt,
        status = this.status,
        username = this.username,
        clientName = this.clientName,
        checkpointName = this.checkpointName,
        executionOrder = this.executionOrder,
        roundExecutionId = this.roundExecutionId,
        descripcionOriginal = this.descripcionOriginal,
        tipoIncidenteIA = this.tipoIncidenteIA,
        prioridadIA = this.prioridadIA,
        resumenIA = this.resumenIA,
        accionSugeridaIA = this.accionSugeridaIA,
        requiereAtencionInmediata = this.requiereAtencionInmediata,
        estadoAnalisisIA = this.estadoAnalisisIA,
        fechaAnalisisIA = this.fechaAnalisisIA,
        modeloIA = this.modeloIA
    )
}

fun Incident.toDto(): IncidentDto {
    return IncidentDto(
        id = this.id,
        title = this.title,
        description = this.description,
        severity = this.severity,
        type = this.type,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt,
        status = this.status,
        username = this.username,
        clientName = this.clientName,
        checkpointName = this.checkpointName,
        executionOrder = this.executionOrder,
        roundExecutionId = this.roundExecutionId,
        descripcionOriginal = this.descripcionOriginal,
        tipoIncidenteIA = this.tipoIncidenteIA,
        prioridadIA = this.prioridadIA,
        resumenIA = this.resumenIA,
        accionSugeridaIA = this.accionSugeridaIA,
        requiereAtencionInmediata = this.requiereAtencionInmediata,
        estadoAnalisisIA = this.estadoAnalisisIA,
        fechaAnalisisIA = this.fechaAnalisisIA,
        modeloIA = this.modeloIA
    )
}
