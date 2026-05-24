package com.siscontrol.mobile.presentation.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.model.Incident
import com.siscontrol.mobile.domain.repository.IncidentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminAlertsViewModel(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _state = mutableStateOf(AdminAlertsState())
    val state: State<AdminAlertsState> = _state

    private var isMonitoring = false

    init {
        loadInitialAlerts()
        startRealTimeMonitoring()
    }

    private fun startRealTimeMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        viewModelScope.launch {
            while (isMonitoring) {
                loadAlertsSilently()
                delay(4000) // 4 segundos de polling para efecto "tiempo real"
            }
        }
    }

    private fun loadInitialAlerts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            incidentRepository.getAllIncidents()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        alerts = list.sortedByDescending { it.createdAt },
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    private fun loadAlertsSilently() {
        viewModelScope.launch {
            incidentRepository.getAllIncidents()
                .onSuccess { list ->
                    val sortedList = list.sortedByDescending { it.createdAt }
                    // Siempre actualizamos si hay cambios en el contenido o tamaño
                    if (sortedList != _state.value.alerts) {
                        android.util.Log.d("ALERTS_VM", "Actualizando lista: ${sortedList.size} alertas")
                        _state.value = _state.value.copy(alerts = sortedList)
                    }
                }
                .onFailure { e ->
                    android.util.Log.w("ALERTS_VM", "Polling falló: ${e.message}")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isMonitoring = false
    }
}

data class AdminAlertsState(
    val alerts: List<Incident> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
