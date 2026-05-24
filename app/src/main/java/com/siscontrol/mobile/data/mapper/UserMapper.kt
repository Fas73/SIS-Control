package com.siscontrol.mobile.data.mapper

import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.model.User

fun UserResponseDto.toDomain(): User {
    return User(
        id = this.id ?: 0L,
        rut = this.rut ?: "",
        username = this.username ?: "",
        email = this.email ?: "",
        fullName = this.fullName ?: "Usuario",
        role = this.role ?: "GUARD",
        phoneNumber = this.phoneNumber,
        imageUrl = this.imageUrl,
        status = this.status ?: 1,
        createdAt = this.createdAt
    )
}

fun User.toDto(): UserResponseDto {
    return UserResponseDto(
        id = this.id,
        rut = this.rut,
        username = this.username,
        email = this.email,
        fullName = this.fullName,
        role = this.role,
        phoneNumber = this.phoneNumber,
        imageUrl = this.imageUrl,
        status = this.status,
        createdAt = this.createdAt
    )
}

fun com.siscontrol.mobile.domain.model.UserCreationParam.toDto(): com.siscontrol.mobile.data.remote.dto.UserRequestDto {
    return com.siscontrol.mobile.data.remote.dto.UserRequestDto(
        rut = this.rut,
        username = this.username,
        email = this.email,
        fullName = this.fullName,
        password = this.password,
        phoneNumber = this.phoneNumber,
        role = this.role
    )
}

fun com.siscontrol.mobile.domain.model.ProfileUpdateParam.toDto(): com.siscontrol.mobile.data.remote.dto.ProfileUpdateRequest {
    return com.siscontrol.mobile.data.remote.dto.ProfileUpdateRequest(
        fullName = this.fullName,
        username = this.username,
        phoneNumber = this.phoneNumber
    )
}

fun com.siscontrol.mobile.domain.model.ChangePasswordParam.toDto(): com.siscontrol.mobile.data.remote.dto.ChangePasswordRequest {
    return com.siscontrol.mobile.data.remote.dto.ChangePasswordRequest(
        currentPassword = this.currentPassword,
        newPassword = this.newPassword
    )
}


