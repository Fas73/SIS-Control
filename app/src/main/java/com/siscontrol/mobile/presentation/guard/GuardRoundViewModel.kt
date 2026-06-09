package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.core.FirebaseStorageManager
import com.siscontrol.mobile.di.AppModule
import kotlinx.coroutines.launch

class GuardRoundViewModel(
    private val endRoundUseCase: EndRoundUseCase,
    private val getCheckpointsUseCase: GetCheckpointsUseCase,
    private val getRoundDetailUseCase: GetRoundDetailUseCase,
    private val scanCheckpointUseCase: ScanCheckpointUseCase,
    private val triggerPanicUseCase: TriggerPanicUseCase,
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val getCurrentGuardStateUseCase: GetCurrentGuardStateUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(GuardRoundState())
    val state: State<GuardRoundState> = _state

    // Motor de monitoreo remoto
    private var isMonitoring = false

    fun startRemoteMonitoring(userId: Long) {
        if (isMonitoring) return
        isMonitoring = true
        
        // 1. Monitoreo por Polling (Fallback cada 10s)
        viewModelScope.launch {
            while (isMonitoring) {
                checkRemoteStatus(userId)
                kotlinx.coroutines.delay(10000)
            }
        }

        // 2. Monitoreo REAL-TIME (WebSockets)
        viewModelScope.launch {
            com.siscontrol.mobile.core.StompService.adminAlertFlow.collect { incident ->
                val myFullName = sessionManager.getFullNameSync() ?: ""
                
                // Si el mensaje es para mí y es un cierre administrativo
                if (incident.roundExecution?.workerId == userId || incident.username == myFullName) {
                    if (incident.description.contains("[CANCELACIÓN ADMINISTRATIVA]") || 
                        incident.description.contains("[CIERRE AUTOMÁTICO]")) {
                        
                        android.util.Log.d("GUARD_ROUND_VM", "¡Cierre Real-time detectado!")
                        _state.value = _state.value.copy(
                            isTerminatedRemotely = true,
                            terminationReason = incident.description
                        )
                        isMonitoring = false
                    }
                }
            }
        }
    }

    private suspend fun checkRemoteStatus(userId: Long) {
        getCurrentGuardStateUseCase(userId).onSuccess { data ->
            // Si el servidor dice que no hay ronda activa pero nosotros estamos en esta pantalla
            if (data.rondaActiva == false) {
                _state.value = _state.value.copy(
                    isTerminatedRemotely = true,
                    terminationReason = data.ronda?.observations ?: "Ronda finalizada administrativamente"
                )
                isMonitoring = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isMonitoring = false
    }

    fun loadCheckpoints(installationId: Long, roundId: Long = 0L) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingCheckpoints = true)
            
            getCheckpointsUseCase(installationId)
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        checkpoints = list,
                        isLoadingCheckpoints = false
                    )
                    
                    if (roundId != 0L) {
                        fetchExecutedCheckpoints(roundId)
                    }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoadingCheckpoints = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun loadPastRoundDetail(roundId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            getRoundDetailUseCase(roundId).onSuccess { detail ->
                // 1. Cargamos de inmediato lo que ya tenemos (Incidentes y Observaciones)
                val scans = detail.escaneos ?: emptyList()
                val validScans = scans.filter { it.checkpoint?.id != null }
                val executedMap = validScans.associate { (it.checkpoint?.id ?: 0L) to (it.scannedAt ?: "S/H") }

                // Fusión de inteligencia: Si el servidor no mandó los nombres, los recuperamos de los escaneos
                val enrichedIncidents = detail.incidentes?.map { incident ->
                    if (incident.checkpointName.isNullOrBlank() && incident.checklogId != null) {
                        val matchingScan = scans.find { it.id == incident.checklogId }
                        incident.copy(
                            checkpointName = matchingScan?.checkpoint?.name,
                            checkpointOrder = matchingScan?.checkpoint?.executionOrder
                        )
                    } else {
                        incident
                    }
                } ?: emptyList()

                _state.value = _state.value.copy(
                    pastIncidents = enrichedIncidents,
                    terminationReason = detail.ronda?.observations ?: "Sin observaciones adicionales.",
                    executedCheckpointIds = executedMap.keys.filter { it > 0L }.toSet(),
                    scanTimes = executedMap
                )

                // 3. Generar Inteligencia de Ronda (IA)
                val totalCheckpoints = allCheckpointsSize(detail)
                val scannedCount = validScans.size
                val incidentsCount = enrichedIncidents.size
                val observations = detail.ronda?.observations ?: ""
                
                // Calcular duración real
                val duration = calculateDurationMinutes(detail.ronda?.startTime, detail.ronda?.endTime)
                
                val aiReport = com.siscontrol.mobile.core.AIManager.analyzeRoundPerformance(
                    totalCheckpoints = totalCheckpoints,
                    scannedCheckpoints = scannedCount,
                    incidentsCount = incidentsCount,
                    durationMinutes = duration,
                    observations = observations
                )
                
                _state.value = _state.value.copy(aiAnalysis = aiReport)

                // 4. Cargamos los nombres de los puntos de control para completar la vista
                val instId = detail.ronda?.installation?.id
                if (instId != null) {
                    getCheckpointsUseCase(instId).onSuccess { allCheckpoints ->
                        _state.value = _state.value.copy(
                            checkpoints = allCheckpoints,
                            isLoading = false
                        )
                    }.onFailure { 
                        _state.value = _state.value.copy(isLoading = false, error = "Carga parcial: No se pudieron obtener los nombres de los puntos.")
                    }
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = com.siscontrol.mobile.core.ErrorUtils.parse(e))
            }
        }
    }

    private fun fetchExecutedCheckpoints(roundId: Long) {
        // ... (Este método ya no es necesario llamarlo directamente desde la UI)
    }

    fun scanNfcTag(context: android.content.Context, roundId: Long, tagId: String, imageUri: android.net.Uri? = null, onVerificationSuccess: (CheckpointDto) -> Unit) {
        val nextCheckpoint = getNextCheckpoint()
        if (nextCheckpoint == null) {
            _state.value = _state.value.copy(error = "No hay más puntos por escanear")
            return
        }

        if (nextCheckpoint.nfcTagCode != tagId) {
            _state.value = _state.value.copy(error = "Etiqueta incorrecta. Debes escanear: ${nextCheckpoint.name}")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val now = java.time.LocalDateTime.now()
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            val location = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)

            var remoteUrl: String? = null
            if (imageUri != null) {
                FirebaseStorageManager.uploadImage(context, imageUri, "evidencias_ronda")
                    .onSuccess { remoteUrl = it }
                    .onFailure { e ->
                        // Si falla la subida de imagen, guardamos localmente para reintentar después
                        saveScanLocally(roundId, nextCheckpoint, tagId, "Falla subida imagen: ${e.message}", imageUri.toString(), location)
                        return@launch
                    }
            }

            scanCheckpointUseCase(
                roundId = roundId, 
                checkpointId = nextCheckpoint.id ?: 0L, 
                comment = "Escaneo NFC automático", 
                imageUrl = remoteUrl, 
                scannedAt = now,
                latitude = location?.latitude,
                longitude = location?.longitude
            )
                .onSuccess {
                    val newExecuted = _state.value.executedCheckpointIds + (nextCheckpoint.id ?: 0L)
                    val newScanTimes = _state.value.scanTimes + ((nextCheckpoint.id ?: 0L) to now)

                    _state.value = _state.value.copy(
                        executedCheckpointIds = newExecuted,
                        scanTimes = newScanTimes,
                        distanceTravelled = newExecuted.size * 150,
                        isLoading = false,
                        lastScannedCheckpoint = nextCheckpoint
                    )
                    onVerificationSuccess(nextCheckpoint)
                }
                .onFailure {
                    // ERROR DE RED -> Guardar en Room para sincronización diferida
                    saveScanLocally(roundId, nextCheckpoint, tagId, "Sin señal de internet", remoteUrl ?: imageUri?.toString(), location)
                }
        }
    }

    private fun saveScanLocally(roundId: Long, checkpoint: CheckpointDto, tagId: String, reason: String, imagePath: String?, location: android.location.Location?) {
        viewModelScope.launch {
            val now = java.time.LocalDateTime.now()
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            val entity = com.siscontrol.mobile.data.local.entities.PendingScanEntity(
                roundId = roundId,
                checkpointId = checkpoint.id ?: 0L,
                notes = "Escaneo Offline ($reason)",
                status = 1,
                scannedAt = now,
                imageUrl = imagePath,
                latitude = location?.latitude,
                longitude = location?.longitude
            )
            AppModule.getDatabase().pendingScanDao().insertScan(entity)
            
            // Actualizar UI para mostrar progreso aunque sea offline
            val newExecuted = _state.value.executedCheckpointIds + (checkpoint.id ?: 0L)
            val newScanTimes = _state.value.scanTimes + ((checkpoint.id ?: 0L) to now)
            
            _state.value = _state.value.copy(
                executedCheckpointIds = newExecuted,
                scanTimes = newScanTimes,
                isLoading = false,
                successMessage = "Punto guardado localmente (Sin señal)"
            )
        }
    }

    fun triggerPanicAlert(roundId: Long) {
        viewModelScope.launch {
            triggerPanicUseCase(roundId = roundId, descripcion = "Solicitud de ayuda inmediata desde el botón de pánico.")
                .onSuccess {
                    _state.value = _state.value.copy(successMessage = "Alerta de pánico activada. Los supervisores han sido notificados.")
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = "Falla al activar pánico: ${e.message}")
                }
        }
    }

    fun skipCheckpoint(context: android.content.Context, roundId: Long, reason: String, imageUri: android.net.Uri?, onFinish: () -> Unit) {
        val nextCheckpoint = getNextCheckpoint() ?: return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var now = ""
            var location: android.location.Location? = null
            var remoteUrl: String? = null
            
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }

                now = java.time.LocalDateTime.now()
                    .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                location = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)

                // 1. PROCESAMIENTO ULTRA-RÁPIDO (Imagen pequeña para subir en segundos)
                if (imageUri != null) {
                    try {
                        val imageBytes = com.siscontrol.mobile.core.ImageUtils.processImageForUpload(context, imageUri, "OMISIÓN: ${nextCheckpoint.name}")
                        if (imageBytes != null) {
                            // Subida con tiempo límite de 25 segundos
                            val uploadResult = FirebaseStorageManager.uploadBytes(imageBytes, "evidencias")
                            remoteUrl = uploadResult.getOrNull()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ROUND_VM", "Error procesando imagen de omisión: ${e.message}")
                    }
                }

                // 2. INTENTO DE GUARDADO ONLINE DIRECTO
                kotlinx.coroutines.withTimeout(30000) {
                    val scanResult = scanCheckpointUseCase(
                        roundId = roundId, 
                        checkpointId = nextCheckpoint.id ?: 0L, 
                        comment = reason, 
                        status = 2, 
                        imageUrl = remoteUrl, 
                        scannedAt = now,
                        latitude = location?.latitude,
                        longitude = location?.longitude
                    )

                    if (scanResult.isSuccess) {
                        val checklogId = scanResult.getOrNull()
                        val incidentReport = IncidentDto(
                            id = null,
                            title = "ALERTA: Checkpoint no escaneado",
                            description = "Justificación: $reason",
                            severity = "Media",
                            type = "MANTENCION",
                            roundExecutionId = roundId,
                            checklogId = checklogId,
                            imageUrl = remoteUrl,
                            clientTimestamp = now,
                            latitude = location?.latitude ?: 0.0,
                            longitude = location?.longitude ?: 0.0,
                            checkpointName = nextCheckpoint.name,
                            checkpointOrder = nextCheckpoint.executionOrder,
                            status = 0
                        )

                        // LOG PARA BACKEND
                        val jsonLog = com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(incidentReport)
                        android.util.Log.d("CRITICAL_FRONTEND", "🚀 ENVIANDO ALERTA DE OMISIÓN: $jsonLog")

                        reportIncidentUseCase(incidentReport)
                        
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            updateRoundStateLocally(nextCheckpoint, now)
                            onFinish()
                        }
                    } else {
                        val e = scanResult.exceptionOrNull()
                        android.util.Log.e("CRITICAL_FRONTEND", "❌ EL SERVIDOR RECHAZÓ EL ESCANEO. Error: ${e?.message}")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Error del servidor: ${e?.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("ROUND_VM", "Fallo flujo online. Guardando localmente.")
                handleSkipOffline(roundId, nextCheckpoint, reason, remoteUrl ?: imageUri?.toString(), location, now, onFinish)
            }
        }
    }

    private fun handleSkipOffline(roundId: Long, checkpoint: CheckpointDto, reason: String, path: String?, loc: android.location.Location?, time: String, onFinish: () -> Unit) {
        saveScanLocally(roundId, checkpoint, checkpoint.nfcTagCode ?: "", reason, path, loc)
        saveIncidentLocally(
            "ALERTA: Checkpoint no escaneado", 
            "Justificacion: $reason", 
            "MEDIA", "MANTENCION", 
            roundId, path, loc, time, 
            checkpoint.name, checkpoint.executionOrder
        )
        onFinish()
    }

    private fun updateRoundStateLocally(checkpoint: CheckpointDto, timestamp: String) {
        val newExecuted = _state.value.executedCheckpointIds + (checkpoint.id ?: 0L)
        val newScanTimes = _state.value.scanTimes + ((checkpoint.id ?: 0L) to timestamp)

        _state.value = _state.value.copy(
            executedCheckpointIds = newExecuted,
            scanTimes = newScanTimes,
            distanceTravelled = newExecuted.size * 150,
            isLoading = false
        )
    }

    private fun saveIncidentLocally(
        title: String,
        description: String,
        severity: String,
        type: String,
        roundId: Long?,
        localPath: String?,
        location: android.location.Location?,
        timestamp: String,
        cpName: String? = null,
        cpOrder: Int? = null
    ) {
        viewModelScope.launch {
            val entity = com.siscontrol.mobile.data.local.entities.PendingIncidentEntity(
                title = title,
                description = description,
                severity = severity,
                type = type,
                roundExecutionId = roundId,
                checklogId = null,
                localImageUri = localPath,
                clientTimestamp = timestamp,
                latitude = location?.latitude,
                longitude = location?.longitude,
                checkpointName = cpName,
                checkpointOrder = cpOrder
            )
            AppModule.getDatabase().pendingIncidentDao().insertIncident(entity)
        }
    }

    private fun allCheckpointsSize(detail: com.siscontrol.mobile.data.remote.dto.RoundDetailResponseDto): Int {
        // Fallback si el backend no mandó el contador: sumamos ejecutados + faltantes (si los supiéramos)
        // Por ahora usamos los que el ViewModel cargará después o los que ya están en el estado
        return _state.value.checkpoints.size.coerceAtLeast(detail.escaneos?.size ?: 0)
    }

    private fun calculateDurationMinutes(start: String?, end: String?): Long {
        if (start == null || end == null) return 0
        return try {
            val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
            val startDt = java.time.LocalDateTime.parse(start, formatter)
            val endDt = java.time.LocalDateTime.parse(end, formatter)
            java.time.Duration.between(startDt, endDt).toMinutes()
        } catch (e: Exception) {
            15 // Valor por defecto razonable
        }
    }

    fun endRound(roundId: Long, observations: String, onSuccess: () -> Unit) {
        isMonitoring = false // Detener monitoreo remoto para no mostrar diálogo administrativo
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            endRoundUseCase(roundId, observations)
                .onSuccess {
                    // sessionManager.clearActiveSession() ya no se llama aquí, o sí? 
                    // En realidad, clearActiveSession limpia toda la jornada? No, clearActiveRound?
                    // El sessionManager debería manejar solo la ronda. Pero mantenemos el código original.
                    sessionManager.clearActiveSession()
                    _state.value = _state.value.copy(isLoading = false, isTerminatedLocally = true)
                    // Ya no llamamos a onSuccess() inmediatamente, se llamará al cerrar el diálogo
                }
                .onFailure { e ->
                    isMonitoring = true // Reanudar si falló
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Error al finalizar ronda: ${com.siscontrol.mobile.core.ErrorUtils.parse(e)}"
                    )
                }
        }
    }

    private fun getNextCheckpoint(): CheckpointDto? {
        return _state.value.checkpoints
            .sortedBy { it.executionOrder }
            .firstOrNull { it.id !in _state.value.executedCheckpointIds }
    }

    fun getMinutesPassed(): Int {
        val diff = System.currentTimeMillis() - _state.value.startTime
        return (diff / 60000).toInt()
    }

    /**
     * Usa la IA para generar un resumen profesional de la ronda.
     */
    fun generateProfessionalSummary(currentNotes: String, onResult: (String) -> Unit) {
        val executedCount = _state.value.executedCheckpointIds.size
        val totalCount = _state.value.checkpoints.size
        val minutes = getMinutesPassed()
        
        // Usamos el motor de inteligencia para generar el análisis de desempeño
        val analysis = com.siscontrol.mobile.core.AIManager.analyzeRoundPerformance(
            totalCheckpoints = totalCount,
            scannedCheckpoints = executedCount,
            incidentsCount = _state.value.pastIncidents.size, // Podría sumar pendientes si los hay
            durationMinutes = minutes.toLong(),
            observations = currentNotes
        )
        
        // Combinamos la redacción profesional con el análisis
        val professionalDescription = com.siscontrol.mobile.core.AIManager.generateProfessionalDescription(
            userInput = currentNotes,
            labels = emptyList(), // No hay fotos aquí
            hasPhoto = false
        )

        val finalReport = "--- REPORTE FINAL DE RONDA ---\n\n$professionalDescription\n\n$analysis"
        
        onResult(finalReport)
    }

    fun getUserIdSync(): Long {
        return sessionManager.getUserIdSync() ?: 0L
    }
}

data class GuardRoundState(
    val isLoading: Boolean = false,
    val isLoadingCheckpoints: Boolean = false,
    val checkpoints: List<CheckpointDto> = emptyList(),
    val executedCheckpointIds: Set<Long> = emptySet(),
    val scanTimes: Map<Long, String> = emptyMap(),
    val distanceTravelled: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val lastScannedCheckpoint: CheckpointDto? = null,
    val successMessage: String? = null,
    val error: String? = null,
    val isTerminatedRemotely: Boolean = false,
    val isTerminatedLocally: Boolean = false,
    val terminationReason: String? = null,
    val pastIncidents: List<IncidentDto> = emptyList(),
    val aiAnalysis: String? = null
)
