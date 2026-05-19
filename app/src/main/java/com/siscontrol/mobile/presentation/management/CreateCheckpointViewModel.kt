package com.siscontrol.mobile.presentation.management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationIdRequest
import com.siscontrol.mobile.domain.usecase.CreateCheckpointUseCase
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

class CreateCheckpointViewModel(
    private val createCheckpointUseCase: CreateCheckpointUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun createCheckpoint(name: String, nfcCode: String, desc: String, installationId: Long) {
        viewModelScope.launch {
            isLoading = true
            error = null
            isSuccess = false

            val editorId = sessionManager.getUserIdSync() ?: 0L

            if (editorId <= 0L) {
                error = "No se pudo recuperar tu ID de usuario. Por favor, re-inicia sesión."
                isLoading = false
                return@launch
            }

            val request = CheckpointRequestDto(
                name = name,
                executionOrder = 1,
                nfcTagCode = nfcCode,
                locationDescription = desc,
                instruction = null,
                installation = InstallationIdRequest(id = installationId)
            )

            createCheckpointUseCase(editorId, request)
                .onSuccess {
                    isSuccess = true
                    isLoading = false
                }
                .onFailure { e ->
                    error = e.message ?: "Error desconocido al crear el checkpoint"
                    isLoading = false
                }
        }
    }

    fun resetState() {
        isSuccess = false
        error = null
        isLoading = false
    }
}
