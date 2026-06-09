package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.core.FirebaseStorageManager
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.usecase.ReportIncidentUseCase
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
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Actualizar estado a cargando en el hilo principal
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                
                val location = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)
                // Formato ISO 8601 exacto (Ej: 2026-06-03T12:00:00) para Jackson/LocalDateTime
                val clientTimeIso = java.time.LocalDateTime.now()
                    .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                    .toString() 
                
                var remoteUrl: String? = null

                // 1. PROCESAMIENTO RÁPIDO (Un solo paso) Y SUBIDA
                if (imageUri != null) {
                    try {
                        val extraInfo = location?.let { "GPS: ${it.latitude}, ${it.longitude}" }
                        // Procesa Marca de Agua + Rotación + Compresión en memoria RAM (Súper veloz)
                        val imageBytes = com.siscontrol.mobile.core.ImageUtils.processImageForUpload(context, imageUri, extraInfo)
                        
                        if (imageBytes != null) {
                            val uploadResult = FirebaseStorageManager.uploadBytes(imageBytes, "evidencias")
                            remoteUrl = uploadResult.getOrNull()
                        }
                        
                        if (remoteUrl != null) {
                            android.util.Log.d("CRITICAL_FRONTEND", "✅ Foto en Firebase: $remoteUrl")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CRITICAL_FRONTEND", "Error multimedia: ${e.message}")
                    }
                }

                // 2. CONSTRUCCIÓN PURA DEL DTO
                val incident = IncidentDto(
                    id = null,
                    title = title,
                    description = description,
                    severity = severity.lowercase().replaceFirstChar { it.uppercase() },
                    type = type.uppercase().trim(),
                    roundExecutionId = if (roundExecutionId > 0) roundExecutionId else null,
                    imageUrl = remoteUrl,
                    clientTimestamp = java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString(),
                    latitude = location?.latitude ?: 0.0,
                    longitude = location?.longitude ?: 0.0,
                    status = 0
                )

                // CONFIGURACIÓN GSON ESTILO POSTMAN (Sin nulos y sin escapar caracteres)
                val gson = com.google.gson.GsonBuilder()
                    .disableHtmlEscaping() // Para que la URL de Firebase no lleve \u003d
                    .create()
                val jsonToSend = gson.toJson(incident)
                
                android.util.Log.d("CRITICAL_FRONTEND", "🚀 JSON ESTILO POSTMAN: $jsonToSend")

                // 3. ENVÍO AL BACKEND
                reportIncidentUseCase(incident)
                    .onSuccess {
                        android.util.Log.d("CRITICAL_FRONTEND", "✅ Sincronización exitosa con MySQL")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                            onSuccess()
                        }
                    }
                    .onFailure { e ->
                        // MODIFICACIÓN PARA DIAGNÓSTICO SOLICITADA POR BACKEND
                        android.util.Log.e("CRITICAL_FRONTEND", "❌ EL SERVIDOR RECHAZÓ EL DTO CON IMAGEN. Error: ${e.localizedMessage}")

                        saveIncidentLocally(title, description, severity, type, roundExecutionId, remoteUrl ?: imageUri?.toString(), location, clientTimeIso, onSuccess)
                    }

            } catch (e: Exception) {
                android.util.Log.e("CRITICAL_FRONTEND", "CRASH EN FLUJO: ${e.localizedMessage}", e)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private fun saveIncidentLocally(
        title: String,
        description: String,
        severity: String,
        type: String,
        roundId: Long?,
        localPath: String?,
        location: android.location.Location?,
        timestamp: String,
        onFinish: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val entity = com.siscontrol.mobile.data.local.entities.PendingIncidentEntity(
                    title = title,
                    description = description,
                    severity = severity,
                    type = type,
                    roundExecutionId = roundId,
                    checklogId = null,
                    localImageUri = localPath,
                    clientTimestamp = timestamp,
                    latitude = location?.latitude,
                    longitude = location?.longitude
                )
                com.siscontrol.mobile.di.AppModule.getDatabase().pendingIncidentDao().insertIncident(entity)
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                onFinish()
            } catch (e: Exception) {
                android.util.Log.e("INCIDENT_VM", "Error guardando localmente: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = "Error al guardar localmente")
            }
        }
    }

    /**
     * Usa la IA para analizar la imagen y sugerir datos al guardia.
     */
    fun analyzeImageWithAI(context: android.content.Context, uri: android.net.Uri, onSuggestion: (String) -> Unit) {
        viewModelScope.launch {
            val labels = com.siscontrol.mobile.core.AIManager.analyzeImage(context, uri)
            _state.value = _state.value.copy(detectedLabels = labels)
            if (labels.isNotEmpty()) {
                val suggestion = com.siscontrol.mobile.core.AIManager.suggestTitle(labels)
                onSuggestion(suggestion)
            }
        }
    }

    /**
     * Mejora la redacción del reporte usando lógica de IA contextual.
     */
    fun improveReportWithAI(currentText: String, hasPhoto: Boolean, onResult: (String) -> Unit) {
        val improved = com.siscontrol.mobile.core.AIManager.generateProfessionalDescription(
            userInput = currentText,
            labels = _state.value.detectedLabels,
            hasPhoto = hasPhoto
        )
        onResult(improved)
    }

    fun resetState() {
        _state.value = IncidentState()
    }
}

data class IncidentState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val detectedLabels: List<String> = emptyList()
)
