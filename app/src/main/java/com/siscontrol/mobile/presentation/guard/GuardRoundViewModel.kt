package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.model.Checkpoint
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
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(GuardRoundState())
    val state: State<GuardRoundState> = _state

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

    private fun fetchExecutedCheckpoints(roundId: Long) {
        viewModelScope.launch {
            getRoundDetailUseCase(roundId).onSuccess { detail ->
                val validScans = detail.scans?.filter { it.checkpoint?.id != null } ?: emptyList()
                val executedMap = validScans.associate { (it.checkpoint?.id ?: 0L) to (it.scannedAt ?: "S/H") }
                
                _state.value = _state.value.copy(
                    executedCheckpointIds = executedMap.keys.filter { it > 0L }.toSet(),
                    scanTimes = executedMap,
                    distanceTravelled = executedMap.size * 150
                )
            }
        }
    }

    fun scanNfcTag(roundId: Long, tagId: String, onVerificationSuccess: (Checkpoint) -> Unit) {
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
                .onSuccess { scannedAtOficial ->
                    // Usar el timestamp oficial generado por el backend.
                    // Si el backend no devuelve scannedAt (caso excepcional), se marca como pendiente.
                    val timeToShow = scannedAtOficial.ifBlank { "S/H" }
                    val newExecuted = _state.value.executedCheckpointIds + (nextCheckpoint.id ?: 0L)
                    val newScanTimes = _state.value.scanTimes + ((nextCheckpoint.id ?: 0L) to timeToShow)

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

    fun skipCheckpoint(roundId: Long, reason: String, imageUri: android.net.Uri?, onFinish: () -> Unit) {
        val nextCheckpoint = getNextCheckpoint() ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            var remoteUrl: String? = null
            if (imageUri != null) {
                FirebaseStorageManager.uploadImage(imageUri, "evidencias")
                    .onSuccess { remoteUrl = it }
                    .onFailure { e ->
                        _state.value = _state.value.copy(isLoading = false, error = "Error al subir evidencia: ${e.message}")
                        return@launch
                    }
            }

            scanCheckpointUseCase(roundId, nextCheckpoint.id ?: 0L, reason, status = 2, imageUrl = remoteUrl)
                .onSuccess { scannedAtOficial ->
                    // Usar el timestamp oficial generado por el backend.
                    val timeToShow = scannedAtOficial.ifBlank { "S/H" }
                    val newExecuted = _state.value.executedCheckpointIds + (nextCheckpoint.id ?: 0L)
                    val newScanTimes = _state.value.scanTimes + ((nextCheckpoint.id ?: 0L) to timeToShow)

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

    private fun getNextCheckpoint(): Checkpoint? {
        return _state.value.checkpoints
            .sortedBy { it.executionOrder }
            .firstOrNull { it.id !in _state.value.executedCheckpointIds }
    }

    fun getMinutesPassed(): Int {
        val diff = System.currentTimeMillis() - _state.value.startTime
        return (diff / 60000).toInt()
    }
}

data class GuardRoundState(
    val isLoading: Boolean = false,
    val isLoadingCheckpoints: Boolean = false,
    val checkpoints: List<Checkpoint> = emptyList(),
    val executedCheckpointIds: Set<Long> = emptySet(),
    val scanTimes: Map<Long, String> = emptyMap(),
    val distanceTravelled: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val lastScannedCheckpoint: Checkpoint? = null,
    val successMessage: String? = null,
    val error: String? = null
)
