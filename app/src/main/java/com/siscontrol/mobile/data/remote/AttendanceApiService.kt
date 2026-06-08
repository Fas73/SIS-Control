package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AttendanceApiService {

    @GET("api/asistencia")
    suspend fun getAllShifts(): List<AttendanceResponse>

    // Usamos el Wrapper porque tu Java envía Map.of("jornada", ...)
    @POST("api/asistencia/check-in")
    suspend fun checkIn(@Body request: AttendanceRequest): Response<AttendanceWrapperResponse>

    @POST("api/asistencia/check-out")
    suspend fun checkOut(@Body request: AttendanceRequest): Response<AttendanceWrapperResponse>

    @GET("api/asistencia/reporte-jornada/{shiftId}")
    suspend fun getShiftReport(
        @Path("shiftId") shiftId: Long
    ): Response<ShiftReportDto>
}
