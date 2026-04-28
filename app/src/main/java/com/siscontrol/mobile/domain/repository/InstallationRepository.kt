package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationRequestDto

interface InstallationRepository {
    suspend fun getInstallations(): Result<List<InstallationDto>>
    suspend fun createInstallation(request: InstallationRequestDto): Result<InstallationDto>
    suspend fun getCheckpoints(installationId: Long): Result<List<CheckpointDto>>
    suspend fun createCheckpoint(installationId: Long, request: CheckpointRequestDto): Result<CheckpointDto>
}
