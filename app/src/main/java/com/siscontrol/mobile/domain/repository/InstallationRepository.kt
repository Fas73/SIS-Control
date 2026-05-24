package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.domain.model.CheckpointCreationParam
import com.siscontrol.mobile.domain.model.Installation
import com.siscontrol.mobile.domain.model.InstallationCreationParam

/**
 * Contrato de repositorio para instalaciones y checkpoints.
 * Operaciones puras de negocio utilizando modelos de dominio limpios.
 */
interface InstallationRepository {
    suspend fun getInstallations(): Result<List<Installation>>

    suspend fun createInstallation(editorId: Long, request: InstallationCreationParam): Result<Unit>

    suspend fun updateInstallation(id: Long, editorId: Long, request: Installation): Result<Unit>

    suspend fun toggleInstallationStatus(id: Long, editorId: Long): Result<Int>

    suspend fun getCheckpoints(installationId: Long): Result<List<Checkpoint>>

    suspend fun createCheckpoint(editorId: Long, request: CheckpointCreationParam): Result<Unit>

    suspend fun updateCheckpoint(id: Long, editorId: Long, request: Checkpoint): Result<Unit>

    suspend fun toggleCheckpointStatus(id: Long, editorId: Long): Result<Int>
}

