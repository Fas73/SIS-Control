package com.siscontrol.mobile.domain.model

data class GuardCurrentState(
    val isShiftActive: Boolean,
    val isRoundActive: Boolean,
    val shift: Attendance?,
    val round: Round?,
    val completedScans: List<Checklog>
)
