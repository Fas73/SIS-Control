package com.siscontrol.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar escaneos NFC de forma local cuando no hay señal de internet.
 * Garantiza que la hora de chequeo sea la del dispositivo y no se pierda.
 */
@Entity(tableName = "pending_scans")
data class PendingScanEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val roundId: Long,
    val checkpointId: Long,
    val notes: String,
    val status: Int, // 1: Físico, 2: Omitido
    val scannedAt: String, // Timestamp ISO del momento exacto del escaneo
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSynced: Boolean = false
)
