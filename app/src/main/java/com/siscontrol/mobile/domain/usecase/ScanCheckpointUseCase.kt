package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.RoundRepository

class ScanCheckpointUseCase(
    private val repository: RoundRepository
) {
    suspend operator fun invoke(roundId: Long, checkpointId: Long, comment: String = "Todo en Orden"): Result<Unit> {
        return repository.scanCheckpoint(roundId, checkpointId, comment)
    }
}
