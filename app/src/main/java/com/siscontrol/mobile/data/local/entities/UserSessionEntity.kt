package com.siscontrol.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la sesión del usuario actual en la base de datos local.
 * Se usa para tener acceso rápido (Room) al ID y perfil mientras la app está abierta.
 */
@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val status: String
)
