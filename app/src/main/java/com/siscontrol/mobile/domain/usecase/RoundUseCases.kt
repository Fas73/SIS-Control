package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.RoundRepository

class StartRoundUseCase(private val repository: RoundRepository) {
    suspend operator fun invoke(userId: Long, installationId: Long): Result<Long> {
        return repository.startRound(userId, installationId)
    }
}

class EndRoundUseCase(private val repository: RoundRepository) {
    suspend operator fun invoke(roundId: Long): Result<Unit> {
        return repository.endRound(roundId)
    }
}
