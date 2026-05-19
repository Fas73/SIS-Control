package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.IncidentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

interface IncidentApiService {

    @POST("/api/incidents")
    suspend fun createIncident(@Body incident: IncidentDto): Response<IncidentDto>

    @GET("/api/incidents")
    suspend fun getAllIncidents(): List<IncidentDto>
}