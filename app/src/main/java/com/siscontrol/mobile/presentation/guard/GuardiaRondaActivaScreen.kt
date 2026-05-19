package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.MainActivity
import com.siscontrol.mobile.core.toTitleCase
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GuardiaRondaActivaScreen(
    roundId: Long,
    installationName: String,
    viewModel: GuardRoundViewModel,
    onFinishRound: () -> Unit,
    onReportIncident: () -> Unit,
    onPanic: () -> Unit,
    onScanCheckpoint: (CheckpointDto) -> Unit = {}
) {
    var showPanicDialog by rememberSaveable { mutableStateOf(false) }
    var observations by rememberSaveable { mutableStateOf("") }
    val state by viewModel.state

    val nextCheckpoint = state.checkpoints
        .sortedBy { it.executionOrder }
        .firstOrNull { it.id !in state.executedCheckpointIds }

    // Escucha de NFC
    LaunchedEffect(Unit) {
        MainActivity.nfcTagFlow.collectLatest { tagId ->
            viewModel.scanNfcTag(roundId, tagId) { checkpoint ->
                onScanCheckpoint(checkpoint)
            }
        }
    }

    if (showPanicDialog) {
        PanicAlertDialog(
            onConfirm = {
                showPanicDialog = false
                onPanic()
            },
            onDismiss = { showPanicDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Ronda en Progreso",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                installationName,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Surface(
                    color = SuccessColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "En Ronda",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Stats Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryVariant)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("${state.executedCheckpointIds.size}/${state.checkpoints.size}", "Checkpoints")
                StatDivider()
                StatItem(viewModel.getMinutesPassed().toString(), "Minutos")
                StatDivider()
                StatItem(state.distanceTravelled.toString(), "Metros")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (nextCheckpoint != null) SuccessColor else PrimaryColor.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val titleText = when {
                            state.checkpoints.isEmpty() && !state.isLoadingCheckpoints -> "Instalación sin puntos"
                            nextCheckpoint != null -> "Siguiente Checkpoint"
                            else -> "Ronda Completada"
                        }
                        
                        val subtitleText = when {
                            state.checkpoints.isEmpty() && !state.isLoadingCheckpoints -> "No hay tags NFC configurados para esta sede."
                            nextCheckpoint != null -> nextCheckpoint.name
                            else -> "¡Todos los puntos verificados!"
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                tint = if (nextCheckpoint != null) Color.White else if(state.checkpoints.isEmpty()) DangerColor else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = titleText,
                                color = if (nextCheckpoint != null) Color.White else if(state.checkpoints.isEmpty()) DangerColor else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitleText,
                            color = if (nextCheckpoint != null) Color.White else TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (nextCheckpoint != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Nfc, null, tint = Color.White)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Acerca el teléfono al Tag NFC", color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onReportIncident,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (nextCheckpoint != null) Color.White else PrimaryColor),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (nextCheckpoint != null) Color.White.copy(alpha = 0.6f) else PrimaryColor.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reportar Incidente", fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    "Progreso de Ronda",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
                )
            }

            if (state.isLoadingCheckpoints) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
            } else {
                items(items = state.checkpoints.sortedBy { it.executionOrder }) { checkpoint ->
                    val isCompleted = checkpoint.id in state.executedCheckpointIds
                    val isActive = checkpoint.id == nextCheckpoint?.id
                    
                    CheckpointListItem(
                        number = checkpoint.executionOrder,
                        title = checkpoint.name.toTitleCase(),
                        time = if (isCompleted) {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        } else "Pendiente",
                        instruction = if (isActive) checkpoint.instruction else null,
                        isCompleted = isCompleted,
                        isActive = isActive
                    )
                }
            }

            item {
                Text(
                    "Observaciones Finales",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    placeholder = { Text("Novedades detectadas...", color = TextSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentSize(Alignment.Center), color = PrimaryColor)
                } else {
                    Button(
                        onClick = { viewModel.endRound(roundId, observations, onFinishRound) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessColor,
                            disabledContainerColor = Color.LightGray
                        ),
                        enabled = state.checkpoints.isNotEmpty() && state.executedCheckpointIds.size == state.checkpoints.size,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Finalizar Ronda", fontWeight = FontWeight.Bold)
                    }
                }
                if (state.error != null) {
                    Text(state.error!!, color = DangerColor, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── BOTÓN DE PÁNICO (full-width, sticky bottom) ────────────────────
        Button(
            onClick = { showPanicDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Pánico",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "BOTÓN DE PÁNICO",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Panic Alert Dialog  (matches Screen_alert.png)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PanicAlertDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Alert icon circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(DangerColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(2.dp, DangerColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Activar Alerta de Emergencia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Se notificará inmediatamente a todos los supervisores y administradores",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Confirmar Emergencia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancelar", fontSize = 15.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper composables
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
fun CheckpointListItem(
    number: Int?,
    title: String,
    time: String,
    instruction: String? = null,
    isCompleted: Boolean,
    isActive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> SuccessColor.copy(alpha = 0.08f)
                isActive    -> PrimaryColor.copy(alpha = 0.04f)
                else        -> Color.White
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isCompleted -> SuccessColor.copy(alpha = 0.2f)
                isActive    -> PrimaryColor.copy(alpha = 0.2f)
                else        -> Color(0xFFF3F4F6)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Leading circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            when {
                                isCompleted -> SuccessColor
                                isActive    -> PrimaryColor
                                else        -> Color(0xFFF3F4F6)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else if (number != null) {
                        Text(number.toString(), color = if(isActive) Color.White else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = if (isCompleted || isActive) TextPrimary else TextSecondary,
                        fontWeight = if (isCompleted || isActive) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = if (isCompleted) SuccessColor else TextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(time, color = if (isCompleted) SuccessColor else TextSecondary, fontSize = 12.sp)
                    }
                }
            }
            
            // Instrucción visible solo si el punto está activo y tiene contenido
            if (isActive && !instruction.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = PrimaryColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(start = 44.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, null, tint = PrimaryColor, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(instruction, fontSize = 12.sp, color = PrimaryColor, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}
