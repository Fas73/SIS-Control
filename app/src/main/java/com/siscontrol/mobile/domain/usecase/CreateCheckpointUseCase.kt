package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.domain.repository.InstallationRepository // <-- CAMBIADO AQUÍ

class CreateCheckpointUseCase(private val repository: InstallationRepository) {
    suspend operator fun invoke(request: CheckpointRequestDto): Result<Unit> {
        return repository.createCheckpoint(request)
    }
}