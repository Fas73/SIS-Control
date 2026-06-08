package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toPhoneDigits
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    userId: Long,
    viewModel: AdminManagementViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state
    val editingUser by viewModel.editingUser
    
    // Cargar datos del usuario
    LaunchedEffect(userId) {
        viewModel.getUserById(userId)
    }

    var fullName by remember(editingUser) { mutableStateOf(editingUser?.fullName ?: "") }
    var username by remember(editingUser) { mutableStateOf(editingUser?.username ?: "") }
    var email by remember(editingUser) { mutableStateOf(editingUser?.email ?: "") }
    var phoneDigits by remember(editingUser) { mutableStateOf(editingUser?.phoneNumber.toPhoneDigits()) }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
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
                        text = "Editar Usuario",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        if (state.isLoading || editingUser == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Datos Personales", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 16.sp)
                        
                        EditFieldPolished(
                            label = "Nombre Completo", 
                            value = fullName, 
                            onValueChange = { fullName = it },
                            icon = Icons.Default.Person
                        )
                        
                        EditFieldPolished(
                            label = "Nombre de Usuario", 
                            value = username, 
                            onValueChange = { username = it },
                            icon = Icons.Default.Badge
                        )
                        
                        EditFieldPolished(
                            label = "Correo Electrónico", 
                            value = email, 
                            onValueChange = { email = it },
                            icon = Icons.Default.Email
                        )
                        
                        EditFieldPolished(
                            label = "Número de Teléfono", 
                            value = phoneDigits, 
                            onValueChange = { if (it.length <= 9) phoneDigits = it.filter { c -> c.isDigit() } },
                            placeholder = "987654321",
                            prefix = "+56 ",
                            icon = Icons.Default.Phone
                        )
                        
                        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Text("Seguridad", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 16.sp)
                        EditFieldPolished(
                            label = "Nueva Contraseña (opcional)", 
                            value = password, 
                            onValueChange = { password = it },
                            placeholder = "Ingresa nueva clave...",
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible },
                            icon = Icons.Default.Lock
                        )
                    }
                }

                if (state.error != null) {
                    Surface(
                        color = DangerColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = state.error!!,
                            color = DangerColor,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        val request = UserRequestDto(
                            rut = (editingUser?.rut ?: "").replace(".", ""),
                            username = username,
                            email = email,
                            fullName = fullName,
                            password = if (password.isNotBlank()) password else "pass123",
                            phoneNumber = "+56$phoneDigits",
                            role = editingUser?.role ?: "GUARD"
                        )
                        viewModel.updateUser(userId, request, onSuccess)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isActionLoading && fullName.isNotBlank() && username.isNotBlank() && email.isNotBlank() && phoneDigits.length == 9
                ) {
                    if (state.isActionLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(10.dp))
                        Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EditFieldPolished(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String = "",
    prefix: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = { Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(22.dp)) },
            placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder, color = Color.Gray) } } else null,
            prefix = if (prefix != null) { { Text(prefix, fontWeight = FontWeight.Bold, color = TextPrimary) } } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword && onPasswordToggle != null) {
                {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = onPasswordToggle) {
                        Icon(image, null, modifier = Modifier.size(22.dp), tint = PrimaryColor)
                    }
                }
            } else null,
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
