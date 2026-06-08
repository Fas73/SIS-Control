package com.siscontrol.mobile.presentation.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationIdRequest
import com.siscontrol.mobile.domain.usecase.CreateCheckpointUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.presentation.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CheckpointViewModel(
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val createCheckpointUseCase: CreateCheckpointUseCase,
    private val sessionManager: SessionManager
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

    fun createCheckpoint(installationId: Long, name: String, tagCode: String, description: String) {
        viewModelScope.launch {
            _uiState.value = CheckpointUiState.Loading
            
            val editorId = sessionManager.getUserIdSync() ?: 0L

            if (editorId <= 0L) {
                _uiState.value = CheckpointUiState.Error("No se pudo recuperar tu ID de usuario. Por favor, re-inicia sesión.")
                return@launch
            }
            
            val request = CheckpointRequestDto(
                name = name,
                executionOrder = 1,
                nfcTagCode = tagCode,
                locationDescription = description,
                instruction = null,
                installation = InstallationIdRequest(id = installationId)
            )

            createCheckpointUseCase(editorId, request).fold(
                onSuccess = {
                    _uiState.value = CheckpointUiState.Success
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
    object Success : CheckpointUiState()
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
    var tagCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
                .imePadding()
                .verticalScroll(rememberScrollState())
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
                    label = { Text("Instalación", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInst) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedInst,
                    onDismissRequest = { expandedInst = false }
                ) {
                    installations.forEach { inst ->
                        DropdownMenuItem(
                            text = { Text(inst.name ?: "Sede sin nombre") },
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
                label = { Text("Nombre (Ej: Puerta Principal)", color = TextPrimary, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            OutlinedTextField(
                value = tagCode,
                onValueChange = { tagCode = it },
                label = { Text("Código NFC", color = TextPrimary, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción de Ubicación", color = TextPrimary, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (uiState is CheckpointUiState.Error) {
                Text(text = (uiState as CheckpointUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    selectedInstallationId?.let { id ->
                        viewModel.createCheckpoint(id, name, tagCode, description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CheckpointUiState.Loading && name.isNotBlank() && tagCode.isNotBlank() && selectedInstallationId != null
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
