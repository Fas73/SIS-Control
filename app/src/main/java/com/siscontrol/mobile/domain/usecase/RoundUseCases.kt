package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.RoundResponseDto
import com.siscontrol.mobile.domain.repository.RoundRepository

class GetAllRoundsUseCase(private val repository: RoundRepository) {
    suspend operator fun invoke(): Result<List<RoundResponseDto>> = repository.getAllRounds()
}

class StartRoundUseCase(private val repository: RoundRepository) {
    suspend operator fun invoke(userId: Long, installationId: Long): Result<Long> {
        return repository.startRound(userId, installationId)
    }
}

class EndRoundUseCase(private val repository: RoundRepository) {
    suspend operator fun invoke(roundId: Long, observations: String): Result<Unit> {
        return repository.endRound(roundId, observations)
    }
}
