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

    override suspend fun createPersonnel(request: UserRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.createPersonnel(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
