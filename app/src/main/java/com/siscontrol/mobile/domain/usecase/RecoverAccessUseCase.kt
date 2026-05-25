package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.AuthRepository

/**
 * Caso de uso para solicitar la recuperación de acceso.
 */
class RecoverAccessUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<String> {
        return repository.solicitarRecuperacion(email)
    }
}
