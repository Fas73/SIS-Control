package com.siscontrol.mobile.presentation.management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationRequestDto
import com.siscontrol.mobile.domain.usecase.CreateInstallationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InstallationViewModel(
    private val createInstallationUseCase: CreateInstallationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InstallationUiState>(InstallationUiState.Idle)
    val uiState: StateFlow<InstallationUiState> = _uiState

    fun createInstallation(name: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.value = InstallationUiState.Loading
            val request = InstallationRequestDto(name, address, lat, lng)
            createInstallationUseCase(request).fold(
                onSuccess = { installation ->
                    _uiState.value = InstallationUiState.Success(installation)
                },
                onFailure = { error ->
                    _uiState.value = InstallationUiState.Error(error.message ?: "Error al crear instalación")
                }
            )
        }
    }
}

sealed class InstallationUiState {
    object Idle : InstallationUiState()
    object Loading : InstallationUiState()
    data class Success(val installation: InstallationDto) : InstallationUiState()
    data class Error(val message: String) : InstallationUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstallationScreen(
    viewModel: InstallationViewModel,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is InstallationUiState.Success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva Instalación") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
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

            if (uiState is InstallationUiState.Error) {
                Text(text = (uiState as InstallationUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val latDouble = lat.toDoubleOrNull() ?: 0.0
                    val lngDouble = lng.toDoubleOrNull() ?: 0.0
                    viewModel.createInstallation(name, address, latDouble, lngDouble)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is InstallationUiState.Loading && name.isNotBlank()
            ) {
                if (uiState is InstallationUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Instalación")
                }
            }
        }
    }
}
