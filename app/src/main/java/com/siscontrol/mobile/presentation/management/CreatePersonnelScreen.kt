package com.siscontrol.mobile.presentation.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.CreatePersonnelUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.presentation.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

sealed class CreatePersonnelUiState {
    object Idle : CreatePersonnelUiState()
    object Loading : CreatePersonnelUiState()
    data class Success(val user: UserResponseDto) : CreatePersonnelUiState()
    data class Error(val message: String) : CreatePersonnelUiState()
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class CreatePersonnelViewModel(
    private val createPersonnelUseCase: CreatePersonnelUseCase,
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val getPersonnelUseCase: com.siscontrol.mobile.domain.usecase.GetPersonnelUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _createState = MutableStateFlow<CreatePersonnelUiState>(CreatePersonnelUiState.Idle)
    val createState: StateFlow<CreatePersonnelUiState> = _createState

    private val _installations = MutableStateFlow<List<InstallationDto>>(emptyList())
    val installations: StateFlow<List<InstallationDto>> = _installations

    private val _existingUsers = MutableStateFlow<List<UserResponseDto>>(emptyList())

    init {
        cargarDatosPrevios()
    }

    private fun cargarDatosPrevios() {
        viewModelScope.launch {
            getInstallationsUseCase().onSuccess { _installations.value = it }
            getPersonnelUseCase().onSuccess { _existingUsers.value = it }
        }
    }

    fun isRutTaken(rut: String): Boolean = _existingUsers.value.any { it.rut?.equals(rut, ignoreCase = true) == true }
    fun isEmailTaken(email: String): Boolean = _existingUsers.value.any { it.email.equals(email, ignoreCase = true) }
    fun isUsernameTaken(username: String): Boolean = _existingUsers.value.any { it.username.equals(username, ignoreCase = true) }

    fun crearPersonal(
        rut: String,
        email: String,
        fullName: String,
        phoneNumber: String,
        role: String
    ) {
        viewModelScope.launch {
            _createState.value = CreatePersonnelUiState.Loading

            val creatorId = getEditorId()
            if (creatorId <= 0L) {
                _createState.value = CreatePersonnelUiState.Error("No se pudo identificar al administrador actual.")
                return@launch
            }

            // Auto-generar username: nombre + _ + primeros 4 digitos del rut
            val firstName = fullName.trim().split(" ").firstOrNull()?.lowercase() ?: "user"
            val rutDigits = rut.filter { it.isDigit() }
            val first4Rut = if (rutDigits.length >= 4) rutDigits.take(4) else rutDigits
            val generatedUsername = "${firstName}_$first4Rut"

            val request = UserRequestDto(
                rut = rut,
                username = generatedUsername,
                email = email,
                fullName = fullName,
                password = "pass123", // Password estática solicitada
                phoneNumber = phoneNumber,
                role = role
            )

            createPersonnelUseCase(creatorId, request).fold(
                onSuccess = { nuevoUsuario ->
                    _createState.value = CreatePersonnelUiState.Success(nuevoUsuario)
                },
                onFailure = { error ->
                    _createState.value = CreatePersonnelUiState.Error(
                        error.message ?: "Error al crear el usuario"
                    )
                }
            )
        }
    }

    private suspend fun getEditorId(): Long {
        val sessionRoom = com.siscontrol.mobile.di.AppModule.getDatabase().userSessionDao().getSessionSync()
        val roomUserId = sessionRoom?.id ?: 0L
        val dsUserId = sessionManager.getUserIdSync() ?: 0L
        return if (roomUserId > 0) roomUserId else dsUserId
    }

    fun resetState() {
        _createState.value = CreatePersonnelUiState.Idle
    }
}

// ---------------------------------------------------------------------------
// Composable Screen Principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePersonnelScreen(
    viewModel: CreatePersonnelViewModel,
    currentUserRole: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var rut          by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var fullName     by remember { mutableStateOf("") }
    var phoneDigits  by remember { mutableStateOf("") }
    
    val rolesMapping = mapOf(
        "Administrador" to "ADMIN",
        "Supervisor" to "SUPERVISOR",
        "Guardia" to "GUARD"
    )
    
    val roleLabels = remember(currentUserRole) {
        if (currentUserRole == "ADMIN") {
            listOf("Administrador", "Supervisor", "Guardia")
        } else {
            listOf("Guardia")
        }
    }
    
    var selectedRoleLabel by remember(roleLabels) { mutableStateOf(roleLabels.first()) }
    var expandedRoleMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.createState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is CreatePersonnelUiState.Success) {
            onSuccess()
            viewModel.resetState()
        }
    }

    val formularioValido = rut.isNotBlank()
            && email.isNotBlank()
            && fullName.isNotBlank()
            && phoneDigits.length == 9

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryVariant)))
                    .statusBarsPadding()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(
                        text = "Registrar Personal",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Configuración de Cuenta", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 16.sp)
                        
                        Column {
                            Text("Perfil de Usuario", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                            ExposedDropdownMenuBox(
                                expanded = if (roleLabels.size > 1) expandedRoleMenu else false,
                                onExpandedChange = { if (roleLabels.size > 1) expandedRoleMenu = !expandedRoleMenu },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedRoleLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = PrimaryColor, modifier = Modifier.size(22.dp)) },
                                    trailingIcon = { 
                                        if (roleLabels.size > 1) {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoleMenu)
                                        }
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryColor,
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    enabled = roleLabels.size > 1
                                )
                                if (roleLabels.size > 1) {
                                    ExposedDropdownMenu(
                                        expanded = expandedRoleMenu,
                                        onDismissRequest = { expandedRoleMenu = false }
                                    ) {
                                        roleLabels.forEach { label ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    selectedRoleLabel = label
                                                    expandedRoleMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Text("Información Personal", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 16.sp)

                        val rutDuplicado = viewModel.isRutTaken(rut)
                        FormField(
                            label = "RUT (Identificación)",
                            value = rut,
                            onValueChange = { input -> 
                                rut = input.replace(".", "").replace(" ", "")
                            },
                            placeholder = "12345678-K (sin puntos)",
                            supportingText = if (rutDuplicado) "Este RUT ya está registrado" else "Formato: 12345678-K",
                            isError = rutDuplicado,
                            icon = Icons.Default.Badge
                        )

                        FormField(
                            label = "Nombre Completo",
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "Ej: Juan Perez",
                            icon = Icons.Default.Person
                        )

                        val emailDuplicado = viewModel.isEmailTaken(email)
                        FormField(
                            label = "Correo Electrónico",
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "correo@ejemplo.com",
                            supportingText = if (emailDuplicado) "Este correo ya está en uso" else null,
                            isError = emailDuplicado,
                            icon = Icons.Default.Email
                        )

                        FormField(
                            label = "Número de Teléfono",
                            value = phoneDigits,
                            onValueChange = { if (it.length <= 9) phoneDigits = it.filter { char -> char.isDigit() } },
                            placeholder = "987654321",
                            prefix = "+56 ",
                            supportingText = "Se requieren 9 dígitos (${phoneDigits.length}/9)",
                            icon = Icons.Default.Phone
                        )

                        // --- NUEVO: Visualización y validación de username sugerido ---
                        val suggestedUsername = remember(fullName, rut) {
                            if (fullName.isNotBlank() && rut.isNotBlank()) {
                                val first = fullName.trim().split(" ").firstOrNull()?.lowercase() ?: "user"
                                val rutDigits = rut.filter { it.isDigit() }
                                val first4 = if (rutDigits.length >= 4) rutDigits.take(4) else rutDigits
                                "${first}_$first4"
                            } else ""
                        }
                        
                        val usernameDuplicado = viewModel.isUsernameTaken(suggestedUsername)
                        
                        if (suggestedUsername.isNotBlank()) {
                            Surface(
                                color = if(usernameDuplicado) DangerColor.copy(alpha = 0.1f) else PrimaryColor.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if(usernameDuplicado) Icons.Default.NoAccounts else Icons.Default.AccountCircle, 
                                        contentDescription = null, 
                                        tint = if(usernameDuplicado) DangerColor else PrimaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if(usernameDuplicado) "Username ya existe" else "Usuario que se asignará:",
                                            fontSize = 10.sp,
                                            color = if(usernameDuplicado) DangerColor else TextSecondary
                                        )
                                        Text(
                                            text = suggestedUsername,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if(usernameDuplicado) DangerColor else PrimaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState is CreatePersonnelUiState.Error) {
                item {
                    Surface(
                        color = DangerColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = DangerColor, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = (uiState as CreatePersonnelUiState.Error).message,
                                color = DangerColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                if (uiState is CreatePersonnelUiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else {
                    Button(
                        onClick = { 
                            val roleToSend = rolesMapping[selectedRoleLabel] ?: "GUARD"
                            viewModel.crearPersonal(
                                rut = rut,
                                email = email,
                                fullName = fullName,
                                phoneNumber = "+56$phoneDigits",
                                role = roleToSend
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = formularioValido,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            disabledContainerColor = Color(0xFFE5E7EB)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null)
                        Spacer(Modifier.width(10.dp))
                        Text("REGISTRAR USUARIO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    prefix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if(isError) DangerColor else TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = { Icon(icon, null, tint = if(isError) DangerColor else PrimaryColor, modifier = Modifier.size(20.dp)) },
            prefix = prefix?.let { { Text(it, fontWeight = FontWeight.ExtraBold, color = TextPrimary) } },
            supportingText = supportingText?.let { { Text(it, fontSize = 11.sp, color = if(isError) DangerColor else TextSecondary, fontWeight = FontWeight.Medium) } },
            isError = isError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if(isError) DangerColor else PrimaryColor,
                unfocusedBorderColor = if(isError) DangerColor else Color.DarkGray,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
