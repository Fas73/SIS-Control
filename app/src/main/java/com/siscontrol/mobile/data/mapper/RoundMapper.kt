package com.siscontrol.mobile.data.mapper

import com.siscontrol.mobile.data.remote.dto.ChecklogDto
import com.siscontrol.mobile.data.remote.dto.CurrentStateResponseDto
import com.siscontrol.mobile.data.remote.dto.RoundDetailResponseDto
import com.siscontrol.mobile.data.remote.dto.RoundResponseDto
import com.siscontrol.mobile.data.remote.dto.ShiftDto
import com.siscontrol.mobile.domain.model.Attendance
import com.siscontrol.mobile.domain.model.Checklog
import com.siscontrol.mobile.domain.model.GuardCurrentState
import com.siscontrol.mobile.domain.model.Round
import com.siscontrol.mobile.domain.model.RoundDetail

fun RoundResponseDto.toDomain(): Round {
    return Round(
        id = this.id ?: 0L,
        startTime = this.startTime,
        endTime = this.endTime,
        status = this.status,
        observations = this.observations,
        workerId = this.workerId,
        worker = this.worker?.toDomain(),
        installationId = this.installationId,
        installation = this.installation?.toDomain()
    )
}

fun ShiftDto.toDomain(): Attendance {
    return Attendance(
        id = this.id ?: 0L,
        entryTime = this.entryTime,
        exitTime = this.exitTime,
        status = this.status,
        installation = this.installation?.toDomain(),
        worker = this.worker?.toDomain()
    )
}

fun ChecklogDto.toDomain(): Checklog {
    return Checklog(
        id = this.id ?: 0L,
        scannedAt = this.scannedAt,
        checkpoint = this.checkpoint?.toDomain(),
        notes = this.notes
    )
}

fun CurrentStateResponseDto.toDomain(): GuardCurrentState {
    return GuardCurrentState(
        isShiftActive = this.jornadaActiva ?: false,
        isRoundActive = this.rondaActiva ?: false,
        shift = this.jornada?.toDomain(),
        round = this.ronda?.toDomain(),
        completedScans = this.escaneosCompletados?.map { it.toDomain() } ?: emptyList()
    )
}

fun RoundDetailResponseDto.toDomain(): RoundDetail {
    return RoundDetail(
        round = this.ronda?.toDomain(),
        scans = this.escaneos?.map { it.toDomain() } ?: emptyList(),
        incidents = this.incidentes ?: emptyList()
    )
}
