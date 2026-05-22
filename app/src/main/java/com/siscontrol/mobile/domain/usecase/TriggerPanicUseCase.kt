package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.IncidentRepository

class TriggerPanicUseCase(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(roundId: Long, descripcion: String? = null): Result<Unit> {
        return repository.triggerPanic(roundId, descripcion)
    }
}
