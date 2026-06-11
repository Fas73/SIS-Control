package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.presentation.theme.*
import androidx.compose.ui.input.pointer.pointerInput
import com.siscontrol.mobile.MainActivity
import com.siscontrol.mobile.core.toTitleCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GuardiaRondaActivaScreen(
    paddingValues: PaddingValues,
    roundId: Long,
    installationName: String,
    viewModel: GuardRoundViewModel,
    onFinishRound: () -> Unit,
    onReportIncident: () -> Unit,
    onPanic: () -> Unit,
    onScanCheckpoint: (CheckpointDto, Int, Int) -> Unit = { _, _, _ -> }
) {
    var showPanicDialog by rememberSaveable { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf(false) }
    var showMandatoryPhotoDialog by remember { mutableStateOf<CheckpointDto?>(null) }
    var skipReason by remember { mutableStateOf("") }
    var observations by rememberSaveable { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // PERSISTENCIA DE URIS COMO STRING
    var skipImageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    var mandatoryImageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCameraUriStr by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUriStr != null) {
            val uri = android.net.Uri.parse(tempCameraUriStr)
            com.siscontrol.mobile.core.ImageUtils.applyWatermark(context, uri)
            if (showSkipDialog) skipImageUriStr = tempCameraUriStr
            else if (showMandatoryPhotoDialog != null) mandatoryImageUriStr = tempCameraUriStr
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
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    val nextCheckpoint = state.checkpoints
        .sortedBy { it.executionOrder }
        .firstOrNull { it.id !in state.executedCheckpointIds }

    val isAllCheckpointsDone = state.checkpoints.isEmpty() || 
                             state.executedCheckpointIds.size == state.checkpoints.size

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it) }
        state.error?.let { if (!it.contains("Etiqueta incorrecta")) snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(Unit) {
        val userId = viewModel.getUserIdSync()
        viewModel.startRemoteMonitoring(userId)
        com.siscontrol.mobile.di.AppModule.provideSyncManager().startSync()

        MainActivity.nfcTagFlow.collectLatest { tagId ->
            val currentNext = state.checkpoints.sortedBy { it.executionOrder }.firstOrNull { it.id !in state.executedCheckpointIds }
            if (currentNext != null && currentNext.nfcTagCode == tagId) {
                if (currentNext.requiresPhoto) showMandatoryPhotoDialog = currentNext
                else viewModel.scanNfcTag(context, roundId, tagId) { checkpoint ->
                    onScanCheckpoint(checkpoint, state.executedCheckpointIds.size, state.checkpoints.size)
                }
            } else if (currentNext != null) {
                snackbarHostState.showSnackbar("Etiqueta incorrecta. Debes escanear: ${currentNext.name}")
            }
        }
    }

    // Registrar Shake Listener para Pánico por Agitación
    DisposableEffect(Unit) {
        val emergencyManager = com.siscontrol.mobile.di.AppModule.getEmergencyManager()
        emergencyManager.startListening {
            viewModel.triggerPanicAlert(roundId)
            scope.launch { snackbarHostState.showSnackbar("¡Alerta de Pánico enviada a la central!") }
            onPanic()
        }
        android.util.Log.d("GUARD_RONDA", "Iniciando detección de agitación (Shake) en ronda")
        onDispose {
            emergencyManager.stopListening()
            android.util.Log.d("GUARD_RONDA", "Deteniendo detección de agitación (Shake) en ronda")
        }
    }

    // --- DIÁLOGOS ---
    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false; skipReason = ""; skipImageUriStr = null },
            title = { Text("Omitir Punto de Control", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Justifica por qué no puedes escanear este punto.", color = TextSecondary)
                    OutlinedTextField(value = skipReason, onValueChange = { skipReason = it }, placeholder = { Text("Ej: Tag dañado") }, modifier = Modifier.fillMaxWidth())
                    if (skipImageUriStr != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))) {
                            AsyncImage(model = Uri.parse(skipImageUriStr), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            IconButton(onClick = { skipImageUriStr = null }, modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        }
                    } else {
                        OutlinedButton(onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) CameraUtils.createTempImageUri(context)?.let { uri -> tempCameraUriStr = uri.toString(); cameraLauncher.launch(uri) }
                            else permissionLauncher.launch(Manifest.permission.CAMERA)
                        }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Capturar Evidencia") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val uri = skipImageUriStr?.let { Uri.parse(it) }
                    val finalReason = if (uri == null) "$skipReason (Sin Evidencia Fotográfica)" else skipReason
                    viewModel.skipCheckpoint(context, roundId, finalReason, uri) { showSkipDialog = false; skipReason = ""; skipImageUriStr = null }
                }, enabled = skipReason.isNotBlank() && !state.isLoading) { Text("CONFIRMAR") }
            },
            dismissButton = { TextButton(onClick = { showSkipDialog = false }) { Text("CANCELAR") } }
        )
    }

    // Pantalla Principal
    Box(modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(modifier = Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(PrimaryColor, PrimaryVariant))).statusBarsPadding().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Ronda en Progreso", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(installationName, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                    }
                    Surface(color = SuccessColor, shape = RoundedCornerShape(20.dp)) {
                        Text("ACTIVA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }
            }

            // Stats
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("${state.executedCheckpointIds.size}/${state.checkpoints.size}", "Puntos", PrimaryVariant)
                    StatDivider()
                    StatItem(viewModel.getMinutesPassed().toString(), "Minutos", WarningColor)
                }
            }

            // Lista con ajuste de teclado nativo (ELIMINA EL CUADRO BLANCO DEFINITIVAMENTE)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = lazyListState,
                contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (nextCheckpoint != null) SuccessColor else Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(if (nextCheckpoint != null) "SIGUIENTE DESTINO" else "RONDA COMPLETADA", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(nextCheckpoint?.name ?: "¡Buen trabajo!", color = if (nextCheckpoint != null) Color.White else TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            if (nextCheckpoint != null) {
                                Spacer(Modifier.height(4.dp))
                                Text("Acerca el dispositivo al punto NFC para registrar tu ubicación automáticamente.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { showSkipDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SuccessColor), modifier = Modifier.weight(1f)) { Text("Omitir", fontWeight = FontWeight.Bold) }
                                    Button(onClick = onReportIncident, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("Reportar", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }

                items(state.checkpoints.sortedBy { it.executionOrder }) { cp ->
                    CheckpointListItem(number = cp.executionOrder, title = (cp.name ?: "Punto").toTitleCase(), time = if (cp.id in state.executedCheckpointIds) formatTime(state.scanTimes[cp.id]) else "Pendiente", isCompleted = cp.id in state.executedCheckpointIds, isActive = cp.id == nextCheckpoint?.id)
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("OBSERVACIONES", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextSecondary)
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // BOTÓN DE VOZ (Speech-to-Text)
                                val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                                        val data = result.data
                                        val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                                        val spokenText = results?.get(0) ?: ""
                                        if (spokenText.isNotBlank()) {
                                            observations = if (observations.isBlank()) spokenText else "$observations. $spokenText"
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "es-CL")
                                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Hable ahora para dictar las observaciones...")
                                        }
                                        try {
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            // Manejar si no hay motor de voz
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).background(PrimaryVariant.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Mic, "Dictar", tint = PrimaryVariant, modifier = Modifier.size(18.dp))
                                }

                                // BOTÓN DE IA: Visible y Tangible
                                AssistChip(
                                    onClick = { 
                                        viewModel.generateProfessionalSummary(observations) { observations = it }
                                    },
                                    label = { Text("Redacción IA", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, "IA", modifier = Modifier.size(16.dp), tint = PrimaryVariant) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = PrimaryColor.copy(alpha = 0.05f),
                                        labelColor = PrimaryColor
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(borderColor = PrimaryColor.copy(alpha = 0.2f), enabled = true)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = observations, 
                            onValueChange = { observations = it }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .bringIntoViewRequester(bringIntoViewRequester)
                                .onFocusChanged { 
                                    if (it.isFocused) {
                                        scope.launch { 
                                            kotlinx.coroutines.delay(300) 
                                            bringIntoViewRequester.bringIntoView()
                                        } 
                                    }
                                },
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        if (state.isLoading) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
                        } else {
                            Button(onClick = { viewModel.endRound(roundId, observations, onFinishRound) }, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = isAllCheckpointsDone, colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)) { Text("FINALIZAR RONDA", fontWeight = FontWeight.Black) }
                        }
                        
                        // BOTON DE PANICO DESLIZABLE AL FINAL (NO TAPA NADA)
                        Spacer(Modifier.height(20.dp))
                        SwipeToPanicButton(onPanicTriggered = {
                            viewModel.triggerPanicAlert(roundId)
                            scope.launch { snackbarHostState.showSnackbar("¡Alerta de Pánico enviada a la central!") }
                            onPanic()
                        })
                    }
                }
            }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
    }

    // DIÁLOGO DE SESIÓN FINALIZADA ADMINISTRATIVAMENTE
    if (state.isTerminatedRemotely && !state.isTerminatedLocally && !state.isLoading) {
        Dialog(onDismissRequest = { onFinishRound() }) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Logout, null, tint = WarningColor, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("SESIÓN FINALIZADA", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(text = if (state.terminationReason?.contains("[CANCEL") == true) "Su sesión ha sido finalizada administrativamente por la jefatura." else (state.terminationReason ?: "La sesión ha terminado."), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                    Button(onClick = { onFinishRound() }, modifier = Modifier.fillMaxWidth()) { Text("ENTENDIDO") }
                }
            }
        }
    }

    // DIÁLOGO DE RONDA FINALIZADA LOCALMENTE (ÉXITO)
    if (state.isTerminatedLocally && !state.isLoading) {
        Dialog(onDismissRequest = { onFinishRound() }) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("¡BUEN TRABAJO!", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(text = "Ronda de Seguridad Cerrada Satisfactoriamente", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp), color = TextSecondary)
                    Button(onClick = { onFinishRound() }, colors = ButtonDefaults.buttonColors(containerColor = SuccessColor), modifier = Modifier.fillMaxWidth()) { Text("ENTENDIDO") }
                }
            }
        }
    }
}

@Composable
fun CheckpointListItem(number: Int?, title: String, time: String, isCompleted: Boolean, isActive: Boolean) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isCompleted) Color(0xFFF0FDF4) else Color.White), border = androidx.compose.foundation.BorderStroke(if(isActive) 2.dp else 1.dp, if (isCompleted) SuccessColor.copy(alpha = 0.5f) else if (isActive) PrimaryVariant else Color(0xFFE5E7EB))) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(36.dp).background(if (isCompleted) SuccessColor else if (isActive) PrimaryVariant else Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) { if (isCompleted) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp)) else Text(number.toString(), color = if (isActive) Color.White else TextSecondary, fontWeight = FontWeight.Black) }; Spacer(modifier = Modifier.width(16.dp)); Column { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccessTime, null, tint = if(isCompleted) SuccessColor else TextPlaceholder, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text(time, fontSize = 12.sp, color = if(isCompleted) SuccessColor else TextPlaceholder) } } } } }
fun formatTime(isoDate: String?): String { if (isoDate == null || isoDate == "S/H") return "--:--"; return try { val cleanDate = isoDate.substringBefore("."); val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(cleanDate); SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault()).format(date!!) } catch (e: Exception) { "--:--" } }

@Composable private fun StatItem(value: String, label: String, color: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black); Text(label.uppercase(), color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun StatDivider() { Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.LightGray)) }
