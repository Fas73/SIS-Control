package com.siscontrol.mobile.data.remote

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoundApiService {

    @POST("api/rondas/iniciar")
    suspend fun startRound(
        @Query("userId") userId: Long,
        @Query("installationId") installationId: Long
    ): Response<Map<String, Any>>

    @PUT("api/rondas/finalizar/{id}")
    suspend fun endRound(
        @Path("id") roundId: Long
    ): Response<Map<String, Any>>
}
