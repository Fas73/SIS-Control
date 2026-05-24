package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.domain.repository.InstallationRepository

class GetCheckpointsUseCase(private val repository: InstallationRepository) {
    suspend operator fun invoke(installationId: Long): Result<List<Checkpoint>> {
        return repository.getCheckpoints(installationId)
    }
}