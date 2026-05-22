package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.domain.repository.InstallationRepository // <-- CAMBIADO AQUÍ

class GetCheckpointsUseCase(private val repository: InstallationRepository) {
    suspend operator fun invoke(installationId: Long): Result<List<CheckpointDto>> {
        return repository.getCheckpoints(installationId)
    }
}