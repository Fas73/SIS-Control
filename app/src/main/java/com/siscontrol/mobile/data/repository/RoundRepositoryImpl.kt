package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.RoundApiService
import com.siscontrol.mobile.domain.repository.RoundRepository

class RoundRepositoryImpl(
    private val api: RoundApiService
) : RoundRepository {

    override suspend fun startRound(userId: Long, installationId: Long): Result<Long> {
        return try {
            val response = api.startRound(userId, installationId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val id = (body["id"] as? Double)?.toLong() ?: 0L
                Result.success(id)
            } else {
                Result.failure(Exception("Error al iniciar ronda: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun endRound(roundId: Long): Result<Unit> {
        return try {
            val response = api.endRound(roundId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al finalizar ronda: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
