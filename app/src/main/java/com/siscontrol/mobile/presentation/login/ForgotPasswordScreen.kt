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
 * En esta iteración, el envío es simulado. Para conectar con el backend:
 * 1. Crear [ForgotPasswordViewModel] que exponga un [StateFlow<ForgotPasswordUiState>].
 * 2. Reemplazar [onSendInstructions] por viewModel.sendResetEmail(email).
 * 3. Observar el estado desde [AppNavigation] o elevar al ViewModel.
 *
 * @param onBack               Navega de vuelta al Login.
 * @param onSendInstructions   Callback con el email ingresado (simulado por ahora).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSendInstructions: (email: String) -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }

    // Estado UI local — reemplazar con ViewModel cuando se conecte el backend
    var uiState by remember { mutableStateOf<ForgotPasswordUiState>(ForgotPasswordUiState.Idle) }

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
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingresa el correo de tu cuenta y te enviaremos\ninstrucciones para restablecerla.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Campo de email ────────────────────────────────────────────
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; uiState = ForgotPasswordUiState.Idle },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !isSuccess
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Botón principal ───────────────────────────────────────────
                PrimaryButton(
                    text = when {
                        isLoading -> "Enviando..."
                        isSuccess -> "¡Enviado!"
                        else      -> "Enviar instrucciones"
                    },
                    onClick = {
                        if (isEmailValid && !isLoading && !isSuccess) {
                            // Simulación: marcar como éxito inmediatamente
                            // TODO (producción): uiState = Loading → llamar ViewModel
                            uiState = ForgotPasswordUiState.Success
                            onSendInstructions(email)
                        }
                    },
                    enabled = isEmailValid && !isLoading && !isSuccess
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Mensaje de confirmación simulado ─────────────────────────
                AnimatedVisibility(
                    visible = isSuccess,
                    enter = fadeIn() + slideInVertically(),
                    exit  = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                SuccessColor.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Instrucciones enviadas",
                                color = SuccessColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Revisa tu bandeja de entrada en $email",
                                color = SuccessColor.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // ── Mensaje de error (futuro backend) ─────────────────────────
                AnimatedVisibility(
                    visible = uiState is ForgotPasswordUiState.Error,
                    enter = fadeIn() + slideInVertically(),
                    exit  = fadeOut()
                ) {
                    Text(
                        text = (uiState as? ForgotPasswordUiState.Error)?.message ?: "",
                        color = DangerColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DangerColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Volver al Login ───────────────────────────────────────────
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
