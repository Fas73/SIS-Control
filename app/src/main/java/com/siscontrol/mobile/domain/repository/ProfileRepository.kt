package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.ChangePasswordParam
import com.siscontrol.mobile.domain.model.ProfileUpdateParam
import com.siscontrol.mobile.domain.model.User

/**
 * Contrato de repositorio para la gestión del perfil del usuario logueado.
 * Utiliza exclusivamente modelos de dominio.
 */
interface ProfileRepository {
    suspend fun updateProfileData(id: Long, request: ProfileUpdateParam): Result<User>
    suspend fun updatePassword(id: Long, request: ChangePasswordParam): Result<Unit>
}

