package com.siscontrol.mobile.domain.model

data class Incident(
    val id: Long?,
    val title: String,
    val description: String,
    val severity: String,
    val type: String,
    val imageUrl: String?,
    val createdAt: String?,
    val status: Int?,
    val username: String?,
    val clientName: String?,
    val checkpointName: String?,
    val executionOrder: Int?,
    val roundExecutionId: Long?
)
