package com.siscontrol.mobile.core

import android.content.Context
import android.net.Uri
import com.siscontrol.mobile.data.local.AppDatabase
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.repository.RoundRepository
import com.siscontrol.mobile.domain.repository.IncidentRepository
import com.siscontrol.mobile.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Gestor de sincronización de datos offline.
 */
class SyncManager(
    private val context: Context,
    private val roundRepository: RoundRepository,
    private val incidentRepository: IncidentRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val db = AppDatabase.getDatabase(context.applicationContext)

    fun startSync() {
        scope.launch {
            android.util.Log.d("SYNC", "Iniciando sincronización de datos pendientes...")
            syncCheckpoints()
            syncIncidents()
        }
    }

    private suspend fun syncCheckpoints() {
        val unsynced = db.pendingScanDao().getUnsyncedScans()
        if (unsynced.isEmpty()) return

        unsynced.forEach { scan ->
            var finalImageUrl = scan.imageUrl
            
            // --- MEJORA: Subir imagen de escaneo si es local ---
            if (scan.imageUrl != null && !scan.imageUrl.startsWith("http")) {
                val uri = Uri.parse(scan.imageUrl)
                FirebaseStorageManager.uploadImage(context, uri, "evidencias_ronda")
                    .onSuccess { finalImageUrl = it }
            }

            roundRepository.scanCheckpoint(
                scan.roundId, scan.checkpointId, scan.notes, scan.status, finalImageUrl, scan.scannedAt, scan.latitude, scan.longitude
            ).onSuccess {
                db.pendingScanDao().deleteScan(scan)
            }.onFailure { e ->
                val msg = e.message ?: ""
                // Solo borramos si es un conflicto real de duplicidad (409)
                if (msg.contains("409") || msg.contains("ya está registrado", ignoreCase = true)) {
                    android.util.Log.w("SYNC", "Borrando escaneo duplicado: ${scan.localId}")
                    db.pendingScanDao().deleteScan(scan)
                } else {
                    android.util.Log.e("SYNC", "Falla persistente en escaneo ${scan.localId}: $msg. Se mantiene local.")
                }
            }
        }
    }

    private suspend fun syncIncidents() {
        val unsynced = db.pendingIncidentDao().getUnsyncedIncidents()
        if (unsynced.isEmpty()) return

        unsynced.forEach { inc ->
            var finalImageUrl = inc.localImageUri
            
            if (inc.localImageUri != null && !inc.localImageUri.startsWith("http")) {
                val uri = Uri.parse(inc.localImageUri)
                FirebaseStorageManager.uploadImage(context, uri, "evidencias")
                    .onSuccess { finalImageUrl = it }
            }

            val dto = IncidentDto(
                id = null,
                title = inc.title,
                description = inc.description,
                severity = inc.severity,
                type = inc.type,
                roundExecutionId = inc.roundExecutionId,
                checklogId = inc.checklogId,
                imageUrl = finalImageUrl,
                clientTimestamp = inc.clientTimestamp,
                latitude = inc.latitude,
                longitude = inc.longitude,
                checkpointName = inc.checkpointName,
                checkpointOrder = inc.checkpointOrder,
                status = 0
            )

            AppModule.reportIncidentUseCase(dto)
                .onSuccess {
                    db.pendingIncidentDao().deleteIncident(inc)
                }
                .onFailure { e ->
                    val msg = e.message ?: ""
                    // Solo borramos si es un conflicto (409). 
                    if (msg.contains("409")) {
                        android.util.Log.w("SYNC", "Borrando incidente conflictivo: ${inc.localId}")
                        db.pendingIncidentDao().deleteIncident(inc)
                    } else {
                        android.util.Log.e("SYNC", "Falla persistente en incidente ${inc.localId}: $msg. Se mantiene local.")
                    }
                }
        }
    }
}
