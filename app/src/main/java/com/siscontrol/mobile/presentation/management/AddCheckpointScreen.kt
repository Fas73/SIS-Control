package com.siscontrol.mobile.presentation.management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.domain.usecase.CreateCheckpointUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CheckpointViewModel(
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val createCheckpointUseCase: CreateCheckpointUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckpointUiState>(CheckpointUiState.Idle)
    val uiState: StateFlow<CheckpointUiState> = _uiState

    private val _installations = MutableStateFlow<List<InstallationDto>>(emptyList())
    val installations: StateFlow<List<InstallationDto>> = _installations

    init {
        loadInstallations()
    }

    private fun loadInstallations() {
        viewModelScope.launch {
            getInstallationsUseCase().onSuccess { insts ->
                _installations.value = insts
            }
        }
    }

    fun createCheckpoint(installationId: Long, name: String, type: String, tagId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.value = CheckpointUiState.Loading
            val request = CheckpointRequestDto(name, type, tagId, lat, lng)
            createCheckpointUseCase(installationId, request).fold(
                onSuccess = { checkpoint ->
                    _uiState.value = CheckpointUiState.Success(checkpoint)
                },
                onFailure = { error ->
                    _uiState.value = CheckpointUiState.Error(error.message ?: "Error al crear checkpoint")
                }
            )
        }
    }
}

sealed class CheckpointUiState {
    object Idle : CheckpointUiState()
    object Loading : CheckpointUiState()
    data class Success(val checkpoint: CheckpointDto) : CheckpointUiState()
    data class Error(val message: String) : CheckpointUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCheckpointScreen(
    viewModel: CheckpointViewModel,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val installations by viewModel.installations.collectAsState()

    var selectedInstallationId by remember { mutableStateOf<Long?>(null) }
    var expandedInst by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("") }
    
    var selectedType by remember { mutableStateOf("NFC") }
    var expandedType by remember { mutableStateOf(false) }
    
    var tagIdentifier by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is CheckpointUiState.Success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agregar Checkpoint") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // Elegir instalación
            ExposedDropdownMenuBox(
                expanded = expandedInst,
                onExpandedChange = { expandedInst = !expandedInst }
            ) {
                val currentText = installations.find { it.id == selectedInstallationId }?.name ?: "Seleccione una instalación"
                OutlinedTextField(
                    readOnly = true,
                    value = currentText,
                    onValueChange = {},
                    label = { Text("Instalación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInst) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedInst,
                    onDismissRequest = { expandedInst = false }
                ) {
                    installations.forEach { inst ->
                        DropdownMenuItem(
                            text = { Text(inst.name) },
                            onClick = {
                                selectedInstallationId = inst.id
                                expandedInst = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (Ej: Puerta Principal)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Elegir Tipo
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedType,
                    onValueChange = {},
                    label = { Text("Tipo de Marcaje") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    listOf("NFC", "QR").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                expandedType = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = tagIdentifier,
                onValueChange = { tagIdentifier = it },
                label = { Text("ID del Tag manual") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Latitud") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = lng,
                onValueChange = { lng = it },
                label = { Text("Longitud") },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState is CheckpointUiState.Error) {
                Text(text = (uiState as CheckpointUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val latDouble = lat.toDoubleOrNull() ?: 0.0
                    val lngDouble = lng.toDoubleOrNull() ?: 0.0
                    selectedInstallationId?.let { id ->
                        viewModel.createCheckpoint(id, name, selectedType, tagIdentifier, latDouble, lngDouble)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CheckpointUiState.Loading && name.isNotBlank() && tagIdentifier.isNotBlank() && selectedInstallationId != null
            ) {
                if (uiState is CheckpointUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Checkpoint")
                }
            }
        }
    }
}
