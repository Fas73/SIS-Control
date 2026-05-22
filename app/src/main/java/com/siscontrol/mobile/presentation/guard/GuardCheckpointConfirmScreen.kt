package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Screen_checkpoint_confirm.png
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GuardCheckpointConfirmScreen(
    checkpointName: String = "Punto de Control",
    checkpointNumber: Int = 1,
    installationName: String = "Instalación",
    instruction: String? = null,
    hour: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
    totalCheckpoints: Int = 1,
    completedCheckpoints: Int = 1,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar (same blue header style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    "Escanear Checkpoint",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    installationName, // Mostramos la instalación
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        }

        // Success content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logotipo Corporativo (Simulado)
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null, 
                tint = PrimaryColor.copy(alpha = 0.2f),
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Green checkmark circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(SuccessColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(SuccessColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Verificado",
                        tint = SuccessColor,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "¡Marcaje Verificado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SuccessColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "N° $checkpointNumber: $checkpointName",
                fontSize = 15.sp,
                color = PrimaryColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Instrucción para el guardia (Si existe)
            if (!instruction.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Instrucción del Jefe", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(instruction, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessColor.copy(alpha = 0.06f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessColor.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ConfirmDetailRow("Instalación:", installationName)
                    Spacer(modifier = Modifier.height(12.dp))
                    ConfirmDetailRow("Hora:", hour)
                    Spacer(modifier = Modifier.height(12.dp))
                    ConfirmDetailRow("Progreso:", "$completedCheckpoints/$totalCheckpoints checkpoints")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Continue button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Continuar Ronda",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ConfirmDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
