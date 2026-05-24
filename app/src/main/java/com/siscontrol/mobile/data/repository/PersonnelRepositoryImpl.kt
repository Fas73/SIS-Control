package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.PersonnelApiService
import com.siscontrol.mobile.data.mapper.toDomain
import com.siscontrol.mobile.data.mapper.toDto
import com.siscontrol.mobile.domain.model.User
import com.siscontrol.mobile.domain.model.UserCreationParam
import com.siscontrol.mobile.domain.repository.PersonnelRepository

/**
 * Implementación del repositorio de gestión de personal.
 * Realiza las llamadas de red mediante [PersonnelApiService] y traduce los
 * DTOs de red a entidades de dominio puro utilizando funciones de mapeo.
 */
class PersonnelRepositoryImpl(
    private val apiService: PersonnelApiService
) : PersonnelRepository {

    override suspend fun getPersonnel(): Result<List<User>> {
        return try {
            val response = apiService.getPersonnel()
            // Convertimos la lista de DTOs de respuesta a entidades de dominio User
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: Long): Result<User> {
        return try {
            val response = apiService.getUserById(id)
            // Convertimos el DTO a entidad de dominio User
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPersonnel(creatorId: Long, request: UserCreationParam): Result<User> {
        return try {
            // Mapeamos los parámetros de dominio al DTO de request esperado por la API
            val response = apiService.createPersonnel(creatorId, request.toDto())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePersonnel(id: Long, editorId: Long, request: UserCreationParam): Result<User> {
        return try {
            // Mapeamos los parámetros de dominio al DTO de request esperado por la API
            val response = apiService.updatePersonnel(id, editorId, request.toDto())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleUserStatus(id: Long, editorId: Long): Result<Int> {
        return try {
            val response = apiService.toggleUserStatus(id, editorId)
            if (response.isSuccessful && response.body() != null) {
                val statusValue = (response.body()!!["status"] as? Number)?.toInt() ?: 0
                Result.success(statusValue)
            } else {
                Result.failure(Exception("Error al alternar estado del usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

