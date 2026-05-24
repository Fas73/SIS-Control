package com.siscontrol.mobile.data.mapper

import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationIdRequest
import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.domain.model.Installation

fun InstallationDto.toDomain(): Installation {
    return Installation(
        id = this.id ?: 0L,
        name = this.name ?: "Sin nombre",
        address = this.address ?: "Sin dirección",
        clientName = this.clientName,
        location = this.location,
        status = this.status ?: 1,
        latitude = this.latitude ?: 0.0,
        longitude = this.longitude ?: 0.0,
        radiusInMeters = this.radiusInMeters ?: 100.0
    )
}

fun Installation.toDto(): InstallationDto {
    return InstallationDto(
        id = this.id,
        name = this.name,
        address = this.address,
        clientName = this.clientName,
        location = this.location,
        status = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        radiusInMeters = this.radiusInMeters
    )
}

fun CheckpointDto.toDomain(): Checkpoint {
    return Checkpoint(
        id = this.id ?: 0L,
        name = this.name ?: "Punto sin nombre",
        locationDescription = this.locationDescription,
        nfcTagCode = this.nfcTagCode ?: "",
        executionOrder = this.executionOrder ?: 0,
        instruction = this.instruction,
        status = this.status ?: 1,
        installationId = this.installation?.id ?: 0L
    )
}

fun Checkpoint.toDto(): CheckpointDto {
    return CheckpointDto(
        id = this.id,
        name = this.name,
        locationDescription = this.locationDescription,
        nfcTagCode = this.nfcTagCode,
        executionOrder = this.executionOrder,
        instruction = this.instruction,
        status = this.status,
        installation = InstallationIdRequest(this.installationId)
    )
}

fun com.siscontrol.mobile.domain.model.InstallationCreationParam.toDto(): com.siscontrol.mobile.data.remote.dto.InstallationRequestDto {
    return com.siscontrol.mobile.data.remote.dto.InstallationRequestDto(
        name = this.name,
        address = this.address,
        clientName = this.clientName,
        latitude = this.latitude,
        longitude = this.longitude,
        radiusInMeters = this.radiusInMeters
    )
}

fun com.siscontrol.mobile.domain.model.CheckpointCreationParam.toDto(): com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto {
    return com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto(
        name = this.name,
        locationDescription = this.locationDescription,
        nfcTagCode = this.nfcTagCode,
        executionOrder = this.executionOrder,
        installationId = this.installationId,
        installation = InstallationIdRequest(this.installationId),
        instruction = this.instruction
    )
}

