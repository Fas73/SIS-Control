package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.RoundRepository

class CancelShiftUseCase(
    private val repository: RoundRepository
) {
    suspend operator fun invoke(shiftId: Long, adminId: Long, motivo: String?): Result<Unit> {
        return repository.cancelShift(shiftId, adminId, motivo)
    }
}
