package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.AttendanceResponse
import com.siscontrol.mobile.data.remote.dto.AttendanceWrapperResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AttendanceApiService {

    @GET("api/asistencia")
    suspend fun getAllShifts(): List<AttendanceResponse>

    @POST("api/asistencia/check-in")
    suspend fun checkIn(@Body request: AttendanceRequest): Response<AttendanceWrapperResponse>

    @POST("api/asistencia/check-out")
    suspend fun checkOut(@Body request: AttendanceRequest): Response<AttendanceWrapperResponse>
}
