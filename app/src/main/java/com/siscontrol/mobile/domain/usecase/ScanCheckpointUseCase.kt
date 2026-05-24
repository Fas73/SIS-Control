package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.RoundRepository

class ScanCheckpointUseCase(
    private val repository: RoundRepository
) {
    suspend operator fun invoke(roundId: Long, checkpointId: Long, comment: String = "Todo en Orden", status: Int = 1, imageUrl: String? = null): Result<String> {
        return repository.scanCheckpoint(roundId, checkpointId, comment, status, imageUrl)
    }
}
