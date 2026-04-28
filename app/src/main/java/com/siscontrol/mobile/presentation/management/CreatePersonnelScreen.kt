package com.siscontrol.mobile.presentation.management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.CreatePersonnelUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel para la pantalla de creación de personal.
 *
 * Responsabilidades:
 *  1. Cargar la lista de instalaciones disponibles desde el backend,
 *     para que el usuario pueda elegir a cuáles asignar al nuevo empleado.
 *  2. Orquestar el caso de uso [CreatePersonnelUseCase] enviando el
 *     [UserRequestDto] con la lista de IDs seleccionados (ManyToMany).
 *
 * Vinculación con las instalaciones:
 *  - El campo [UserRequestDto.installationIds] viaja como List<Long> hacia
 *    el backend, donde UserService.createUser() lo resuelve en objetos
 *    Installation y los persiste en la tabla intermedia `user_installations`.
 */
class CreatePersonnelViewModel(
    private val createPersonnelUseCase: CreatePersonnelUseCase,
    private val getInstallationsUseCase: GetInstallationsUseCase
) : ViewModel() {

    /** Estado de la operación de creación */
    private val _createState = MutableStateFlow<CreatePersonnelUiState>(CreatePersonnelUiState.Idle)
    val createState: StateFlow<CreatePersonnelUiState> = _createState

    /** Lista de instalaciones disponibles para seleccionar */
    private val _installations = MutableStateFlow<List<InstallationDto>>(emptyList())
    val installations: StateFlow<List<InstallationDto>> = _installations

    /** Conjunto de IDs de instalaciones seleccionadas por el usuario */
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    init {
        // Al crear el ViewModel, cargamos la lista de recintos disponibles
        cargarInstalaciones()
    }

    /**
     * Recupera todas las instalaciones del sistema para llenar el selector múltiple.
     * En caso de error la lista permanece vacía (el formulario igual será válido,
     * aunque no se asignen instalaciones al usuario).
     */
    private fun cargarInstalaciones() {
        viewModelScope.launch {
            getInstallationsUseCase().onSuccess { lista ->
                _installations.value = lista
            }
        }
    }

    /**
     * Alterna la selección de una instalación:
     * - Si ya estaba seleccionada, la deselecciona.
     * - Si no estaba, la añade al conjunto de seleccionados.
     * Esto permite la asignación Multiple (ManyToMany) en el modelo de datos.
     */
    fun toggleInstalacion(id: Long) {
        _selectedIds.value = if (_selectedIds.value.contains(id)) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
    }

    /**
     * Construye el [UserRequestDto] con la lista de IDs seleccionados y lo
     * envía al backend a través de [CreatePersonnelUseCase].
     *
     * La tabla intermedia `user_installations` del backend vinculará al nuevo
     * usuario con cada una de las instalaciones indicadas en [installationIds].
     *
     * @param username  Nombre de usuario único.
     * @param email     Email válido.
     * @param fullName  Nombre completo visible en la app.
     * @param password  Contraseña en texto plano (el backend la cifrará con BCrypt).
     * @param role      Rol deseado: "SUPERVISOR" si el creador es ADMIN, "GUARD" si el creador es SUPERVISOR.
     */
    fun crearPersonal(
        username: String,
        email: String,
        fullName: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            _createState.value = CreatePersonnelUiState.Loading

            val request = UserRequestDto(
                username = username,
                email = email,
                fullName = fullName,
                password = password,
                role = role,
                // Convertimos el Set de IDs seleccionados a List<Long> para el DTO
                installationIds = _selectedIds.value.toList()
            )

            createPersonnelUseCase(request).fold(
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
}

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

sealed class CreatePersonnelUiState {
    /** Estado inicial, sin acción en curso */
    object Idle : CreatePersonnelUiState()
    /** Petición en vuelo — mostrar spinner */
    object Loading : CreatePersonnelUiState()
    /** Creación exitosa */
    data class Success(val user: UserResponseDto) : CreatePersonnelUiState()
    /** Error de red/negocio */
    data class Error(val message: String) : CreatePersonnelUiState()
}

// ---------------------------------------------------------------------------
// Composable Screen
// ---------------------------------------------------------------------------

/**
 * Pantalla de creación de nuevo personal.
 *
 * Casos de uso cubiertos:
 *  - Un ADMIN puede crear SUPERVISORES y asignarles MÚLTIPLES instalaciones.
 *  - Un SUPERVISOR puede crear GUARDIAS y asignarles las instalaciones donde trabajarán.
 *
 * El campo de instalaciones usa un selector de tipo "chips" que refleja la
 * relación ManyToMany: se puede marcar/desmarcar cada instalación libremente.
 *
 * @param viewModel       ViewModel inyectado con sus casos de uso.
 * @param currentUserRole Rol del usuario logueado, determina el rol que se puede crear.
 * @param onSuccess       Callback invocado al crear el usuario con éxito.
 * @param onBack          Callback para navegar atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePersonnelScreen(
    viewModel: CreatePersonnelViewModel,
    currentUserRole: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // ---- Campos del formulario ----
    var username  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var fullName  by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }

    // El rol a crear se deduce del rol actual:
    // ADMIN crea SUPERVISOR | SUPERVISOR crea GUARD
    val rolACrear = if (currentUserRole == "ADMIN") "SUPERVISOR" else "GUARD"

    // Estado del selector de instalaciones
    var expandidoSelector by remember { mutableStateOf(false) }

    val uiState       by viewModel.createState.collectAsState()
    val installations by viewModel.installations.collectAsState()
    val selectedIds   by viewModel.selectedIds.collectAsState()

    // Cuando la operación es exitosa, notificamos y salimos
    LaunchedEffect(uiState) {
        if (uiState is CreatePersonnelUiState.Success) {
            onSuccess()
        }
    }

    // ---- Validación básica del formulario ----
    val formularioValido = username.isNotBlank()
        && email.isNotBlank()
        && fullName.isNotBlank()
        && password.length >= 6

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
                        text = "Registrar ${if (rolACrear == "SUPERVISOR") "Supervisor" else "Guardia"}",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // --- Cabecera informativa ---
            SectionHeader(titulo = "Datos del Nuevo ${ if (rolACrear == "SUPERVISOR") "Supervisor" else "Guardia" }")

            // --- Campos de texto ---
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nombre Completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mín. 6 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // --- Selector múltiple de instalaciones (ManyToMany) ---
            SectionHeader(titulo = "Asignación de Instalaciones")

            Text(
                text = "Selecciona una o más instalaciones donde trabajará este empleado. " +
                       "El modelo permite asignación múltiple para supervisores y guardias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            InstallationMultiSelector(
                installations = installations,
                selectedIds = selectedIds,
                expandido = expandidoSelector,
                onToggleExpanded = { expandidoSelector = !expandidoSelector },
                onToggleInstallation = { viewModel.toggleInstalacion(it) }
            )

            // --- Chips de instalaciones seleccionadas ---
            if (selectedIds.isNotEmpty()) {
                ChipsInstalaciones(
                    instalaciones = installations.filter { it.id in selectedIds }
                )
            }

            Divider()

            // --- Error ---
            if (uiState is CreatePersonnelUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as CreatePersonnelUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // --- Botón de acción principal ---
            if (uiState is CreatePersonnelUiState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                com.siscontrol.mobile.presentation.components.PrimaryButton(
                    text = "Guardar ${ if (rolACrear == "SUPERVISOR") "Supervisor" else "Guardia" }",
                    onClick = {
                        viewModel.crearPersonal(username, email, fullName, password, rolACrear)
                    },
                    enabled = formularioValido
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Componentes auxiliares
// ---------------------------------------------------------------------------

/**
 * Encabezado de sección con separador visual.
 */
@Composable
private fun SectionHeader(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Selector expandible con checkboxes para selección múltiple de instalaciones.
 *
 * La relación ManyToMany se modela aquí: el usuario puede marcar N instalaciones
 * y los IDs se acumulan en [selectedIds] dentro del ViewModel.
 *
 * @param installations      Lista completa de instalaciones disponibles.
 * @param selectedIds        Conjunto de IDs actualmente seleccionados.
 * @param expandido          Si el panel desplegable está visible.
 * @param onToggleExpanded   Callback para abrir/cerrar el panel.
 * @param onToggleInstallation Callback que recibe el ID a activar/desactivar.
 */
@Composable
private fun InstallationMultiSelector(
    installations: List<InstallationDto>,
    selectedIds: Set<Long>,
    expandido: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleInstallation: (Long) -> Unit
) {
    val resumen = when {
        installations.isEmpty() -> "Cargando instalaciones…"
        selectedIds.isEmpty()   -> "Ninguna instalación seleccionada"
        else                    -> "${selectedIds.size} instalación(es) seleccionada(s)"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        // Fila de cabecera (clic para expandir/colapsar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = installations.isNotEmpty(), onClick = onToggleExpanded)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(resumen, style = MaterialTheme.typography.bodyMedium)
            Icon(
                imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expandido) "Colapsar" else "Expandir"
            )
        }

        // Lista desplegable con checkboxes
        if (expandido && installations.isNotEmpty()) {
            Divider()
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
            ) {
                items(installations) { inst ->
                    val seleccionada = inst.id in selectedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleInstallation(inst.id) }
                            .background(
                                if (seleccionada) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = inst.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (inst.address.isNotBlank()) {
                                Text(
                                    text = inst.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (seleccionada) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionada",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Muestra los chips de las instalaciones seleccionadas.
 * Usa FlowRow manual (compatible sin librería extra).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsInstalaciones(instalaciones: List<InstallationDto>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        instalaciones.forEach { inst ->
            SuggestionChip(
                onClick = {},
                label = { Text(inst.name) }
            )
        }
    }
}
