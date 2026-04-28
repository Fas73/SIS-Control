package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto

interface PersonnelRepository {
    suspend fun getPersonnel(): Result<List<UserResponseDto>>
    suspend fun createPersonnel(request: UserRequestDto): Result<UserResponseDto>
}
