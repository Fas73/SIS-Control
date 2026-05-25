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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (token: String, role: String, userId: Long, fullName: String, username: String) -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            val success = state as LoginUiState.Success
            onLoginSuccess(success.token, success.role, success.userId, success.fullName, success.username)
        }
    }

    LoginScreenContent(
        state = state,
        onLoginClick = { user, pass -> viewModel.performLogin(user, pass) },
        onForgotPassword = onForgotPassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .imePadding() // Empuja el contenido hacia arriba cuando sale el teclado
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permite deslizar si el teclado tapa campos
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                "¡Bienvenido!",
                color = TextPrimary, 
                fontSize = 26.sp, 
                fontWeight = FontWeight.Bold
            )
            Text(
                "Inicia sesión para continuar",
                color = TextSecondary, 
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
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
            
            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
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

            Spacer(modifier = Modifier.height(36.dp))

            PrimaryButton(
                text = if (state is LoginUiState.Loading) "Autenticando..." else "Iniciar Sesión",
                onClick = { onLoginClick(username, password) },
                enabled = state !is LoginUiState.Loading && username.isNotBlank() && password.isNotBlank()
            )

            // ── Enlace: ¿Olvidaste tu contraseña? ──────────────────────────
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = PrimaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(
                visible = state is LoginUiState.Error,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (state as? LoginUiState.Error)?.message ?: "", 
                        color = DangerColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DangerColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
