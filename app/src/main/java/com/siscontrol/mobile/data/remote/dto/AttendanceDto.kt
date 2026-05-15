package com.siscontrol.mobile.data.remote.dto

data class AttendanceRequest(
    val userId: Long,
    val installationId: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class AttendanceResponse(
    val id: Long,
    val userId: Long,
    val installationId: Long,
    val checkInTime: String,
    val checkOutTime: String? = null,
    val status: String
)
