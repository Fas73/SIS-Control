package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.User
import com.siscontrol.mobile.domain.model.UserCreationParam

/**
 * Contrato del repositorio para la gestión del personal/usuarios.
 * Define operaciones en base a modelos de dominio, aislando la lógica de negocio de la capa de datos.
 */
interface PersonnelRepository {
    suspend fun getPersonnel(): Result<List<User>>
    suspend fun getUserById(id: Long): Result<User>
    suspend fun createPersonnel(creatorId: Long, request: UserCreationParam): Result<User>
    suspend fun updatePersonnel(id: Long, editorId: Long, request: UserCreationParam): Result<User>
    suspend fun toggleUserStatus(id: Long, editorId: Long): Result<Int>
}

