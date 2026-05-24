package com.siscontrol.mobile.domain.model

data class Round(
    val id: Long,
    val startTime: String?,
    val endTime: String?,
    val status: String?,
    val observations: String?,
    val workerId: Long?,
    val worker: User?,
    val installationId: Long?,
    val installation: Installation?
)
