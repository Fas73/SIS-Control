package com.siscontrol.mobile.presentation.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.di.AppModule
import com.siscontrol.mobile.domain.usecase.GetPersonnelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class PersonnelViewModel(
    private val getPersonnelUseCase: GetPersonnelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonnelUiState>(PersonnelUiState.Loading)
    val uiState: StateFlow<PersonnelUiState> = _uiState

    init {
        loadPersonnel()
    }

    private fun loadPersonnel() {
        viewModelScope.launch {
            _uiState.value = PersonnelUiState.Loading
            getPersonnelUseCase().fold(
                onSuccess = { personnel ->
                    _uiState.value = PersonnelUiState.Success(personnel)
                },
                onFailure = { error ->
                    _uiState.value = PersonnelUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// ViewModel Factory
// ---------------------------------------------------------------------------

private class PersonnelViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PersonnelViewModel(AppModule.getPersonnelUseCase) as T
}

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

sealed class PersonnelUiState {
    object Loading : PersonnelUiState()
    data class Success(val personnel: List<UserResponseDto>) : PersonnelUiState()
    data class Error(val message: String) : PersonnelUiState()
}

// ---------------------------------------------------------------------------
// Composable Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelListScreen(
    userRole: String,
    onBack: () -> Unit
) {
    val viewModel: PersonnelViewModel = viewModel(factory = PersonnelViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(com.siscontrol.mobile.presentation.theme.PrimaryColor, com.siscontrol.mobile.presentation.theme.PrimaryVariant)
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = androidx.compose.ui.graphics.Color.White)
                    }
                    Text(
                        text = "Personal",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(com.siscontrol.mobile.presentation.theme.BackgroundColor)) {
            when (val state = uiState) {
                is PersonnelUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
                is PersonnelUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                is PersonnelUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(state.personnel) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = com.siscontrol.mobile.presentation.theme.SurfaceColor),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(50.dp).background(com.siscontrol.mobile.presentation.theme.PrimaryColor, shape = androidx.compose.foundation.shape.CircleShape),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        Text(user.fullName.take(1).uppercase(), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = user.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        Text(text = user.role, color = com.siscontrol.mobile.presentation.theme.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        if (!user.installations.isNullOrEmpty()) {
                                            Text(
                                                text = "📍 ${user.installations.first().name}",
                                                color = com.siscontrol.mobile.presentation.theme.TextSecondary,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    com.siscontrol.mobile.presentation.components.StatusBadge(status = com.siscontrol.mobile.presentation.components.BadgeStatus.ACTIVE)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
