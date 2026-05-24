package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.CheckpointCreationParam
import com.siscontrol.mobile.domain.repository.InstallationRepository

class CreateCheckpointUseCase(private val repository: InstallationRepository) {
    suspend operator fun invoke(editorId: Long, request: CheckpointCreationParam): Result<Unit> {
        return repository.createCheckpoint(editorId, request)
    }
}