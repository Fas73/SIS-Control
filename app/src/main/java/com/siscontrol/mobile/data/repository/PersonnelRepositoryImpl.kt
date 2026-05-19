package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.PersonnelApiService
import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.repository.PersonnelRepository

class PersonnelRepositoryImpl(
    private val apiService: PersonnelApiService
) : PersonnelRepository {

    override suspend fun getPersonnel(): Result<List<UserResponseDto>> {
        return try {
            val response = apiService.getPersonnel()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: Long): Result<UserResponseDto> {
        return try {
            val response = apiService.getUserById(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPersonnel(creatorId: Long, request: UserRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.createPersonnel(creatorId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePersonnel(id: Long, editorId: Long, request: UserRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.updatePersonnel(id, editorId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleUserStatus(id: Long, editorId: Long): Result<Int> {
        return try {
            val response = apiService.toggleUserStatus(id, editorId)
            if (response.isSuccessful && response.body() != null) {
                // El backend devuelve UserResponseDTO, pero en toggle-status solemos extraer el status
                // Ajustado para manejar Map si es lo que devuelve el body según la interfaz
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
