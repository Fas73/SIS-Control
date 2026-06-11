package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.BorderStroke
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase

@Composable
fun GuardHomeScreen(
    paddingValues: PaddingValues,
    onNavigate: (String) -> Unit,
    viewModel: GuardHomeViewModel,
    instViewModel: GuardInstallationsViewModel,
    token: String,
    role: String,
    userName: String = "Guardia"
) {
    var showCheckoutWarningDialog by remember { mutableStateOf(false) }
    var distanceToInst by remember { mutableIntStateOf(0) }
    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLon by remember { mutableDoubleStateOf(0.0) }
    
    val navState by viewModel.navState
    val instState by instViewModel.state
    val formattedName = userName.toTitleCase()
    
    val adminNotification by viewModel.adminNotification
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val estadoActual = (navState as? GuardNavigationState.Idle)?.estado
    val isShiftActive = estadoActual?.jornadaActiva == true
    
    val activeInstallation = estadoActual?.jornada?.installation?.clientName 
        ?: estadoActual?.jornada?.installation?.name 
        ?: "Sede no seleccionada"

    // Refrescar estado CADA VEZ que la pantalla sea visible (Silent para evitar pestañeo)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkStatusAndRedirect(token, role, silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Registrar Shake Listener GLOBAL durante la jornada
    DisposableEffect(isShiftActive) {
        val emergencyManager = com.siscontrol.mobile.di.AppModule.getEmergencyManager()
        if (isShiftActive) {
            emergencyManager.startListening {
                viewModel.triggerPanicAlert()
                scope.launch {
                    snackbarHostState.showSnackbar("¡Alerta de Pánico enviada a la central!")
                }
            }
            android.util.Log.d("GUARD_HOME", "Iniciando detección de agitación (Shake)")
        } else {
            emergencyManager.stopListening()
            android.util.Log.d("GUARD_HOME", "Deteniendo detección de agitación (Shake)")
        }
        onDispose {
            emergencyManager.stopListening()
            android.util.Log.d("GUARD_HOME", "Limpiando detección de agitación")
        }
    }

    LaunchedEffect(navState) {
        if (navState is GuardNavigationState.Redirect) {
            val route = (navState as GuardNavigationState.Redirect).route
            onNavigate(route)
            viewModel.clearRedirect() // Evita bucles de redirección
        }
    }

    if (navState is GuardNavigationState.Loading || instState.isActionLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(PrimaryColor, PrimaryVariant)
                        )
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val letter = formattedName.take(1).uppercase()
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SuccessColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(letter, color = SuccessColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Hola, $formattedName", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Perfil: Guardia", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                }
            }

            // Status Banner (Active/Inactive)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isShiftActive) SuccessColor else DangerColor)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isShiftActive) "ESTADO: EN TURNO" else "ESTADO: FUERA DE TURNO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Instalación actual
                if (isShiftActive) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryColor)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Instalación Asignada", fontSize = 12.sp, color = Color.Gray)
                                    Text(activeInstallation, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }

                // Tarjeta Principal (Iniciar Turno / Comenzar Ronda)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6).copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (isShiftActive) SuccessColor.copy(alpha = 0.1f) else PrimaryColor.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if(isShiftActive) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isShiftActive) SuccessColor else PrimaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if(isShiftActive) "Turno Activo" else "Iniciar Jornada", 
                                color = TextPrimary, 
                                fontSize = 22.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isShiftActive) "Puedes comenzar tu ronda ahora" 
                                       else "Registra tu asistencia para empezar", 
                                color = TextSecondary, 
                                fontSize = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // BOTÓN 1: RONDA (Dinámico)
                            Button(
                                onClick = { 
                                    if (!isShiftActive) {
                                        val route = com.siscontrol.mobile.presentation.Destinos.guardStartRoundRoute(token, role)
                                        onNavigate(route)
                                    } else {
                                        val instId = estadoActual?.jornada?.installation?.id ?: 0L
                                        instViewModel.startNewRound(context, instId) { rId, iId, iName ->
                                            val route = com.siscontrol.mobile.presentation.Destinos.guardRondaRoute(token, role, rId, iId, iName)
                                            onNavigate(route)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isShiftActive) SuccessColor else PrimaryColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if(isShiftActive) "COMENZAR RONDA NFC" else "MARCAR ENTRADA", 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // BOTÓN 2: FINALIZAR JORNADA (Solo visible en turno)
                            if (isShiftActive) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { 
                                        viewModel.notifyLocalAction()
                                        scope.launch {
                                            val loc = com.siscontrol.mobile.core.LocationUtils.getCurrentLocation(context)
                                            val activeInst = instState.installations.find { it.id == instState.activeInstallationId }
                                            var distance = Int.MAX_VALUE
                                            val instLat = activeInst?.latitude
                                            val instLon = activeInst?.longitude
                                            if (loc != null && instLat != null && instLon != null && instLat != 0.0) {
                                                val results = FloatArray(1)
                                                android.location.Location.distanceBetween(loc.latitude, loc.longitude, instLat, instLon, results)
                                                distance = results[0].toInt()
                                            }
                                            
                                            currentLat = loc?.latitude ?: 0.0
                                            currentLon = loc?.longitude ?: 0.0
                                            distanceToInst = distance

                                            if (distance > 500) {
                                                showCheckoutWarningDialog = true
                                            } else {
                                                instViewModel.endShift(currentLat, currentLon) {
                                                    viewModel.checkStatusAndRedirect(token, role, isLocal = true)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("FINALIZAR MI JORNADA", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                if (instState.error != null) {
                    item {
                        Text(instState.error!!, color = DangerColor, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
        
        // REEMPLAZO: BOTÓN DE PÁNICO DESLIZABLE
        if (isShiftActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 60.dp) // Separación para BottomBar
            ) {
                SwipeToPanicButton(
                    onPanicTriggered = {
                        viewModel.triggerPanicAlert()
                        scope.launch {
                            snackbarHostState.showSnackbar("¡Alerta de Pánico enviada a la central!")
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState, 
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
        )
    }

    // DIÁLOGO DE NOTIFICACIÓN ADMINISTRATIVA EN TIEMPO REAL (DISEÑO HERMOSO)
    if (adminNotification != null) {
        Dialog(onDismissRequest = { viewModel.clearNotification() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(WarningColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, null, tint = WarningColor, modifier = Modifier.size(36.dp))
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        "AVISO DEL SISTEMA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = adminNotification!!,
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                        lineHeight = 20.sp,
                        fontSize = 14.sp
                    )
                    
                    Spacer(Modifier.height(28.dp))
                    
                    Button(
                        onClick = { viewModel.clearNotification() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("ENTENDIDO", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    if (showCheckoutWarningDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutWarningDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cierre Irregular de Jornada", fontWeight = FontWeight.Bold, color = DangerColor)
                }
            },
            text = {
                val distanceText = if (distanceToInst == Int.MAX_VALUE) "una distancia desconocida" else if (distanceToInst > 1000) "${distanceToInst / 1000.0} km" else "$distanceToInst metros"
                Text(
                    text = "Detectamos que te encuentras a $distanceText de la instalación activa.\n\nTu cierre de jornada quedará registrado en el sistema con estas coordenadas.\n\n¿Estás seguro de que deseas finalizar tu jornada desde aquí?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutWarningDialog = false
                        instViewModel.endShift(currentLat, currentLon) {
                            viewModel.checkStatusAndRedirect(token, role, isLocal = true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                ) {
                    Text("Sí, finalizar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutWarningDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SwipeToPanicButton(onPanicTriggered: () -> Unit) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var triggered by remember { mutableStateOf(false) }
    // Asumimos un ancho general aproximado
    val maxSwipe = LocalContext.current.resources.displayMetrics.density * 220f
    val scope = rememberCoroutineScope()

    val animatedOffset by androidx.compose.animation.core.animateFloatAsState(targetValue = swipeOffset)
    val animatedColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (triggered) Color(0xFF7F1D1D) else DangerColor
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = animatedColor,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background text
            Text(
                text = if (triggered) "¡ALERTA ENVIADA!" else "DESLIZA PARA PÁNICO ➔",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center).padding(start = 24.dp)
            )

            // The draggable thumb
            Box(
                modifier = Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(animatedOffset.toInt(), 0) }
                    .padding(4.dp)
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .pointerInput(triggered) {
                        if (!triggered) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (swipeOffset < maxSwipe * 0.7f) {
                                        swipeOffset = 0f
                                    } else {
                                        swipeOffset = maxSwipe
                                        triggered = true
                                        onPanicTriggered()
                                        // Reset after 3 seconds
                                        scope.launch {
                                            delay(3000)
                                            triggered = false
                                            swipeOffset = 0f
                                        }
                                    }
                                }
                            ) { change, dragAmount ->
                                // no consume needed for local component
                                swipeOffset = (swipeOffset + dragAmount).coerceIn(0f, maxSwipe)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = animatedColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
