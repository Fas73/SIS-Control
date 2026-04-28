package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.InstallationApiService
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationRequestDto
import com.siscontrol.mobile.domain.repository.InstallationRepository

class InstallationRepositoryImpl(
    private val apiService: InstallationApiService
) : InstallationRepository {

    override suspend fun getInstallations(): Result<List<InstallationDto>> {
        return try {
            val response = apiService.getInstallations()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createInstallation(request: InstallationRequestDto): Result<InstallationDto> {
        return try {
            val response = apiService.createInstallation(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCheckpoints(installationId: Long): Result<List<CheckpointDto>> {
        return try {
            val response = apiService.getCheckpoints(installationId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCheckpoint(installationId: Long, request: CheckpointRequestDto): Result<CheckpointDto> {
        return try {
            val response = apiService.createCheckpoint(installationId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
