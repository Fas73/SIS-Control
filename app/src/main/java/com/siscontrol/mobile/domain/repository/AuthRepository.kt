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
}
