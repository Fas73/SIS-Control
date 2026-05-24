package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RoundApiService {

    @GET("api/rondas/estado-actual/{userId}")
    suspend fun getCurrentState(@Path("userId") userId: Long): Response<CurrentStateResponseDto>

    @GET("api/rondas/buscar")
    suspend fun getAllRounds(): Response<List<RoundResponseDto>>

    @POST("api/rondas/iniciar")
    suspend fun startRound(
        @Query("userId") userId: Long,
        @Query("installationId") installationId: Long
    ): Response<RoundStartResponseDto>

    @PUT("api/rondas/finalizar/{id}")
    suspend fun endRound(
        @Path("id") roundId: Long,
        @Body request: EndRoundRequest
    ): Response<RoundEndResponseDto>

    @GET("api/rondas/{id}")
    suspend fun getRoundDetail(@Path("id") id: Long): RoundDetailResponseDto

    @POST("api/rondas/escaneo")
    suspend fun scanCheckpoint(@Body request: ScanCheckpointRequest): Response<Map<String, Any>>

    @PUT("api/rondas/cancelar/{id}")
    suspend fun cancelRoundAdministratively(
        @Path("id") roundId: Long,
        @Query("adminId") adminId: Long,
        @Query("motivo") motivo: String?
    ): Response<Map<String, Any>>

    @PUT("api/rondas/jornada/cancelar/{id}")
    suspend fun cancelShiftAdministratively(
        @Path("id") shiftId: Long,
        @Query("adminId") adminId: Long,
        @Query("motivo") motivo: String?
    ): Response<Map<String, Any>>
}
