package com.siscontrol.mobile.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.siscontrol.mobile.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.ButtonVariant
import com.siscontrol.mobile.presentation.components.PrimaryButton
import com.siscontrol.mobile.presentation.theme.*

import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (token: String, role: String, userId: Long, fullName: String, username: String) -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val isUsernameVerified by viewModel.isUsernameVerified.collectAsState()
    val isVerifyingUsername by viewModel.isVerifyingUsername.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState()
    val isQuickLoginMode by viewModel.isQuickLoginMode.collectAsState()
    val lastFullName by viewModel.lastFullName.collectAsState()
    val lastUsername by viewModel.lastUsername.collectAsState()
    
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            val success = state as LoginUiState.Success
            onLoginSuccess(success.token, success.role, success.userId, success.fullName, success.username)
        }
    }

    LoginScreenContent(
        state = state,
        isUsernameVerified = isUsernameVerified,
        isVerifyingUsername = isVerifyingUsername,
        usernameError = usernameError,
        isQuickLoginMode = isQuickLoginMode,
        lastFullName = lastFullName,
        lastUsername = lastUsername,
        onVerifyUsername = { viewModel.verifyUsername(it) },
        onResetUsernameVerification = { viewModel.resetUsernameVerification() },
        onSwitchToNormalLogin = { viewModel.switchToNormalLogin() },
        onResetLoginState = { viewModel.resetLoginState() },
        onLoginClick = { user, pass -> 
            val finalUser = if (isQuickLoginMode) lastUsername ?: user else user
            viewModel.performLogin(finalUser, pass) 
        },
        onForgotPassword = onForgotPassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginUiState,
    isUsernameVerified: Boolean,
    isVerifyingUsername: Boolean,
    usernameError: String?,
    isQuickLoginMode: Boolean,
    lastFullName: String?,
    lastUsername: String?,
    onVerifyUsername: (String) -> Unit,
    onResetUsernameVerification: () -> Unit,
    onSwitchToNormalLogin: () -> Unit,
    onResetLoginState: () -> Unit,
    onLoginClick: (String, String) -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showQuickLoginPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permite deslizar si el teclado tapa campos
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isQuickLoginMode) {
                Image(
                    painter = painterResource(id = R.drawable.logo_branding_sis_control),
                    contentDescription = "Logo SIS-Control",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val firstName = (lastFullName ?: lastUsername ?: "").split(" ").firstOrNull() ?: ""
                Text(
                    "Hola $firstName", 
                    color = TextPrimary, 
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Animación sutil de entrada para el Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_branding_sis_control),
                    contentDescription = "Logo SIS-Control",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "Bienvenido de vuelta", 
                    color = TextPrimary, 
                    fontSize = 26.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Inicia sesión en tu cuenta para continuar", 
                    color = TextSecondary, 
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))

            if (!isQuickLoginMode) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        if (isUsernameVerified || usernameError != null) onResetUsernameVerification() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Usuario o Correo", color = TextSecondary, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryColor) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            }
            
            if (!isQuickLoginMode) {
                AnimatedVisibility(
                    visible = !usernameError.isNullOrEmpty(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = usernameError ?: "", 
                            color = DangerColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DangerColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, DangerColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isUsernameVerified || (isQuickLoginMode && showQuickLoginPassword),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña", color = TextSecondary, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryColor) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, null, tint = PrimaryColor)
                            }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            if (!isUsernameVerified && !isQuickLoginMode) {
                PrimaryButton(
                    text = if (isVerifyingUsername) "Verificando..." else "Siguiente",
                    onClick = { onVerifyUsername(username) },
                    enabled = !isVerifyingUsername && username.isNotBlank()
                )
            } else if (isQuickLoginMode && !showQuickLoginPassword) {
                PrimaryButton(
                    text = "Entrar",
                    onClick = { showQuickLoginPassword = true },
                    enabled = true
                )
            } else {
                PrimaryButton(
                    text = if (state is LoginUiState.Loading) "Autenticando..." else if (isQuickLoginMode) "Entrar" else "Iniciar Sesión",
                    onClick = { onLoginClick(username, password) },
                    enabled = state !is LoginUiState.Loading && password.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Enlace: Recuperar Contraseña ──────────────────────────
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recuperar contraseña",
                    color = PrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isQuickLoginMode) {
                val firstName = (lastFullName ?: lastUsername ?: "").split(" ").firstOrNull() ?: ""
                TextButton(
                    onClick = { 
                        showQuickLoginPassword = false
                        onSwitchToNormalLogin() 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No soy $firstName",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // La visualización de errores (LoginUiState.Error) ahora se maneja con un Dialog más arriba,
            // pero mantenemos el padding final
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Hermoso Cuadro de Alerta (Dialog) para Contraseña Incorrecta ──
        if (state is LoginUiState.Error) {
            val errorMessage = (state as LoginUiState.Error).message
            AlertDialog(
                onDismissRequest = { onResetLoginState() },
                properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = DangerColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acceso Denegado",
                            fontWeight = FontWeight.ExtraBold,
                            color = DangerColor,
                            fontSize = 20.sp
                        )
                    }
                },
                text = {
                    Text(
                        text = if (errorMessage.contains("credenciales", ignoreCase = true) || errorMessage.contains("incorrecta", ignoreCase = true)) 
                            "La contraseña que has ingresado es incorrecta. Por favor, verifica tus datos y vuelve a intentarlo." 
                            else errorMessage,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    com.siscontrol.mobile.presentation.components.PrimaryButton(
                        text = "Volver a intentar",
                        onClick = {
                            password = "" // Limpiar el campo para que vuelva a intentar
                            onResetLoginState()
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).height(48.dp)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                tonalElevation = 8.dp
            )
        }
    }
}
