package com.siscontrol.mobile.domain.model

data class DashboardActiveRound(
    val id: Long,
    val guardName: String,
    val location: String,
    val progress: Float,
    val statusDisplay: String,
    val status: String
)

data class DashboardActiveShift(
    val id: Long,
    val guardName: String,
    val location: String,
    val entryTime: String
)

data class AdminDashboard(
    val totalGuards: Int,
    val activeShiftsCount: Int,
    val totalRoundsToday: Int,
    val roundsInProgress: Int,
    val roundsCompleted: Int,
    val totalIncidents: Int,
    val pendingIncidents: Int,
    val totalInstallations: Int,
    val activeInstallationsCount: Int,
    val activeRoundsList: List<DashboardActiveRound>,
    val activeShiftsList: List<DashboardActiveShift>
)
