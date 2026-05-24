package com.siscontrol.mobile.data.mapper

import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import com.siscontrol.mobile.domain.model.Attendance

fun AttendanceResponse.toDomain(): Attendance {
    return Attendance(
        id = this.id ?: 0L,
        entryTime = this.entryTime,
        exitTime = this.exitTime,
        status = this.status,
        installation = this.installation?.toDomain(),
        worker = this.worker?.toDomain()
    )
}

fun com.siscontrol.mobile.domain.model.AttendanceParam.toDto(): com.siscontrol.mobile.data.remote.dto.AttendanceRequest {
    return com.siscontrol.mobile.data.remote.dto.AttendanceRequest(
        userId = this.userId,
        installationId = this.installationId,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

