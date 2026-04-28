package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
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
    suspend operator fun invoke(request: InstallationRequestDto): Result<InstallationDto> {
        return repository.createInstallation(request)
    }
}

class GetCheckpointsUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(installationId: Long): Result<List<CheckpointDto>> {
        return repository.getCheckpoints(installationId)
    }
}

class CreateCheckpointUseCase(
    private val repository: InstallationRepository
) {
    suspend operator fun invoke(installationId: Long, request: CheckpointRequestDto): Result<CheckpointDto> {
        return repository.createCheckpoint(installationId, request)
    }
}
