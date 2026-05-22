package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.*

interface InstallationRepository {
    suspend fun getInstallations(): Result<List<InstallationDto>>

    suspend fun createInstallation(editorId: Long, request: InstallationRequestDto): Result<Unit>

    suspend fun updateInstallation(id: Long, editorId: Long, request: InstallationDto): Result<Unit>

    suspend fun toggleInstallationStatus(id: Long, editorId: Long): Result<Int>

    suspend fun getCheckpoints(installationId: Long): Result<List<CheckpointDto>>

    suspend fun createCheckpoint(editorId: Long, request: CheckpointRequestDto): Result<Unit>

    suspend fun updateCheckpoint(id: Long, editorId: Long, request: CheckpointDto): Result<Unit>

    suspend fun toggleCheckpointStatus(id: Long, editorId: Long): Result<Int>
}
