package com.siscontrol.mobile.domain.model

data class Checklog(
    val id: Long,
    val scannedAt: String?,
    val checkpoint: Checkpoint?,
    val notes: String?
)
