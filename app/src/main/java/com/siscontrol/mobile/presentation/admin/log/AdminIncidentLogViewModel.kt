package com.siscontrol.mobile.presentation.admin.log

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.repository.IncidentRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AdminIncidentLogViewModel(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _state = mutableStateOf(AdminIncidentLogState())
    val state: State<AdminIncidentLogState> = _state

    init {
        loadIncidents()
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            incidentRepository.getAllIncidents()
                .onSuccess { list ->
                    // ORDENAR POR FECHA DESCENDENTE (Lo más nuevo arriba)
                    val sortedList = list.sortedByDescending { it.createdAt }
                    _state.value = _state.value.copy(
                        allIncidents = sortedList,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    /**
     * Obtiene el reporte de jornada vinculado a una alerta de término de turno.
     */
    fun getShiftReportForAlert(shiftId: Long, onResult: (com.siscontrol.mobile.data.remote.dto.ShiftReportDto?) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            com.siscontrol.mobile.di.AppModule.getShiftReportUseCase(shiftId)
                .onSuccess { report ->
                    _state.value = _state.value.copy(isLoading = false)
                    onResult(report)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false)
                    onResult(null)
                }
        }
    }
}

data class AdminIncidentLogState(
    val allIncidents: List<IncidentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
