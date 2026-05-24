package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.User
import com.siscontrol.mobile.domain.model.UserCreationParam
import com.siscontrol.mobile.domain.repository.PersonnelRepository

class GetPersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        return repository.getPersonnel()
    }
}

class GetUserByIdUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(id: Long): Result<User> {
        return repository.getUserById(id)
    }
}

class CreatePersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(creatorId: Long, request: UserCreationParam): Result<User> {
        return repository.createPersonnel(creatorId, request)
    }
}

class UpdatePersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long, request: UserCreationParam): Result<User> {
        return repository.updatePersonnel(id, editorId, request)
    }
}

class ToggleUserStatusUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long): Result<Int> {
        return repository.toggleUserStatus(id, editorId)
    }
}

