package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.usecase.ReportIncidentUseCase
import kotlinx.coroutines.launch

class IncidentViewModel(
    private val reportIncidentUseCase: ReportIncidentUseCase
) : ViewModel() {

    private val _state = mutableStateOf(IncidentState())
    val state: State<IncidentState> = _state

    fun reportIncident(
        title: String,
        description: String,
        severity: String,
        type: String,
        roundExecutionId: Long,
        imageUri: android.net.Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Simulación de subida: si hay URI, simulamos una URL remota
            val simulatedUrl = if (imageUri != null) {
                "https://siscontrol-storage.s3.amazonaws.com/incidents/${java.util.UUID.randomUUID()}.jpg"
            } else {
                null
            }

            val incident = IncidentDto(
                title = title,
                description = description,
                severity = severity,
                type = type,
                roundExecutionId = roundExecutionId,
                imageUrl = simulatedUrl
            )

            reportIncidentUseCase(incident)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                    onSuccess()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false, 
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun resetState() {
        _state.value = IncidentState()
    }
}

data class IncidentState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
