package com.siscontrol.mobile.presentation.admin

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.*
import kotlinx.coroutines.launch

class AdminMapViewModel(
    private val getAdminDashboardUseCase: GetAdminDashboardUseCase
) : ViewModel() {

    private val _state = mutableStateOf(AdminMapState())
    val state: State<AdminMapState> = _state

    private var isMonitoring = false
    private var lastPanicCount = 0

    init {
        loadMapData()
    }

    fun startAutoRefresh(context: Context) {
        if (isMonitoring) return
        isMonitoring = true
        viewModelScope.launch {
            while (isMonitoring) {
                fetchData(context)
                kotlinx.coroutines.delay(10000) 
            }
        }
    }

    fun loadMapData(context: Context? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            fetchData(context)
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private suspend fun fetchData(context: Context?) {
        getAdminDashboardUseCase()
            .onSuccess { data ->
                // Creamos un set de nombres de guardias que están en ronda activa para cruzar datos
                val guardsInRoundNames = data.activeRoundsList.map { it.guardName }.toSet()

                val activeGuards = data.activeShiftsList.map { shift ->
                    val isReallyOnRound = shift.isOnRound || guardsInRoundNames.contains(shift.guardName)
                    
                    // Lógica de Ubicación Plan B: 
                    // 1. Usar GPS del guardia si existe.
                    // 2. Si no, usar coordenadas de la Instalación (si el backend las envía).
                    // 3. Fallback final: 0.0 (Se filtrará).
                    val lat = shift.latitude ?: 0.0
                    val lon = shift.longitude ?: 0.0
                    
                    GuardMapPin(
                        guardId = shift.id,
                        guardName = shift.guardName,
                        installationName = shift.location,
                        latitude = lat, 
                        longitude = lon,
                        status = if (isReallyOnRound) "En Ronda" else "Activo"
                    )
                }.filter { it.latitude != 0.0 && it.longitude != 0.0 }

                if (data.pendingIncidents > lastPanicCount && context != null) {
                    playAlertSound(context)
                }
                lastPanicCount = data.pendingIncidents

                _state.value = _state.value.copy(activeGuards = activeGuards)
            }
            .onFailure { e ->
                _state.value = _state.value.copy(
                    error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                )
            }
    }

    private fun playAlertSound(context: Context) {
        try {
            // Sonido
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()

            // Vibración (Modo moderno para evitar advertencias de depreciación)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isMonitoring = false
    }
}

data class AdminMapState(
    val isLoading: Boolean = false,
    val activeGuards: List<GuardMapPin> = emptyList(),
    val error: String? = null
)

data class GuardMapPin(
    val guardId: Long,
    val guardName: String,
    val installationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String // "En Ronda", "Activo"
)
