package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.ChangePasswordRequest
import com.siscontrol.mobile.data.remote.dto.ProfileUpdateRequest
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.repository.ProfileRepository

class UpdateProfileDataUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: Long, request: ProfileUpdateRequest): Result<UserResponseDto> {
        return repository.updateProfileData(id, request)
    }
}

class ChangeMyPasswordUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: Long, request: ChangePasswordRequest): Result<Unit> {
        return repository.updatePassword(id, request)
    }
}

class UpdateProfileImageUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: Long, imageUrl: String): Result<UserResponseDto> {
        return repository.updateProfileImage(id, imageUrl)
    }
}
