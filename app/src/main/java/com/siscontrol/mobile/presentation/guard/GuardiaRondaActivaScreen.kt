package com.siscontrol.mobile.presentation.guard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

@Composable
fun GuardiaRondaActivaScreen(
    onFinishRound: () -> Unit,
    onReportIncident: () -> Unit,
    onPanic: () -> Unit
) {
    // Animación de pulso para el radar NFC
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            // Header Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryColor)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ronda en Progreso", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text("Ronda Perimetral", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("14:22", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Radar NFC Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                    // Pulsing rings
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .background(PrimaryVariant.copy(alpha = alpha), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale * 0.7f)
                            .background(PrimaryVariant.copy(alpha = alpha * 1.5f), CircleShape)
                    )
                    // Central Icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(8.dp, CircleShape)
                            .background(PrimaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Nfc, contentDescription = "NFC Scanner", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Acerca el dispositivo al Checkpoint", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Buscando tag NFC...", fontSize = 14.sp, color = TextSecondary)
            }

            // Barra de Progreso General
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progreso de la Ronda", fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("2 / 8", fontWeight = FontWeight.Bold, color = PrimaryVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.25f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PrimaryVariant,
                    trackColor = Color(0xFFE5E7EB)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de Checkpoints
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                item {
                    CheckpointItem(title = "Puerta Principal", time = "14:05", isCompleted = true)
                    CheckpointLine(isCompleted = true)
                    CheckpointItem(title = "Estacionamiento Norte", time = "14:15", isCompleted = true)
                    CheckpointLine(isCompleted = false)
                    CheckpointItem(title = "Bodega Central", time = "Pendiente", isCompleted = false, isActive = true)
                    CheckpointLine(isCompleted = false)
                    CheckpointItem(title = "Salida Emergencia", time = "Pendiente", isCompleted = false)
                    
                    Spacer(modifier = Modifier.height(80.dp)) // Espacio para FAB
                }
            }
            
            // Botones inferiores
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onReportIncident,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningColor.copy(alpha = 0.1f), contentColor = WarningColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Incidente", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onFinishRound,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerColor.copy(alpha = 0.1f), contentColor = DangerColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Text("Finalizar Ronda", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Botón de Pánico
        Button(
            onClick = onPanic,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp) // Arriba del Bottom Bar
                .size(64.dp)
                .shadow(12.dp, CircleShape),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Pánico", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun CheckpointItem(title: String, time: String, isCompleted: Boolean, isActive: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isCompleted) SuccessColor else if (isActive) PrimaryVariant else Color(0xFFE5E7EB), CircleShape)
                .border(2.dp, if (isActive) PrimaryVariant.copy(alpha = 0.3f) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else if (isActive) {
                Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal, color = if (isCompleted || isActive) TextPrimary else TextSecondary, fontSize = 16.sp)
            Text(time, color = if (isCompleted) SuccessColor else TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
fun CheckpointLine(isCompleted: Boolean) {
    Box(modifier = Modifier.padding(start = 15.dp, top = 4.dp, bottom = 4.dp)) {
        Box(modifier = Modifier.width(2.dp).height(24.dp).background(if (isCompleted) SuccessColor else Color(0xFFE5E7EB)))
    }
}
