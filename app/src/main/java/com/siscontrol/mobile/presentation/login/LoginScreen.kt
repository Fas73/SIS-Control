package com.siscontrol.mobile.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (token: String, role: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            val success = state as LoginUiState.Success
            onLoginSuccess(success.token, success.role)
        }
    }

    LoginScreenContent(
        state = state,
        onLoginClick = { user, pass -> viewModel.performLogin(user, pass) },
        onDemoClick = { role -> 
            // Para la demo, podemos simular un login exitoso directo o intentar con credenciales predefinidas
            // Asumiendo que el ViewModel tiene soporte o simulamos aquí
            val fakeToken = "demo-token-$role"
            onLoginSuccess(fakeToken, role)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onDemoClick: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animación sutil de entrada para el Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(16.dp, shape = CircleShape, spotColor = PrimaryColor.copy(alpha = 0.5f))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryColor, PrimaryVariant)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SIS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    letterSpacing = 2.sp
                )
            }
            
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
            
            Spacer(modifier = Modifier.height(40.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
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
                    label = { Text("Contraseña", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryColor) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
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
            
            AnimatedVisibility(
                visible = state is LoginUiState.Error,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
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

            Spacer(modifier = Modifier.weight(1f))

            Text("Demo: Probar como", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onDemoClick("ADMIN") }, 
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor.copy(alpha = 0.1f), contentColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    elevation = null
                ) { Text("ADMIN", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { onDemoClick("SUPERVISOR") }, 
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor.copy(alpha = 0.1f), contentColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    elevation = null
                ) { Text("SUP", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { onDemoClick("GUARDIA") }, 
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor.copy(alpha = 0.1f), contentColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    elevation = null
                ) { Text("GUARD", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
