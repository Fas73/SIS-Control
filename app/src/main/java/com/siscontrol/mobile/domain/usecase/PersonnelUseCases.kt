package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.repository.PersonnelRepository

class GetPersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(): Result<List<UserResponseDto>> {
        return repository.getPersonnel()
    }
}

class GetUserByIdUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(id: Long): Result<UserResponseDto> {
        return repository.getUserById(id)
    }
}

class CreatePersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(creatorId: Long, request: UserRequestDto): Result<UserResponseDto> {
        return repository.createPersonnel(creatorId, request)
    }
}

class UpdatePersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(id: Long, editorId: Long, request: UserRequestDto): Result<UserResponseDto> {
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
