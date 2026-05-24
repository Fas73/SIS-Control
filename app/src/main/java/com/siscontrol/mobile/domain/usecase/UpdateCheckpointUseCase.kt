package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.domain.repository.InstallationRepository

class UpdateCheckpointUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long, request: Checkpoint): Result<Unit> {
        return repository.updateCheckpoint(id, editorId, request)
    }
}

class ToggleCheckpointStatusUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long): Result<Int> {
        return repository.toggleCheckpointStatus(id, editorId)
    }
}

