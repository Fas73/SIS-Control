package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Servicio Retrofit para gestionar recintos e infraestructuras de marcaje (Checkpoints).
 */
interface InstallationApiService {

    @GET("/api/installations")
    suspend fun getInstallations(): List<InstallationDto>

    @POST("/api/installations")
    suspend fun createInstallation(@Body request: InstallationRequestDto): InstallationDto

    @GET("/api/installations/{id}/checkpoints")
    suspend fun getCheckpoints(@Path("id") installationId: Long): List<CheckpointDto>

    @POST("/api/installations/{id}/checkpoints")
    suspend fun createCheckpoint(
        @Path("id") installationId: Long,
        @Body request: CheckpointRequestDto
    ): CheckpointDto
}
