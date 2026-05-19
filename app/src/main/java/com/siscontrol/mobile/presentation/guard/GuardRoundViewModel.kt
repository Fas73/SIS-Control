package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.EndRoundUseCase
import com.siscontrol.mobile.domain.usecase.GetCheckpointsUseCase
import com.siscontrol.mobile.domain.usecase.ScanCheckpointUseCase
import kotlinx.coroutines.launch

class GuardRoundViewModel(
    private val endRoundUseCase: EndRoundUseCase,
    private val getCheckpointsUseCase: GetCheckpointsUseCase,
    private val scanCheckpointUseCase: ScanCheckpointUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(GuardRoundState())
    val state: State<GuardRoundState> = _state

    fun loadCheckpoints(installationId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingCheckpoints = true)
            getCheckpointsUseCase(installationId)
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        checkpoints = list,
                        isLoadingCheckpoints = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoadingCheckpoints = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
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
            scanCheckpointUseCase(roundId, nextCheckpoint.id, "Escaneo NFC automático")
                .onSuccess {
                    val newExecuted = _state.value.executedCheckpointIds + nextCheckpoint.id
                    _state.value = _state.value.copy(
                        executedCheckpointIds = newExecuted,
                        distanceTravelled = newExecuted.size * 150, // Simulación: 150 metros por punto
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
}

data class GuardRoundState(
    val isLoading: Boolean = false,
    val isLoadingCheckpoints: Boolean = false,
    val checkpoints: List<CheckpointDto> = emptyList(),
    val executedCheckpointIds: Set<Long> = emptySet(),
    val distanceTravelled: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val lastScannedCheckpoint: CheckpointDto? = null,
    val error: String? = null
)
