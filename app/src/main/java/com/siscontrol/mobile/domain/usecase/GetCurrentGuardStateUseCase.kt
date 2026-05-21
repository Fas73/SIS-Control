package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.CurrentStateResponseDto
import com.siscontrol.mobile.domain.repository.RoundRepository

class GetCurrentGuardStateUseCase(
    private val repository: RoundRepository
) {
    suspend operator fun invoke(userId: Long): Result<CurrentStateResponseDto> {
        return repository.getCurrentState(userId)
    }
}
