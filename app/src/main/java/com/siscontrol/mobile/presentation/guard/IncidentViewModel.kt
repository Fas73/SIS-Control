package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.model.Incident
import com.siscontrol.mobile.domain.usecase.ReportIncidentUseCase
import com.siscontrol.mobile.core.FirebaseStorageManager
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
            
            var remoteUrl: String? = null
            
            // Subir a Firebase si hay una imagen local
            if (imageUri != null) {
                FirebaseStorageManager.uploadImage(imageUri, "evidencias")
                    .onSuccess { remoteUrl = it }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Error al subir imagen: ${e.message}"
                        )
                        return@launch
                    }
            }

            val incident = Incident(
                id = null,
                createdAt = null,
                status = null,
                username = null,
                clientName = null,
                checkpointName = null,
                executionOrder = null,
                roundExecutionId = roundExecutionId,
                title = title,
                description = description,
                severity = severity,
                type = type,
                imageUrl = remoteUrl
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
