package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISCard
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.presentation.profile.ProfileViewModel
import com.siscontrol.mobile.core.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun GuardProfileScreen(
    paddingValues: PaddingValues,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.state
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryVariant)))
                    .statusBarsPadding()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mi Perfil", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (state.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = DangerColor)
                }
            } else {
                val user = state.user
                val displayRole = when(user?.role) {
                    "ADMIN" -> "Administrador"
                    "SUPERVISOR" -> "Supervisor"
                    "GUARD", "GUARDIA" -> "Guardia"
                    else -> user?.role ?: "Sin Rol"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar con sombra y borde
                    Surface(
                        modifier = Modifier.size(130.dp),
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(6.dp).background(Color(0xFFF3F4F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(70.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(user?.fullName?.toTitleCase() ?: "Usuario", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SISBadge(
                        displayRole, 
                        containerColor = when(user?.role) {
                            "ADMIN" -> Color(0xFFEDE9FE)
                            "SUPERVISOR" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFD1FAE5)
                        },
                        contentColor = when(user?.role) {
                            "ADMIN" -> PrimaryColor
                            "SUPERVISOR" -> WarningColor
                            else -> SuccessColor
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text("Información de la Cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            
                            ProfileItemRow(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "N/A", color = PrimaryVariant)
                            ProfileItemRow(icon = Icons.Default.Phone, label = "Teléfono", value = user?.phoneNumber ?: "N/A", color = SuccessColor)
                            ProfileItemRow(icon = Icons.Default.DateRange, label = "Miembro desde", value = user?.createdAt.formatDateToDisplay(), color = WarningColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Buttons
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Editar Datos Personales", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Cambiar Contraseña", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Logout
                    TextButton(
                        onClick = { viewModel.logout(onLogout) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, null, tint = DangerColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Cerrar Sesión", color = DangerColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showEditDialog && state.user != null) {
        EditProfileDialogPolished(
            user = state.user!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, username, phone ->
                viewModel.updateProfile(name, username, "+56$phone") { success, msg ->
                    if (success) showEditDialog = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            isSaving = state.isActionLoading
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialogPolished(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { current, new ->
                viewModel.changePassword(current, new) { success, msg ->
                    if (success) showPasswordDialog = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            isSaving = state.isActionLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialogPolished(
    user: com.siscontrol.mobile.data.remote.dto.UserResponseDto,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isSaving: Boolean
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var username by remember { mutableStateOf(user.username) }
    var phoneDigits by remember { mutableStateOf(user.phoneNumber.toPhoneDigits()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(56.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Editar Perfil", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 10.dp)) {
                EditFieldPolished(label = "Nombre Completo", value = fullName, onValueChange = { fullName = it }, icon = Icons.Default.Badge)
                EditFieldPolished(label = "Usuario", value = username, onValueChange = { username = it }, icon = Icons.Default.AccountCircle)
                EditFieldPolished(
                    label = "Teléfono", 
                    value = phoneDigits, 
                    onValueChange = { if (it.length <= 9) phoneDigits = it.filter { c -> c.isDigit() } }, 
                    icon = Icons.Default.Phone,
                    prefix = "+56 ",
                    supportingText = "Ej: 987654321 (${phoneDigits.length}/9)"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fullName, username, phoneDigits) },
                enabled = !isSaving && fullName.isNotBlank() && phoneDigits.length == 9,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                Text("CANCELAR", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialogPolished(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isSaving: Boolean
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(56.dp).background(WarningColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LockReset, null, tint = WarningColor, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Nueva Contraseña", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 10.dp)) {
                PasswordInputPolished(label = "Clave Actual", value = currentPass, onValueChange = { currentPass = it }, visible = passwordVisible)
                PasswordInputPolished(label = "Clave Nueva", value = newPass, onValueChange = { newPass = it }, visible = passwordVisible)
                PasswordInputPolished(label = "Confirmar Clave", value = confirmPass, onValueChange = { confirmPass = it }, visible = passwordVisible)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { passwordVisible = !passwordVisible }) {
                    Checkbox(checked = passwordVisible, onCheckedChange = { passwordVisible = it }, colors = CheckboxDefaults.colors(checkedColor = PrimaryColor))
                    Text("Mostrar caracteres", fontSize = 14.sp, color = TextSecondary)
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = DangerColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPass != confirmPass) errorMsg = "Las claves no coinciden"
                    else if (newPass.length < 4) errorMsg = "Mínimo 4 caracteres"
                    else onConfirm(currentPass, newPass)
                },
                enabled = !isSaving && currentPass.isNotBlank() && newPass.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("ACTUALIZAR CLAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                Text("CANCELAR", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun ProfileItemRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PasswordInputPolished(label: String, value: String, onValueChange: (String) -> Unit, visible: Boolean) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor, modifier = Modifier.size(22.dp)) },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color(0xFFE5E7EB))
        )
    }
}

@Composable
fun EditFieldPolished(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, prefix: String? = null, supportingText: String? = null) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(22.dp)) },
            prefix = prefix?.let { { Text(it, fontWeight = FontWeight.Bold) } },
            supportingText = supportingText?.let { { Text(it, fontSize = 11.sp) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color(0xFFE5E7EB))
        )
    }
}
