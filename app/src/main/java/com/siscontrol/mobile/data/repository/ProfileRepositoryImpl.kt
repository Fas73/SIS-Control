package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.ProfileApiService
import com.siscontrol.mobile.data.remote.dto.ChangePasswordRequest
import com.siscontrol.mobile.data.remote.dto.ProfileUpdateRequest
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val apiService: ProfileApiService
) : ProfileRepository {

    override suspend fun updateProfileData(id: Long, request: ProfileUpdateRequest): Result<UserResponseDto> {
        return try {
            val response = apiService.updateProfileData(id, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(id: Long, request: ChangePasswordRequest): Result<Unit> {
        return try {
            apiService.updatePassword(id, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
