package com.siscontrol.mobile.data.remote.dto

data class RoundStatsResponse(
    val totalRondas: Int,
    val completadas: Int,
    val enProgreso: Int,
    val periodo: PeriodoDto
)

data class PeriodoDto(
    val desde: String,
    val hasta: String
)

data class GuardStatsResponse(
    val guardiasConJornadaIniciada: Int,
    val totalGuardias: Int
)

data class AdminDashboardResponse(
    val totalGuards: Int = 0,
    val activeShiftsCount: Int = 0,
    val totalRoundsToday: Int = 0,
    val roundsInProgress: Int = 0,
    val roundsCompleted: Int = 0,
    val totalIncidents: Int = 0,
    val pendingIncidents: Int = 0,
    val totalInstallations: Int = 0,
    val activeInstallationsCount: Int = 0,
    val activeRoundsList: List<DashboardActiveRoundDto> = emptyList(),
    val activeShiftsList: List<DashboardActiveShiftDto> = emptyList()
)

data class DashboardActiveRoundDto(
    val id: Long,
    val guardName: String,
    val location: String,
    val progreso: Float,
    val statusDisplay: String,
    val status: String
)

data class DashboardActiveShiftDto(
    val id: Long,
    val guardName: String,
    val location: String,
    val entryTime: String
)

data class GuardRoundHistoryResponse(
    val total: Int,
    val completas: Int,
    val porcentajeExito: String,
    val rondas: List<RoundHistoryItemDto>
)

data class RoundHistoryItemDto(
    val id: Long,
    val installationName: String,
    val startTime: String?,
    val endTime: String?,
    val durationMinutes: Int,
    val status: String?,
    val statusDisplay: String?,
    val checkpointsExecuted: Int,
    val checkpointsTotal: Int,
    val incidentCount: Int,
    val shiftStartTime: String?,
    val shiftEndTime: String?,
    val detailedSummary: String?
)
