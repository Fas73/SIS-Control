package com.siscontrol.mobile.domain.repository

interface RoundRepository {
    suspend fun startRound(userId: Long, installationId: Long): Result<Long>
    suspend fun endRound(roundId: Long): Result<Unit>
}
