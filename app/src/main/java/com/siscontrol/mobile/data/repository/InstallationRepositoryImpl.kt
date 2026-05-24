package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.InstallationApiService
import com.siscontrol.mobile.data.mapper.toDomain
import com.siscontrol.mobile.data.mapper.toDto
import com.siscontrol.mobile.domain.model.Installation
import com.siscontrol.mobile.domain.model.InstallationCreationParam
import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.domain.model.CheckpointCreationParam
import com.siscontrol.mobile.domain.repository.InstallationRepository

/**
 * Implementación del repositorio de instalaciones.
 * Convierte los DTOs de la capa de datos a entidades de dominio puras mediante los mapeadores.
 */
class InstallationRepositoryImpl(
    private val apiService: InstallationApiService
) : InstallationRepository {

    override suspend fun getInstallations(): Result<List<Installation>> {
        return try {
            val response = apiService.getInstallations()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createInstallation(editorId: Long, request: InstallationCreationParam): Result<Unit> {
        return try {
            val response = apiService.createInstallation(editorId, request.toDto())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al crear instalación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateInstallation(id: Long, editorId: Long, request: Installation): Result<Unit> {
        return try {
            val response = apiService.updateInstallation(id, editorId, request.toDto())
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

    override suspend fun getCheckpoints(installationId: Long): Result<List<Checkpoint>> {
        return try {
            val response = apiService.getCheckpoints(installationId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCheckpoint(editorId: Long, request: CheckpointCreationParam): Result<Unit> {
        return try {
            val response = apiService.createCheckpoint(editorId, request.toDto())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al crear checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCheckpoint(id: Long, editorId: Long, request: Checkpoint): Result<Unit> {
        return try {
            val response = apiService.updateCheckpoint(id, editorId, request.toDto())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleCheckpointStatus(id: Long, editorId: Long): Result<Int> {
        return try {
            val response = apiService.toggleCheckpointStatus(id, editorId)
            if (response.isSuccessful && response.body() != null) {
                val statusValue = (response.body()!!["status"] as? Number)?.toInt() ?: 0
                Result.success(statusValue)
            } else {
                Result.failure(Exception("Error al alternar estado del checkpoint: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
