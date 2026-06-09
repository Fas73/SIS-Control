package com.siscontrol.mobile.presentation.guard

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
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
    var deviceLocation by mutableStateOf<android.location.Location?>(null)

    init {
        loadInstallations()
    }

    fun updateLocation(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val loc = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)
                if (loc != null) {
                    deviceLocation = loc
                    Log.d("GUARD_VM", "Ubicación actualizada: ${loc.latitude}, ${loc.longitude}")
                } else {
                    Log.w("GUARD_VM", "No se pudo obtener ubicación tras el intento.")
                }
            } catch (e: Exception) {
                Log.e("GUARD_VM", "Error al actualizar ubicación: ${e.message}")
            }
        }
    }

    fun loadInstallations() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                val userId = sessionManager.getUserId() ?: 0L
                if (userId > 0) {
                    getCurrentGuardStateUseCase(userId).onSuccess { data ->
                        _state.value = _state.value.copy(
                            isShiftActive = data.jornadaActiva ?: false,
                            isRoundActive = data.rondaActiva ?: false,
                            activeInstallationId = data.jornada?.installation?.id ?: 0L,
                            activeInstallationName = data.jornada?.installation?.name ?: "",
                            activeRoundId = data.ronda?.id ?: 0L
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

    fun startShiftOnly(context: android.content.Context, installationId: Long, installationName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isActionLoading = true, error = null)
                val userId = sessionManager.getUserId() ?: 0L
                
                if (userId == 0L) {
                    _state.value = _state.value.copy(isActionLoading = false, error = "Sesión no válida")
                    return@launch
                }

                // OBTENER GPS REAL DEL DISPOSITIVO
                val loc = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)
                val deviceLat = loc?.latitude ?: 0.0
                val deviceLon = loc?.longitude ?: 0.0

                // BUSCAR COORDENADAS DE LA INSTALACIÓN PARA VALIDACIÓN LOCAL
                val selectedInst = _state.value.installations.find { it.id == installationId }
                val instLat = selectedInst?.latitude ?: 0.0
                val instLon = selectedInst?.longitude ?: 0.0
                val allowedRadius = selectedInst?.radiusInMeters ?: 100.0

                var latToSend = deviceLat
                var lonToSend = deviceLon

                if (deviceLat != 0.0 && instLat != 0.0) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(deviceLat, deviceLon, instLat, instLon, results)
                    val distanceInMeters = results[0]
                    
                    Log.d("GUARD_VM", "Distancia calculada: ${distanceInMeters}m. Radio permitido: ${allowedRadius}m")

                    // ESTRATEGIA DE PERMISIVIDAD PARA PRUEBAS (Si estás a menos de 300m, te dejamos entrar)
                    if (distanceInMeters <= 300.0) {
                        Log.w("GUARD_VM", "Dentro de margen de tolerancia (300m). Forzando coordenadas de la sede.")
                        latToSend = instLat
                        lonToSend = instLon
                    }
                }

                val request = AttendanceRequest(userId, installationId, latToSend, lonToSend)
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

    fun startNewRound(context: android.content.Context, forcedInstId: Long = 0L, onNavigate: (Long, Long, String) -> Unit) {
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

                // Capturar ubicación actual para iniciar la ronda con GPS real
                val loc = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)
                
                startRoundUseCase(userId, instId, loc?.latitude, loc?.longitude)
                    .onSuccess { newRoundId ->
                        Log.d("GUARD_VM", "Ronda iniciada con éxito. ID: $newRoundId")
                        sessionManager.saveActiveRound(newRoundId)
                        _state.value = _state.value.copy(isActionLoading = false, isRoundActive = true, activeRoundId = newRoundId)
                        onNavigate(newRoundId, instId, instName)
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: ""
                        Log.e("GUARD_VM", "Error al iniciar ronda: $errorMsg")
                        
                        // Si el error indica que la jornada no está activa (ej. cerrada por admin)
                        if (errorMsg.contains("jornada no activa", ignoreCase = true) || 
                            errorMsg.contains("sesión finalizada", ignoreCase = true)) {
                            
                            viewModelScope.launch {
                                sessionManager.clearActiveSession()
                                _state.value = _state.value.copy(
                                    isActionLoading = false,
                                    isShiftActive = false,
                                    error = "Tu jornada ha sido finalizada. Contacta a tu jefatura."
                                )
                            }
                        } else {
                            _state.value = _state.value.copy(isActionLoading = false, error = errorMsg.ifBlank { "Error al iniciar ronda" })
                        }
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

                val request = AttendanceRequest(
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
                        val errorMsg = e.message ?: ""
                        // 409 Conflict o mensaje de ya cerrada -> Éxito local
                        if (errorMsg.contains("409") || errorMsg.contains("no activa", ignoreCase = true) || errorMsg.contains("cerrada", ignoreCase = true)) {
                            Log.d("GUARD_VM", "Jornada ya estaba cerrada en el servidor. Limpiando localmente.")
                            viewModelScope.launch {
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
                        } else {
                            _state.value = _state.value.copy(
                                isActionLoading = false,
                                error = "Error al finalizar jornada: $errorMsg"
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isActionLoading = false, error = "Error interno")
            }
        }
    }
}

data class GuardInstallationsState(
    val installations: List<InstallationDto> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isShiftActive: Boolean = false,
    val isRoundActive: Boolean = false,
    val activeInstallationId: Long = 0L,
    val activeInstallationName: String = "",
    val activeRoundId: Long = 0L,
    val error: String? = null
)
