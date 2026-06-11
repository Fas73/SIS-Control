package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.usecase.LoginResult

/**
 * Interfaz del Repositorio de Autenticación.
 * 
 * En Clean Architecture, el dominio no conoce los detalles de red o bases de datos (Retrofit/Room); 
 * solo define los contratos (Interfaces) que necesita usar para cumplir las reglas de negocio.
 */
interface AuthRepository {
    /**
     * Intenta autenticar un usuario usando sus credenciales.
     * Retorna Result<LoginResult> con el token, rol y datos básicos del usuario en caso exitoso.
     */
    suspend fun login(username: String, password: String): Result<LoginResult>

    /**
     * Solicita la recuperación de acceso para el correo electrónico indicado.
     */
    suspend fun solicitarRecuperacion(email: String): Result<String>

    /**
     * Verifica si el usuario (username o email) existe en el sistema.
     */
    suspend fun checkUsername(username: String): Result<Boolean>
}
