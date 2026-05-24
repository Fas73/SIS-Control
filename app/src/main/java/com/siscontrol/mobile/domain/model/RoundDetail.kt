package com.siscontrol.mobile.domain.model

data class RoundDetail(
    val round: Round?,
    val scans: List<Checklog>,
    val incidents: List<Map<String, Any>>
)
