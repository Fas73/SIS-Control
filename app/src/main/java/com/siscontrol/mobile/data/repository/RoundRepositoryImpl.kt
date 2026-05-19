package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.RoundApiService
import com.siscontrol.mobile.data.remote.dto.EndRoundRequest
import com.siscontrol.mobile.data.remote.dto.IdRequest
import com.siscontrol.mobile.data.remote.dto.RoundResponseDto
import com.siscontrol.mobile.data.remote.dto.ScanCheckpointRequest
import com.siscontrol.mobile.domain.repository.RoundRepository

class RoundRepositoryImpl(
    private val api: RoundApiService
) : RoundRepository {

    override suspend fun getAllRounds(): Result<List<RoundResponseDto>> {
        return try {
            val response = api.getAllRounds()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startRound(userId: Long, installationId: Long): Result<Long> {
        return try {
            val response = api.startRound(userId, installationId)
            if (response.isSuccessful && response.body() != null) {
                val roundId = response.body()!!.ronda.id
                Result.success(roundId)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                val cleanMessage = if (errorMsg.contains("\"mensaje\":\"")) {
                    errorMsg.substringAfter("\"mensaje\":\"").substringBefore("\"")
                } else "Error ${response.code()}: Contacte al Admin"
                
                Result.failure(Exception(cleanMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun endRound(roundId: Long, observations: String): Result<Unit> {
        return try {
            val response = api.endRound(roundId, EndRoundRequest(observations))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al finalizar ronda: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanCheckpoint(roundId: Long, checkpointId: Long, comment: String): Result<Unit> {
        return try {
            val request = ScanCheckpointRequest(
                roundExecution = IdRequest(roundId),
                checkpoint = IdRequest(checkpointId),
                notes = comment
            )
            val response = api.scanCheckpoint(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al escanear checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRound(roundId: Long, adminId: Long, motivo: String?): Result<Unit> {
        return try {
            val response = api.cancelRoundAdministratively(roundId, adminId, motivo)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cancelar ronda: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelShift(shiftId: Long, adminId: Long, motivo: String?): Result<Unit> {
        return try {
            val response = api.cancelShiftAdministratively(shiftId, adminId, motivo)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cancelar jornada: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
