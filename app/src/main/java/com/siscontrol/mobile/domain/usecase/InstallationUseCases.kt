package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.Installation
import com.siscontrol.mobile.domain.model.InstallationCreationParam
import com.siscontrol.mobile.domain.repository.InstallationRepository

class GetInstallationsUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(): Result<List<Installation>> {
        return repository.getInstallations()
    }
}

class CreateInstallationUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(editorId: Long, request: InstallationCreationParam): Result<Unit> {
        return repository.createInstallation(editorId, request)
    }
}

class UpdateInstallationUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long, request: Installation): Result<Unit> {
        return repository.updateInstallation(id, editorId, request)
    }
}

class ToggleInstallationStatusUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long): Result<Int> {
        return repository.toggleInstallationStatus(id, editorId)
    }
}

