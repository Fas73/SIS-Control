package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.AuthRepository

/**
 * Caso de Uso (Use Case) responsable de ejecutar la acción de Inicio de Sesión.
 * Encapsula la orquestación requerida por el negocio, manteniéndose libre de frameworks de UI o Red.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * El operador 'invoke' permite llamar a la instancia de la clase como si fuese una función.
     * Ej: loginUseCase(username, password)
     *
     * @return Result<LoginResult> con el JWT, rol y datos básicos del usuario en caso de éxito.
     */
    suspend operator fun invoke(username: String, password: String): Result<LoginResult> {
        // Validaciones de negocio antes de intentar ir a la red
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(Exception("El usuario y contraseña no pueden estar vacíos."))
        }
        
        // Se orquesta el uso del repositorio real
        return authRepository.login(username, password)
    }
}
