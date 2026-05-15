package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AttendanceApiService {

    @POST("api/asistencia/check-in")
    suspend fun checkIn(@Body request: AttendanceRequest): Response<AttendanceResponse>

    @POST("api/asistencia/check-out")
    suspend fun checkOut(@Body request: Map<String, Long>): Response<AttendanceResponse>
}
