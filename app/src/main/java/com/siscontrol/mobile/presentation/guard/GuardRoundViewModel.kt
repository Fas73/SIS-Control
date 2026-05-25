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
        viewModelScope.launch {
            while (isMonitoring) {
                checkRemoteStatus(userId)
                kotlinx.coroutines.delay(10000) // Verificar cada 10 segundos
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

                // 2. Cargamos los nombres de los puntos de control para completar la vista
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

    fun scanNfcTag(roundId: Long, tagId: String, onVerificationSuccess: (CheckpointDto) -> Unit) {
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
            scanCheckpointUseCase(roundId, nextCheckpoint.id ?: 0L, "Escaneo NFC automático")
                .onSuccess {
                    val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
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
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun triggerPanicAlert(roundId: Long) {
        viewModelScope.launch {
            triggerPanicUseCase(roundId, "Solicitud de ayuda inmediata desde el botón de pánico.")
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
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            var remoteUrl: String? = null
            if (imageUri != null) {
                FirebaseStorageManager.uploadImage(context, imageUri, "evidencias")
                    .onSuccess { remoteUrl = it }
                    .onFailure { e ->
                        _state.value = _state.value.copy(isLoading = false, error = "Error al subir evidencia: ${e.message}")
                        return@launch
                    }
            }

            // 1. Notificar el escaneo/omisión al servidor (Estado de la ronda)
            scanCheckpointUseCase(roundId, nextCheckpoint.id ?: 0L, reason, status = 2, imageUrl = remoteUrl)
                .onSuccess { checklogId ->
                    // 2. REGLA DE NEGOCIO: Toda omisión genera un incidente automático para auditoría,
                    // lleve o no lleve fotografía (imageUrl será null si no se capturó).
                    val incidentReport = IncidentDto(
                        title = "ALERTA: Checkpoint no escaneado",
                        description = "Justificacion: $reason",
                        severity = "MEDIA",
                        type = "MANTENCION",
                        roundExecutionId = roundId,
                        checklogId = checklogId,
                        imageUrl = remoteUrl,
                        checkpointName = nextCheckpoint.name,
                        checkpointOrder = nextCheckpoint.executionOrder
                    )
                    reportIncidentUseCase(incidentReport)

                    val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    val newExecuted = _state.value.executedCheckpointIds + (nextCheckpoint.id ?: 0L)
                    val newScanTimes = _state.value.scanTimes + ((nextCheckpoint.id ?: 0L) to now)

                    _state.value = _state.value.copy(
                        executedCheckpointIds = newExecuted,
                        scanTimes = newScanTimes,
                        distanceTravelled = newExecuted.size * 150,
                        isLoading = false
                    )
                    onFinish()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun endRound(roundId: Long, observations: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            endRoundUseCase(roundId, observations)
                .onSuccess {
                    sessionManager.clearActiveSession()
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { e ->
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
    val terminationReason: String? = null,
    val pastIncidents: List<IncidentDto> = emptyList()
)
