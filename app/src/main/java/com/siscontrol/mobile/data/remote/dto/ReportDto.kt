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
    val totalGuards: Int,
    val activeShiftsCount: Int,
    val totalRoundsToday: Int,
    val roundsInProgress: Int,
    val roundsCompleted: Int,
    val totalIncidents: Int,
    val pendingIncidents: Int,
    val totalInstallations: Int,
    val activeInstallationsCount: Int,
    val activeRoundsList: List<DashboardActiveRoundDto>,
    val activeShiftsList: List<DashboardActiveShiftDto>
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
