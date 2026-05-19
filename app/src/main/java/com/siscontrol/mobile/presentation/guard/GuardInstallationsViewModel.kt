package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.CheckInUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import com.siscontrol.mobile.domain.usecase.StartRoundUseCase
import kotlinx.coroutines.launch

class GuardInstallationsViewModel(
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val startRoundUseCase: StartRoundUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(GuardInstallationsState())
    val state: State<GuardInstallationsState> = _state

    private val _checkpointCounts = mutableStateMapOf<Long, Int>()
    val checkpointCounts: Map<Long, Int> = _checkpointCounts

    init {
        checkActiveSession()
        loadInstallations()
    }

    private fun checkActiveSession() {
        val activeRoundId = sessionManager.getActiveRoundIdSync() ?: 0L
        if (activeRoundId != 0L) {
            _state.value = _state.value.copy(
                activeRoundId = activeRoundId,
                activeInstallationId = sessionManager.getActiveInstallationIdSync() ?: 0L,
                activeInstallationName = sessionManager.getActiveInstallationNameSync() ?: ""
            )
        }
    }

    fun loadInstallations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getInstallationsUseCase()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        installations = list,
                        isLoading = false
                    )
                    // Cargar conteo verídico por cada instalación
                    list.forEach { inst ->
                        inst.id?.let { id -> fetchCount(id) }
                    }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    private fun fetchCount(installationId: Long) {
        viewModelScope.launch {
            com.siscontrol.mobile.di.AppModule.getCheckpointsUseCase(installationId)
                .onSuccess { _checkpointCounts[installationId] = it.size }
        }
    }

    fun startTurnAndRound(installationId: Long, installationName: String, onRoundStarted: (Long, Long, String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true, error = null)
            
            val userId = sessionManager.getUserIdSync() ?: 0L
            if (userId == 0L) {
                _state.value = _state.value.copy(isActionLoading = false, error = "Sesión inválida")
                return@launch
            }

            // Buscamos la instalación seleccionada para enviar sus coordenadas verídicas
            val selectedInst = _state.value.installations.find { it.id == installationId }
            val lat = selectedInst?.latitude ?: 0.0
            val lon = selectedInst?.longitude ?: 0.0

            checkInUseCase(AttendanceRequest(userId, installationId, lat, lon))
                .onSuccess {
                    startRoundUseCase(userId, installationId)
                        .onSuccess { roundId ->
                            sessionManager.saveActiveSession(installationId, roundId, installationName)
                            onRoundStarted(roundId, installationId, installationName)
                        }
                        .onFailure { e ->
                            _state.value = _state.value.copy(isActionLoading = false, error = e.message)
                        }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isActionLoading = false, error = e.message)
                }
        }
    }
}

data class GuardInstallationsState(
    val installations: List<InstallationDto> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val activeRoundId: Long = 0L,
    val activeInstallationId: Long = 0L,
    val activeInstallationName: String = "",
    val error: String? = null
)
