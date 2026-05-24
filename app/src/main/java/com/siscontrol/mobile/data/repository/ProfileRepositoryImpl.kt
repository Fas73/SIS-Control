package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.ProfileApiService
import com.siscontrol.mobile.domain.model.ProfileUpdateParam
import com.siscontrol.mobile.domain.model.ChangePasswordParam
import com.siscontrol.mobile.domain.model.User
import com.siscontrol.mobile.data.mapper.toDto
import com.siscontrol.mobile.data.mapper.toDomain
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val apiService: ProfileApiService
) : ProfileRepository {

    override suspend fun updateProfileData(id: Long, request: ProfileUpdateParam): Result<User> {
        return try {
            val response = apiService.updateProfileData(id, request.toDto())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(id: Long, request: ChangePasswordParam): Result<Unit> {
        return try {
            apiService.updatePassword(id, request.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
