package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.ChangePasswordParam
import com.siscontrol.mobile.domain.model.ProfileUpdateParam
import com.siscontrol.mobile.domain.model.User
import com.siscontrol.mobile.domain.repository.ProfileRepository

class UpdateProfileDataUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: Long, request: ProfileUpdateParam): Result<User> {
        return repository.updateProfileData(id, request)
    }
}

class ChangeMyPasswordUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: Long, request: ChangePasswordParam): Result<Unit> {
        return repository.updatePassword(id, request)
    }
}

