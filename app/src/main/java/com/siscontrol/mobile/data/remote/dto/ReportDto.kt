package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RoundStatsResponse(
    @SerializedName("totalRondas") val totalRondas: Int,
    @SerializedName("completadas") val completadas: Int,
    @SerializedName("enProgreso") val enProgreso: Int,
    @SerializedName("periodo") val periodo: PeriodoDto
)

data class PeriodoDto(
    @SerializedName("desde") val desde: String,
    @SerializedName("hasta") val hasta: String
)

data class GuardStatsResponse(
    @SerializedName("guardiasConJornadaIniciada") val guardiasConJornadaIniciada: Int,
    @SerializedName("totalGuardias") val totalGuardias: Int
)

data class AdminDashboardResponse(
    @SerializedName("totalGuards") val totalGuards: Int = 0,
    @SerializedName("activeShiftsCount") val activeShiftsCount: Int = 0,
    @SerializedName("totalRoundsToday") val totalRoundsToday: Int = 0,
    @SerializedName("roundsInProgress") val roundsInProgress: Int = 0,
    @SerializedName("roundsCompleted") val roundsCompleted: Int = 0,
    @SerializedName("totalIncidents") val totalIncidents: Int = 0,
    @SerializedName("pendingIncidents") val pendingIncidents: Int = 0,
    @SerializedName("totalInstallations") val totalInstallations: Int = 0,
    @SerializedName("activeInstallationsCount") val activeInstallationsCount: Int = 0,
    @SerializedName("activeRoundsList") val activeRoundsList: List<DashboardActiveRoundDto> = emptyList(),
    @SerializedName("activeShiftsList") val activeShiftsList: List<DashboardActiveShiftDto> = emptyList()
)

data class DashboardActiveRoundDto(
    @SerializedName("id") val id: Long,
    @SerializedName("guardName") val guardName: String,
    @SerializedName("location") val location: String,
    @SerializedName("progreso") val progreso: Float,
    @SerializedName("statusDisplay") val statusDisplay: String,
    @SerializedName("status") val status: String,
    @SerializedName("checkpointsExecuted") val checkpointsExecuted: Int = 0,
    @SerializedName("checkpointsTotal") val checkpointsTotal: Int = 0
)

data class DashboardActiveShiftDto(
    @SerializedName("id") val id: Long,
    @SerializedName("guardName") val guardName: String,
    @SerializedName("location") val location: String,
    @SerializedName("entryTime") val entryTime: String
)

data class GuardRoundHistoryResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("completas") val completas: Int,
    @SerializedName("porcentajeExito") val porcentajeExito: String,
    @SerializedName("rondas") val rondas: List<RoundHistoryItemDto>
)

data class RoundHistoryItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("installationName") val installationName: String,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("status") val status: String?,
    @SerializedName("statusDisplay") val statusDisplay: String?,
    
    @SerializedName("checkpointsExecuted") 
    val checkpointsExecuted: Int,
    
    @SerializedName("checkpointsTotal") 
    val checkpointsTotal: Int,
    
    @SerializedName("incidentCount") 
    val incidentCount: Int,
    
    @SerializedName("shiftStartTime") 
    val shiftStartTime: String?,
    
    @SerializedName("shiftEndTime") 
    val shiftEndTime: String?,
    
    @SerializedName("detailedSummary") 
    val detailedSummary: String?
)
