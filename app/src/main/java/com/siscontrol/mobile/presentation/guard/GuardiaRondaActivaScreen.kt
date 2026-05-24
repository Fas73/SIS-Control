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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.MainActivity
import com.siscontrol.mobile.core.toTitleCase
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.siscontrol.mobile.core.CameraUtils
import coil.compose.AsyncImage
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun GuardiaRondaActivaScreen(
    paddingValues: PaddingValues,
    roundId: Long,
    installationName: String,
    viewModel: GuardRoundViewModel,
    onFinishRound: () -> Unit,
    onReportIncident: () -> Unit,
    onPanic: () -> Unit,
    onScanCheckpoint: (Checkpoint, Int, Int) -> Unit = { _, _, _ -> }
) {
    var showPanicDialog by rememberSaveable { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf(false) }
    var skipReason by remember { mutableStateOf("") }
    var observations by rememberSaveable { mutableStateOf("") }
    
    val context = LocalContext.current
    
    // GUARDAR URIS COMO STRING PARA EVITAR FALLOS DE CÁMARA
    var skipImageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCameraUriStr by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUriStr != null) {
            skipImageUriStr = tempCameraUriStr
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            CameraUtils.createTempImageUri(context)?.let { uri ->
                tempCameraUriStr = uri.toString()
                cameraLauncher.launch(uri)
            }
        }
    }

    val state by viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    
    val nextCheckpoint = state.checkpoints
        .sortedBy { it.executionOrder }
        .firstOrNull { it.id !in state.executedCheckpointIds }

    val isAllCheckpointsDone = state.checkpoints.isNotEmpty() && 
                             state.executedCheckpointIds.size == state.checkpoints.size

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it) }
        state.error?.let { if (!it.contains("Etiqueta incorrecta")) snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(Unit) {
        MainActivity.nfcTagFlow.collectLatest { tagId ->
            viewModel.scanNfcTag(roundId, tagId) { checkpoint ->
                onScanCheckpoint(checkpoint, state.executedCheckpointIds.size, state.checkpoints.size)
            }
        }
    }

    if (showPanicDialog) {
        PanicAlertDialog(onConfirm = {
            showPanicDialog = false
            viewModel.triggerPanicAlert(roundId)
            onPanic()
        }, onDismiss = { showPanicDialog = false })
    }

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false; skipReason = ""; skipImageUriStr = null },
            title = { Text("Omitir Punto de Control", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Justifica por qué no puedes escanear este punto.", color = TextSecondary)
                    OutlinedTextField(
                        value = skipReason,
                        onValueChange = { skipReason = it },
                        placeholder = { Text("Ej: Tag dañado", color = TextPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray
                        )
                    )

                    if (skipImageUriStr != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))) {
                            AsyncImage(model = Uri.parse(skipImageUriStr), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            IconButton(onClick = { skipImageUriStr = null }, modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape).size(32.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    CameraUtils.createTempImageUri(context)?.let { uri ->
                                        tempCameraUriStr = uri.toString()
                                        cameraLauncher.launch(uri)
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryVariant)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = PrimaryVariant)
                            Spacer(Modifier.width(8.dp))
                            Text("Capturar Evidencia", color = PrimaryVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = skipImageUriStr?.let { Uri.parse(it) }
                        viewModel.skipCheckpoint(roundId, skipReason, uri) {
                            showSkipDialog = false
                            skipReason = ""
                            skipImageUriStr = null
                        }
                    },
                    enabled = skipReason.isNotBlank() && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("CONFIRMAR", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = { TextButton(onClick = { showSkipDialog = false; skipReason = ""; skipImageUriStr = null }) { Text("CANCELAR", color = TextSecondary) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
            // Header
            Box(modifier = Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(PrimaryColor, PrimaryVariant))).statusBarsPadding().padding(horizontal = 16.dp, vertical = 18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Ronda en Progreso", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(installationName, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                        }
                    }
                    Surface(color = SuccessColor, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))) {
                        Text("ACTIVA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("${state.executedCheckpointIds.size}/${state.checkpoints.size}", "Puntos", PrimaryVariant)
                    StatDivider()
                    StatItem(viewModel.getMinutesPassed().toString(), "Minutos", WarningColor)
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 140.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    // ── TARJETA SIGUIENTE PUNTO (VERDE VIBRANTE) ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (nextCheckpoint != null) SuccessColor else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = if (nextCheckpoint == null) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (nextCheckpoint != null) Icons.Default.Navigation else Icons.Default.CheckCircle, 
                                    null, 
                                    tint = if (nextCheckpoint != null) Color.White else PrimaryColor
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (nextCheckpoint != null) "SIGUIENTE DESTINO" else "RONDA COMPLETADA", 
                                    color = if (nextCheckpoint != null) Color.White.copy(alpha = 0.8f) else TextSecondary, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                nextCheckpoint?.name ?: "¡Todos los puntos verificados!", 
                                color = if (nextCheckpoint != null) Color.White else TextPrimary, 
                                fontSize = 22.sp, 
                                fontWeight = FontWeight.Black
                            )
                            
                            if (nextCheckpoint != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // INSTRUCCIÓN DE MARCADO NFC
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Nfc, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Acerque el teléfono al Tag NFC para marcar", 
                                            color = Color.White, 
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { showSkipDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SuccessColor),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Omitir Punto", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = onReportIncident,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f), contentColor = Color.White),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                    ) {
                                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Reportar", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                items(state.checkpoints.sortedBy { it.executionOrder }) { checkpoint ->
                    CheckpointListItem(number = checkpoint.executionOrder, title = (checkpoint.name ?: "Punto").toTitleCase(), time = if (checkpoint.id in state.executedCheckpointIds) formatTime(state.scanTimes[checkpoint.id]) else "Esperando...", isCompleted = checkpoint.id in state.executedCheckpointIds, isActive = checkpoint.id == nextCheckpoint?.id)
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("OBSERVACIONES DE LA RONDA", fontWeight = FontWeight.ExtraBold, color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(value = observations, onValueChange = { observations = it }, placeholder = { Text("Novedades...", color = TextPlaceholder) }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp), textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                    Spacer(modifier = Modifier.height(20.dp))
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = PrimaryColor)
                    else Button(onClick = { viewModel.endRound(roundId, observations, onFinishRound) }, modifier = Modifier.fillMaxWidth().height(58.dp), enabled = isAllCheckpointsDone, colors = ButtonDefaults.buttonColors(containerColor = SuccessColor), shape = RoundedCornerShape(16.dp)) { Text("FINALIZAR MI RONDA", fontWeight = FontWeight.Black) }
                }
            }
        }
        Button(onClick = { showPanicDialog = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = DangerColor), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.Warning, null, tint = Color.White); Spacer(Modifier.width(12.dp)); Text("BOTÓN DE PÁNICO", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable private fun StatItem(value: String, label: String, color: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black); Text(label.uppercase(), color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun StatDivider() { Box(modifier = Modifier.width(1.dp).height(35.dp).background(Color.LightGray)) }
@Composable fun CheckpointListItem(number: Int?, title: String, time: String, isCompleted: Boolean, isActive: Boolean) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isCompleted) Color(0xFFF0FDF4) else Color.White), border = androidx.compose.foundation.BorderStroke(if(isActive) 2.dp else 1.dp, if (isCompleted) SuccessColor.copy(alpha = 0.5f) else if (isActive) PrimaryVariant else Color(0xFFE5E7EB))) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(36.dp).background(if (isCompleted) SuccessColor else if (isActive) PrimaryVariant else Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) { if (isCompleted) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp)) else Text(number.toString(), color = if (isActive) Color.White else TextSecondary, fontWeight = FontWeight.Black) }; Spacer(modifier = Modifier.width(16.dp)); Column { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccessTime, null, tint = if(isCompleted) SuccessColor else TextPlaceholder, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text(time, fontSize = 12.sp, color = if(isCompleted) SuccessColor else TextPlaceholder) } } } } }
fun formatTime(isoDate: String?): String { if (isoDate == null || isoDate == "S/H") return "--:--"; return try { val cleanDate = isoDate.substringBefore("."); val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(cleanDate); SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault()).format(date!!) } catch (e: Exception) { "--:--" } }
@Composable fun PanicAlertDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) { Dialog(onDismissRequest = onDismiss) { Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier.size(70.dp).background(DangerColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Report, null, tint = DangerColor, modifier = Modifier.size(40.dp)) }; Spacer(Modifier.height(20.dp)); Text("¿ACTIVAR PÁNICO?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DangerColor); Spacer(Modifier.height(8.dp)); Text("Se notificará a la central.", textAlign = TextAlign.Center, color = TextSecondary); Spacer(Modifier.height(28.dp)); Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DangerColor), shape = RoundedCornerShape(12.dp)) { Text("SÍ, ENVIAR ALERTA", fontWeight = FontWeight.Black) }; Spacer(Modifier.height(8.dp)); TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("NO, CANCELAR", color = TextSecondary) } } } } }
