package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationRequestDto
import com.siscontrol.mobile.domain.repository.InstallationRepository

class GetInstallationsUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(): Result<List<InstallationDto>> {
        return repository.getInstallations()
    }
}

class CreateInstallationUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(editorId: Long, request: InstallationRequestDto): Result<Unit> {
        return repository.createInstallation(editorId, request)
    }
}

class UpdateInstallationUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long, request: InstallationDto): Result<Unit> {
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
