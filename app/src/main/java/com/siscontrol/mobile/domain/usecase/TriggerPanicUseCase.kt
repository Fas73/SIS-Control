package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.IncidentRepository

class TriggerPanicUseCase(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(roundId: Long? = null, shiftId: Long? = null, descripcion: String? = null): Result<Unit> {
        return repository.triggerPanic(roundId, shiftId, descripcion)
    }
}
