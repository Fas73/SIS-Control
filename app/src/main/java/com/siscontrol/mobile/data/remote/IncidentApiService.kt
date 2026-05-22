package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.IncidentDto
import retrofit2.Response
import retrofit2.http.*

interface IncidentApiService {

    @POST("api/incidents")
    suspend fun createIncident(@Body incident: IncidentDto): Response<IncidentDto>

    @GET("api/incidents")
    suspend fun getAllIncidents(): List<IncidentDto>

    // Sincronizado con IncidentController.java (Backend)
    @POST("api/incidents/panico")
    suspend fun triggerPanic(
        @Query("roundExecutionId") roundExecutionId: Long,
        @Query("descripcion") descripcion: String? = null
    ): Response<IncidentDto>
}
