package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.RoundResponseDto

interface RoundRepository {
    suspend fun getCurrentState(userId: Long): Result<com.siscontrol.mobile.data.remote.dto.CurrentStateResponseDto>
    suspend fun getAllRounds(): Result<List<RoundResponseDto>>
    suspend fun startRound(userId: Long, installationId: Long): Result<Long>
    suspend fun endRound(roundId: Long, observations: String): Result<Unit>
    suspend fun getRoundDetail(roundId: Long): Result<com.siscontrol.mobile.data.remote.dto.RoundDetailResponseDto>
    suspend fun scanCheckpoint(roundId: Long, checkpointId: Long, comment: String, status: Int = 1, imageUrl: String? = null): Result<Long?>
    suspend fun cancelRound(roundId: Long, adminId: Long, motivo: String?): Result<Unit>
    suspend fun cancelShift(shiftId: Long, adminId: Long, motivo: String?): Result<Unit>
}
