package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.InstallationApiService
import com.siscontrol.mobile.domain.repository.InstallationRepository
import com.siscontrol.mobile.data.remote.dto.*

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

    override suspend fun createInstallation(editorId: Long, request: InstallationRequestDto): Result<Unit> {
        return try {
            val response = apiService.createInstallation(editorId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al crear instalación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateInstallation(id: Long, editorId: Long, request: InstallationDto): Result<Unit> {
        return try {
            val response = apiService.updateInstallation(id, editorId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar instalación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleInstallationStatus(id: Long, editorId: Long): Result<Int> {
        return try {
            val response = apiService.toggleInstallationStatus(id, editorId)
            if (response.isSuccessful && response.body() != null) {
                val statusValue = (response.body()!!["status"] as? Number)?.toInt() ?: 0
                Result.success(statusValue)
            } else {
                Result.failure(Exception("Error al alternar estado: ${response.code()}"))
            }
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

    override suspend fun createCheckpoint(editorId: Long, request: CheckpointRequestDto): Result<Unit> {
        return try {
            val response = apiService.createCheckpoint(editorId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al crear checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCheckpoint(id: Long, editorId: Long, request: CheckpointDto): Result<Unit> {
        return try {
            android.util.Log.d("SIS_CONTROL_REPO", "PUT Checkpoint ID: $id, Editor: $editorId, Body: $request")
            val response = apiService.updateCheckpoint(id, editorId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                android.util.Log.e("SIS_CONTROL_REPO", "Update failed: ${response.code()} - ${response.errorBody()?.string()}")
                Result.failure(Exception("Error al actualizar checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("SIS_CONTROL_REPO", "Update exception", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleCheckpointStatus(id: Long, editorId: Long): Result<Int> {
        return try {
            android.util.Log.d("SIS_CONTROL_REPO", "Toggle Checkpoint ID: $id, Editor: $editorId")
            val response = apiService.toggleCheckpointStatus(id, editorId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                android.util.Log.d("SIS_CONTROL_REPO", "Toggle success body: $body")
                val statusValue = (body["status"] as? Number)?.toInt() ?: 0
                Result.success(statusValue)
            } else {
                android.util.Log.e("SIS_CONTROL_REPO", "Toggle failed code: ${response.code()} error: ${response.errorBody()?.string()}")
                Result.failure(Exception("Error al alternar estado del checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("SIS_CONTROL_REPO", "Toggle exception", e)
            Result.failure(e)
        }
    }
}
