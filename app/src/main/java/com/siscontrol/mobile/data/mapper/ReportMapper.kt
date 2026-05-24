package com.siscontrol.mobile.data.mapper

import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.data.remote.dto.DashboardActiveRoundDto
import com.siscontrol.mobile.data.remote.dto.DashboardActiveShiftDto
import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse
import com.siscontrol.mobile.data.remote.dto.RoundHistoryItemDto
import com.siscontrol.mobile.domain.model.AdminDashboard
import com.siscontrol.mobile.domain.model.DashboardActiveRound
import com.siscontrol.mobile.domain.model.DashboardActiveShift
import com.siscontrol.mobile.domain.model.GuardRoundHistory
import com.siscontrol.mobile.domain.model.RoundHistoryItem

fun DashboardActiveRoundDto.toDomain(): DashboardActiveRound {
    return DashboardActiveRound(
        id = this.id,
        guardName = this.guardName,
        location = this.location,
        progress = this.progreso,
        statusDisplay = this.statusDisplay,
        status = this.status
    )
}

fun DashboardActiveShiftDto.toDomain(): DashboardActiveShift {
    return DashboardActiveShift(
        id = this.id,
        guardName = this.guardName,
        location = this.location,
        entryTime = this.entryTime
    )
}

fun AdminDashboardResponse.toDomain(): AdminDashboard {
    return AdminDashboard(
        totalGuards = this.totalGuards,
        activeShiftsCount = this.activeShiftsCount,
        totalRoundsToday = this.totalRoundsToday,
        roundsInProgress = this.roundsInProgress,
        roundsCompleted = this.roundsCompleted,
        totalIncidents = this.totalIncidents,
        pendingIncidents = this.pendingIncidents,
        totalInstallations = this.totalInstallations,
        activeInstallationsCount = this.activeInstallationsCount,
        activeRoundsList = this.activeRoundsList.map { it.toDomain() },
        activeShiftsList = this.activeShiftsList.map { it.toDomain() }
    )
}

fun RoundHistoryItemDto.toDomain(): RoundHistoryItem {
    return RoundHistoryItem(
        id = this.id,
        installationName = this.installationName,
        startTime = this.startTime,
        endTime = this.endTime,
        durationMinutes = this.durationMinutes,
        status = this.status,
        statusDisplay = this.statusDisplay,
        checkpointsExecuted = this.checkpointsExecuted,
        checkpointsTotal = this.checkpointsTotal,
        incidentCount = this.incidentCount,
        shiftStartTime = this.shiftStartTime,
        shiftEndTime = this.shiftEndTime,
        detailedSummary = this.detailedSummary
    )
}

fun GuardRoundHistoryResponse.toDomain(): GuardRoundHistory {
    return GuardRoundHistory(
        total = this.total,
        completed = this.completas,
        successRate = this.porcentajeExito,
        rounds = this.rondas.map { it.toDomain() }
    )
}
