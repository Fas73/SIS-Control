package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto

interface PersonnelRepository {
    suspend fun getPersonnel(): Result<List<UserResponseDto>>
    suspend fun getUserById(id: Long): Result<UserResponseDto>
    suspend fun createPersonnel(creatorId: Long, request: UserRequestDto): Result<UserResponseDto>
    suspend fun updatePersonnel(id: Long, editorId: Long, request: UserRequestDto): Result<UserResponseDto>
    suspend fun toggleUserStatus(id: Long, editorId: Long): Result<Int>
}
