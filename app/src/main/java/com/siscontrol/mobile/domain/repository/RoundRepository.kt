package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.GuardCurrentState
import com.siscontrol.mobile.domain.model.Round
import com.siscontrol.mobile.domain.model.RoundDetail

/**
 * Contrato de repositorio para el control de rondas, turnos y escaneo de puntos.
 * Todas las operaciones regresan e interactúan con entidades de dominio.
 */
interface RoundRepository {
    suspend fun getCurrentState(userId: Long): Result<GuardCurrentState>
    suspend fun getAllRounds(): Result<List<Round>>
    suspend fun startRound(userId: Long, installationId: Long): Result<Long>
    suspend fun endRound(roundId: Long, observations: String): Result<Unit>
    suspend fun getRoundDetail(roundId: Long): Result<RoundDetail>
    suspend fun scanCheckpoint(roundId: Long, checkpointId: Long, comment: String, status: Int = 1, imageUrl: String? = null): Result<String>
    suspend fun cancelRound(roundId: Long, adminId: Long, motivo: String?): Result<Unit>
    suspend fun cancelShift(shiftId: Long, adminId: Long, motivo: String?): Result<Unit>
}

