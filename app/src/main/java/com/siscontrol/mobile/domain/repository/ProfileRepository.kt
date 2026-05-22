package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.ChangePasswordRequest
import com.siscontrol.mobile.data.remote.dto.ProfileUpdateRequest
import com.siscontrol.mobile.data.remote.dto.UserResponseDto

interface ProfileRepository {
    suspend fun updateProfileData(id: Long, request: ProfileUpdateRequest): Result<UserResponseDto>
    suspend fun updatePassword(id: Long, request: ChangePasswordRequest): Result<Unit>
}
