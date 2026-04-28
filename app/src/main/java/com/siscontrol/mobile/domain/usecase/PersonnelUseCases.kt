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

class CreatePersonnelUseCase(
    private val repository: PersonnelRepository
) {
    suspend operator fun invoke(request: UserRequestDto): Result<UserResponseDto> {
        return repository.createPersonnel(request)
    }
}
