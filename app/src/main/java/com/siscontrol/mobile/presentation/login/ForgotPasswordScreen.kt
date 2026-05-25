package com.siscontrol.mobile.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.PrimaryButton
import com.siscontrol.mobile.presentation.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// UiState — Base preparada para conexión con backend
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Estado de la pantalla de recuperación de contraseña.
 *
 * TODO (producción): Conectar con un UseCase que llame al endpoint
 *   POST /api/auth/forgot-password { "email": "..." }
 */
sealed interface ForgotPasswordUiState {
    data object Idle    : ForgotPasswordUiState
    data object Loading : ForgotPasswordUiState
    data object Success : ForgotPasswordUiState
    data class  Error(val message: String) : ForgotPasswordUiState
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable principal — Stateless
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de recuperación de contraseña (stateless).
 *
 * Envía una solicitud de soporte al Administrador.
 *
 * @param onBack               Navega de vuelta al Login.
 * @param viewModel            ViewModel para gestionar la lógica de envío.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState

    val isEmailValid = email.contains("@") && email.contains(".")
    val isLoading    = uiState is ForgotPasswordUiState.Loading
    val isSuccess    = uiState is ForgotPasswordUiState.Success

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Login"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = PrimaryColor
                )
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // ── Ícono decorativo ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Email,
                        contentDescription = null,
                        tint = if (isSuccess) SuccessColor else PrimaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isSuccess) "¡Solicitud Recibida!" else "¿Olvidaste tu contraseña?",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSuccess) 
                        "Tu solicitud ha sido enviada al Administrador. Él se contactará contigo para entregarte una clave temporal."
                        else "Ingresa el correo de tu cuenta y enviaremos una notificación al Administrador para ayudarte.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Campo de email ────────────────────────────────────────────
                if (!isSuccess) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            if (uiState is ForgotPasswordUiState.Error) viewModel.resetState() 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo electrónico", color = TextSecondary, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Botón principal ───────────────────────────────────────────
                    PrimaryButton(
                        text = if (isLoading) "Enviando..." else "Solicitar Ayuda",
                        onClick = {
                            if (isEmailValid && !isLoading) {
                                viewModel.enviarSolicitudSoporte(email)
                            }
                        },
                        enabled = isEmailValid && !isLoading
                    )
                } else {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                    ) {
                        Text("VOLVER AL INICIO", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Mensaje de error ─────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState is ForgotPasswordUiState.Error,
                    enter = fadeIn() + slideInVertically(),
                    exit  = fadeOut()
                ) {
                    Text(
                        text = (uiState as? ForgotPasswordUiState.Error)?.message ?: "",
                        color = DangerColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DangerColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Volver al Login ───────────────────────────────────────────
                if (!isSuccess) {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "← Volver al inicio de sesión",
                            color = PrimaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
