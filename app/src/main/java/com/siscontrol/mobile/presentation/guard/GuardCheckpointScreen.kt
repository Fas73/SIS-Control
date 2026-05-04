package com.siscontrol.mobile.presentation.guard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Screen_checkpoint.png
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GuardCheckpointScreen(
    checkpointName: String = "Salida de Emergencia",
    checkpointNumber: Int = 4,
    onSimulateScan: () -> Unit
) {
    // Pulse animation for the concentric rings
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_scale"
    )
    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
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
                    "Checkpoint $checkpointNumber: $checkpointName",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated NFC rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer pulsing ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(outerScale)
                        .background(Color(0xFFBFDBFE).copy(alpha = outerAlpha), CircleShape)
                )
                // Middle ring
                Box(
                    modifier = Modifier
                        .size(155.dp)
                        .background(Color(0xFF93C5FD).copy(alpha = 0.25f), CircleShape)
                )
                // Inner filled circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF93C5FD).copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = "NFC",
                        tint = PrimaryColor,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Acerca tu dispositivo",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Coloca tu teléfono cerca del tag NFC del checkpoint",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // NFC hint chip
            Surface(
                color = Color(0xFFFEF9C3),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "El NFC debe estar activado en tu dispositivo",
                        fontSize = 13.sp,
                        color = Color(0xFF854D0E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Simulate scan button
            Button(
                onClick = onSimulateScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Simular Escaneo Exitoso",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Bottom Nav placeholder (visual only)
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
