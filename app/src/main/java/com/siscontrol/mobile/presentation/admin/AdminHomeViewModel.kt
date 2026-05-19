package com.siscontrol.mobile.presentation.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.*
import kotlinx.coroutines.launch

class AdminHomeViewModel(
    private val getAdminDashboardUseCase: GetAdminDashboardUseCase,
    private val cancelRoundUseCase: CancelRoundUseCase,
    private val cancelShiftUseCase: CancelShiftUseCase,
    private val sessionManager: com.siscontrol.mobile.di.SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(AdminHomeState())
    val state: State<AdminHomeState> = _state

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getAdminDashboardUseCase()
                .onSuccess { data ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        totalGuards = data.totalGuards,
                        activeShifts = data.activeShiftsCount,
                        totalRoundsToday = data.totalRoundsToday,
                        roundsInProgress = data.roundsInProgress,
                        completedRoundsToday = data.roundsCompleted,
                        totalIncidents = data.totalIncidents,
                        pendingIncidents = data.pendingIncidents,
                        totalInstallations = data.totalInstallations,
                        activeInstallations = data.activeInstallationsCount,
                        activeRounds = data.activeRoundsList.map { 
                            DashboardActiveRound(
                                id = it.id,
                                guardName = it.guardName,
                                location = it.location,
                                progress = it.progreso,
                                progressText = it.statusDisplay,
                                status = it.status
                            )
                        },
                        activeShiftsList = data.activeShiftsList.map { 
                            DashboardActiveShift(
                                id = it.id,
                                guardName = it.guardName,
                                location = it.location,
                                entryTime = it.entryTime
                            )
                        }
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun cancelRound(roundId: Long, motivo: String) {
        viewModelScope.launch {
            val adminId = sessionManager.getUserIdSync() ?: 0L
            cancelRoundUseCase(roundId, adminId, motivo)
                .onSuccess { loadDashboardData() }
                .onFailure { e -> _state.value = _state.value.copy(error = com.siscontrol.mobile.core.ErrorUtils.parse(e)) }
        }
    }

    fun cancelShift(shiftId: Long, motivo: String) {
        viewModelScope.launch {
            val adminId = sessionManager.getUserIdSync() ?: 0L
            cancelShiftUseCase(shiftId, adminId, motivo)
                .onSuccess { loadDashboardData() }
                .onFailure { e -> _state.value = _state.value.copy(error = com.siscontrol.mobile.core.ErrorUtils.parse(e)) }
        }
    }
}

data class AdminHomeState(
    val isLoading: Boolean = false,
    val totalGuards: Int = 0,
    val activeShifts: Int = 0,
    val totalRoundsToday: Int = 0,
    val roundsInProgress: Int = 0,
    val completedRoundsToday: Int = 0,
    val totalIncidents: Int = 0,
    val pendingIncidents: Int = 0,
    val totalInstallations: Int = 0,
    val activeInstallations: Int = 0,
    val activeRounds: List<DashboardActiveRound> = emptyList(),
    val activeShiftsList: List<DashboardActiveShift> = emptyList(),
    val error: String? = null
)

data class DashboardActiveRound(
    val id: Long,
    val guardName: String,
    val location: String,
    val progress: Float,
    val progressText: String,
    val status: String
)

data class DashboardActiveShift(
    val id: Long,
    val guardName: String,
    val location: String,
    val entryTime: String
)
