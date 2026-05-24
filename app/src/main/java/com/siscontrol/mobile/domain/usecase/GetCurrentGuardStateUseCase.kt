package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.GuardCurrentState
import com.siscontrol.mobile.domain.repository.RoundRepository

class GetCurrentGuardStateUseCase(
    private val repository: RoundRepository
) {
    suspend operator fun invoke(userId: Long): Result<GuardCurrentState> {
        return repository.getCurrentState(userId)
    }
}
