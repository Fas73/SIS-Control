package com.siscontrol.mobile.domain.usecase

/**
 * Modelo simple del Dominio que encapsula el resultado exitoso de un login.
 *
 * Separarlo del DTO de red ([LoginResponse]) permite que el Dominio
 * permanezca independiente de los detalles de la capa de datos.
 *
 * @param token    JWT devuelto por el servidor.
 * @param role     Rol del usuario: "ADMIN", "SUPERVISOR" o "GUARD".
 * @param fullName Nombre completo para mostrar en la UI.
 * @param username Nombre de usuario autenticado.
 */
data class LoginResult(
    val token: String,
    val role: String,
    val fullName: String,
    val username: String
)
