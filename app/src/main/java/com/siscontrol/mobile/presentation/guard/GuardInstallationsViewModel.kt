package com.siscontrol.mobile.presentation.guard

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.model.Installation
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.domain.model.AttendanceParam
import kotlinx.coroutines.launch

class GuardInstallationsViewModel(
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val getCheckpointsUseCase: GetCheckpointsUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val getCurrentGuardStateUseCase: GetCurrentGuardStateUseCase,
    private val startRoundUseCase: StartRoundUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(GuardInstallationsState())
    val state: State<GuardInstallationsState> = _state

    val checkpointCounts = mutableStateMapOf<Long, Int>()

    init {
        loadInstallations()
    }

    fun loadInstallations() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                val userId = sessionManager.getUserId() ?: 0L
                if (userId > 0) {
                    getCurrentGuardStateUseCase(userId).onSuccess { data ->
                        _state.value = _state.value.copy(
                            isShiftActive = data.isShiftActive ?: false,
                            isRoundActive = data.isRoundActive ?: false,
                            activeInstallationId = data.shift?.installation?.id ?: 0L,
                            activeInstallationName = data.shift?.installation?.name ?: "",
                            activeRoundId = data.round?.id ?: 0L
                        )
                    }
                }

                getInstallationsUseCase()
                    .onSuccess { list ->
                        _state.value = _state.value.copy(installations = list, isLoading = false)
                        list.forEach { inst -> inst.id?.let { loadCheckpointCount(it) } }
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(isLoading = false, error = "Error al conectar: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("GUARD_VM", "Error en loadInstallations", e)
                _state.value = _state.value.copy(isLoading = false, error = "Error interno de la aplicación.")
            }
        }
    }

    private fun loadCheckpointCount(id: Long) {
        viewModelScope.launch {
            getCheckpointsUseCase(id).onSuccess { list -> checkpointCounts[id] = list.size }
        }
    }

    fun startShiftOnly(installationId: Long, installationName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isActionLoading = true, error = null)
                val userId = sessionManager.getUserId() ?: 0L
                
                if (userId == 0L) {
                    _state.value = _state.value.copy(isActionLoading = false, error = "Sesión no válida")
                    return@launch
                }

                val selectedInst = _state.value.installations.find { it.id == installationId }
                val lat = selectedInst?.latitude ?: 0.0
                val lon = selectedInst?.longitude ?: 0.0

                val request = AttendanceParam(userId, installationId, lat, lon)
                checkInUseCase(request)
                    .onSuccess {
                        sessionManager.saveActiveInstallation(installationId, installationName)
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            isShiftActive = true,
                            activeInstallationId = installationId,
                            activeInstallationName = installationName
                        )
                        onSuccess()
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Error desconocido"
                        Log.e("GUARD_VM", "ERROR RECIBIDO: $errorMsg")
                        
                        if (errorMsg.contains("jornada en curso", ignoreCase = true) || errorMsg.contains("ya tienes", ignoreCase = true)) {
                            Log.d("GUARD_VM", "Interpretando Error 400 como éxito (jornada ya existía).")
                            viewModelScope.launch {
                                sessionManager.saveActiveInstallation(installationId, installationName)
                            }
                            _state.value = _state.value.copy(
                                isActionLoading = false,
                                isShiftActive = true,
                                activeInstallationId = installationId,
                                activeInstallationName = installationName
                            )
                            onSuccess()
                        } else {
                            _state.value = _state.value.copy(
                                isActionLoading = false, 
                                error = if (errorMsg.contains("GPS", ignoreCase = true)) {
                                    "📍 Estás fuera del rango de la instalación"
                                } else {
                                    "Error: $errorMsg"
                                }
                            )
                        }
                    }
            } catch (e: Throwable) {
                _state.value = _state.value.copy(isActionLoading = false, error = "Falla crítica")
            }
        }
    }

    fun startNewRound(forcedInstId: Long = 0L, onNavigate: (Long, Long, String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("GUARD_VM", "--- INICIO startNewRound ---")
                _state.value = _state.value.copy(isActionLoading = true, error = null)
                
                val userId = sessionManager.getUserId() ?: 0L
                val instId = if (forcedInstId != 0L) forcedInstId else (sessionManager.getActiveInstallationId() ?: 0L)
                val instName = sessionManager.getActiveInstallationName() ?: ""
                
                Log.d("GUARD_VM", "Usuario: $userId, Instalación: $instId")

                if (userId == 0L || instId == 0L) {
                    _state.value = _state.value.copy(isActionLoading = false, error = "Falta información de la sede")
                    return@launch
                }

                startRoundUseCase(userId, instId)
                    .onSuccess { newRoundId ->
                        Log.d("GUARD_VM", "Ronda iniciada con éxito. ID: $newRoundId")
                        sessionManager.saveActiveRound(newRoundId)
                        _state.value = _state.value.copy(isActionLoading = false, isRoundActive = true, activeRoundId = newRoundId)
                        onNavigate(newRoundId, instId, instName)
                    }
                    .onFailure { e ->
                        Log.e("GUARD_VM", "Error al iniciar ronda: ${e.message}")
                        _state.value = _state.value.copy(isActionLoading = false, error = e.message ?: "Error al iniciar ronda")
                    }
            } catch (e: Exception) {
                Log.e("GUARD_VM", "Crash en startNewRound", e)
                _state.value = _state.value.copy(isActionLoading = false, error = "Error interno al iniciar ronda")
            }
        }
    }

    fun endShift(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isActionLoading = true, error = null)
                val userId = sessionManager.getUserId() ?: 0L
                val installationId = sessionManager.getActiveInstallationId() ?: 0L

                if (userId == 0L || installationId == 0L) {
                    _state.value = _state.value.copy(isActionLoading = false, error = "No hay una sesión activa para finalizar")
                    return@launch
                }

                // Buscamos la instalación para obtener sus coordenadas y que el servidor no de Error 500
                val selectedInst = _state.value.installations.find { it.id == installationId }
                val lat = selectedInst?.latitude ?: 0.0
                val lon = selectedInst?.longitude ?: 0.0

                val request = AttendanceParam(
                    userId = userId,
                    installationId = installationId,
                    latitude = lat,
                    longitude = lon
                )

                checkOutUseCase(request)
                    .onSuccess {
                        sessionManager.clearActiveSession()
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            isShiftActive = false,
                            isRoundActive = false,
                            activeInstallationId = 0L,
                            activeInstallationName = ""
                        )
                        onSuccess()
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            error = "Error al finalizar jornada: ${e.message}"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isActionLoading = false, error = "Error interno")
            }
        }
    }
}

data class GuardInstallationsState(
    val installations: List<Installation> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isShiftActive: Boolean = false,
    val isRoundActive: Boolean = false,
    val activeInstallationId: Long = 0L,
    val activeInstallationName: String = "",
    val activeRoundId: Long = 0L,
    val error: String? = null
)
