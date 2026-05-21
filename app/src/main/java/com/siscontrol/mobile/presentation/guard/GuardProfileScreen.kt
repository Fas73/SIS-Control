package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.presentation.profile.ProfileViewModel
import com.siscontrol.mobile.core.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.siscontrol.mobile.core.CameraUtils
import androidx.compose.ui.draw.clip
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun GuardProfileScreen(
    paddingValues: PaddingValues,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.state
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // LANZADORES
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) selectedImageUri = tempCameraUri
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            CameraUtils.createTempImageUri(context)?.let { uri ->
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundColor // F3F4F6
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Header Azul Vibrante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryVariant)))
                    .statusBarsPadding()
                    .padding(vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mi Perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.Logout, "Salir", tint = Color.White)
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
            } else {
                val user = state.user
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar con BORDE BLANCO para resaltar del fondo
                    Surface(
                        modifier = Modifier.size(140.dp).clickable { showImageSourceDialog = true },
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val img = selectedImageUri ?: user?.imageUrl
                            if (img != null) {
                                AsyncImage(
                                    model = img,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                            }
                            
                            // Botón Cámara Flotante
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).size(38.dp).offset(x = (-4).dp, y = (-4).dp),
                                shape = CircleShape,
                                color = PrimaryColor,
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // TEXTO EN NEGRO ABSOLUTO
                    Text(
                        user?.fullName?.toTitleCase() ?: "Usuario SIS", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Black, 
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SISBadge(
                        if(user?.role?.contains("ADMIN") == true) "Administrador" else "Guardia SIS",
                        containerColor = PrimaryColor.copy(alpha = 0.1f),
                        contentColor = PrimaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    // Tarjeta de Información Blanca
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text("Información de la Cuenta", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryColor, letterSpacing = 1.sp)
                            
                            ProfileItemRow(Icons.Default.Email, "Email", user?.email ?: "N/A", PrimaryVariant)
                            ProfileItemRow(Icons.Default.Phone, "Teléfono", user?.phoneNumber ?: "N/A", SuccessColor)
                            ProfileItemRow(Icons.Default.CalendarToday, "Miembro desde", (user?.createdAt ?: "").formatDateToDisplay(), WarningColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones de Acción Claros
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(10.dp))
                        Text("EDITAR PERFIL", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryColor.copy(alpha = 0.3f))
                    ) {
                        Text("CAMBIAR CONTRASEÑA", color = PrimaryColor, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Actualizar Fotografía", fontWeight = FontWeight.Black, color = TextPrimary) },
            text = { Text("Elija una opción para su foto de perfil.", color = TextSecondary) },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                CameraUtils.createTempImageUri(context)?.let { uri -> tempCameraUri = uri; cameraLauncher.launch(uri) }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            showImageSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Tomar Foto")
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*"); showImageSourceDialog = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Galería")
                    }
                }
            },
            dismissButton = { TextButton(onClick = { showImageSourceDialog = false }) { Text("CANCELAR", color = TextSecondary, fontWeight = FontWeight.Bold) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showEditDialog && state.user != null) {
        EditProfileDialogPolished(state.user!!, { showEditDialog = false }, { n, u, p ->
            viewModel.updateProfile(n, u, "+56$p", selectedImageUri) { s, m ->
                if (s) showEditDialog = false
                scope.launch { snackbarHostState.showSnackbar(m) }
            }
        }, state.isActionLoading)
    }
    
    if (showPasswordDialog) {
        ChangePasswordDialogPolished({ showPasswordDialog = false }, { c, n ->
            viewModel.changePassword(c, n) { s, m ->
                if (s) showPasswordDialog = false
                scope.launch { snackbarHostState.showSnackbar(m) }
            }
        }, state.isActionLoading)
    }
}

@Composable
fun ProfileItemRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialogPolished(user: com.siscontrol.mobile.data.remote.dto.UserResponseDto, onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit, isSaving: Boolean) {
    var fullName by remember { mutableStateOf(user.fullName ?: "") }
    var username by remember { mutableStateOf(user.username ?: "") }
    var phoneDigits by remember { mutableStateOf(user.phoneNumber.toPhoneDigits()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Editar Perfil", fontWeight = FontWeight.Black) }, text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { EditFieldPolished("Nombre Completo", fullName, { fullName = it }, Icons.Default.Badge); EditFieldPolished("Usuario", username, { username = it }, Icons.Default.AccountCircle); EditFieldPolished("Teléfono", phoneDigits, { if (it.length <= 9) phoneDigits = it.filter { c -> c.isDigit() } }, Icons.Default.Phone, "+56 ") } }, confirmButton = { Button(onClick = { onConfirm(fullName, username, phoneDigits) }, enabled = !isSaving && fullName.isNotBlank() && phoneDigits.length == 9, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("GUARDAR") } }, dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("CERRAR", color = TextSecondary) } }, containerColor = Color.White, shape = RoundedCornerShape(24.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialogPolished(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit, isSaving: Boolean) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Seguridad", fontWeight = FontWeight.Black) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { PasswordInputPolished("Clave Actual", currentPass, { currentPass = it }, visible); PasswordInputPolished("Nueva Clave", newPass, { newPass = it }, visible); PasswordInputPolished("Confirmar", confirmPass, { confirmPass = it }, visible); Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { visible = !visible }) { Checkbox(visible, { visible = it }); Text("Ver claves", fontSize = 12.sp) }; err?.let { Text(it, color = DangerColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) } } }, confirmButton = { Button(onClick = { if (newPass != confirmPass) err = "No coinciden" else onConfirm(currentPass, newPass) }, modifier = Modifier.fillMaxWidth()) { Text("ACTUALIZAR") } }, dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("CERRAR") } }, containerColor = Color.White, shape = RoundedCornerShape(24.dp))
}

@Composable
fun PasswordInputPolished(l: String, v: String, onV: (String) -> Unit, vis: Boolean) { Column { Text(l, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary); OutlinedTextField(v, onV, leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)) } }

@Composable
fun EditFieldPolished(l: String, v: String, onV: (String) -> Unit, i: androidx.compose.ui.graphics.vector.ImageVector, p: String? = null) { Column { Text(l, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary); OutlinedTextField(v, onV, leadingIcon = { Icon(i, null, tint = PrimaryColor) }, prefix = p?.let { { Text(it, fontWeight = FontWeight.Bold, color = TextPrimary) } }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)) } }
