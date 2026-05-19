package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.RoundRepository

class CancelRoundUseCase(
    private val repository: RoundRepository
) {
    suspend operator fun invoke(roundId: Long, adminId: Long, motivo: String?): Result<Unit> {
        return repository.cancelRound(roundId, adminId, motivo)
    }
}
