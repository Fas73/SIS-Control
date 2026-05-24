package com.siscontrol.mobile.domain.model

data class RoundHistoryItem(
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

data class GuardRoundHistory(
    val total: Int,
    val completed: Int,
    val successRate: String,
    val rounds: List<RoundHistoryItem>
)
