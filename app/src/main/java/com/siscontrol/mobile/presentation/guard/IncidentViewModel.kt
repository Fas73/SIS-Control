package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.usecase.ReportIncidentUseCase
import com.siscontrol.mobile.core.FirebaseStorageManager
import kotlinx.coroutines.launch

class IncidentViewModel(
    private val reportIncidentUseCase: ReportIncidentUseCase
) : ViewModel() {

    private val _state = mutableStateOf(IncidentState())
    val state: State<IncidentState> = _state

    fun reportIncident(
        context: android.content.Context,
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
            
            // Subir a Firebase con COMPRESIÓN OPTIMIZADA
            if (imageUri != null) {
                FirebaseStorageManager.uploadImage(context, imageUri, "evidencias")
                    .onSuccess { remoteUrl = it }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Error al subir imagen: ${e.message}"
                        )
                        return@launch
                    }
            }

            val incident = IncidentDto(
                title = title,
                description = description,
                severity = severity,
                type = type,
                roundExecutionId = roundExecutionId,
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
