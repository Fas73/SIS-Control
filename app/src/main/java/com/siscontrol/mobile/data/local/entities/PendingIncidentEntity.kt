package com.siscontrol.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar reportes de incidentes de forma local cuando no hay señal.
 */
@Entity(tableName = "pending_incidents")
data class PendingIncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val title: String,
    val description: String,
    val severity: String,
    val type: String,
    val roundExecutionId: Long?,
    val checklogId: Long?,
    val localImageUri: String?, // Ruta del archivo local en el dispositivo
    val clientTimestamp: String,
    val latitude: Double?,
    val longitude: Double?,
    val checkpointName: String? = null,
    val checkpointOrder: Int? = null,
    val isSynced: Boolean = false
)
