package com.siscontrol.mobile.presentation.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.components.SeverityPieChart
import com.siscontrol.mobile.presentation.components.ShimmerCardItem
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase
import com.siscontrol.mobile.core.formatDateToDisplay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AdminHomeScreen(
    paddingValues: PaddingValues,
    onNavigate: (String) -> Unit,
    viewModel: AdminHomeViewModel,
    token: String,
    role: String,
    userName: String = "Usuario"
) {
    val formattedName = userName.toTitleCase()
    val state by viewModel.state
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    
    var showCancelRoundDialog by remember { mutableStateOf<DashboardActiveRound?>(null) }
    var showCancelShiftDialog by remember { mutableStateOf<DashboardActiveShift?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    
    // Pull to Refresh State
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.loadDashboardData()
                delay(800) // Simular un pequeño tiempo extra para que la animación se aprecie
                isRefreshing = false
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Dashboard ADMIN",
            subtitle = "Bienvenido, $formattedName",
            showAdminLogo = true
        )

        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Mostrar Cargando
                if (state.isLoading && !isRefreshing) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ShimmerCardItem()
                            ShimmerCardItem()
                            ShimmerCardItem()
                        }
                    }
                }
                
                // Mostrar Error sin romper el Dashboard
                if (state.error != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = DangerColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, null, tint = DangerColor)
                                Spacer(Modifier.width(12.dp))
                                Text(state.error!!, color = DangerColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.resetMessages() }) {
                                    Icon(Icons.Default.Close, null, tint = DangerColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // KPIs y Dashboard
                item {
                    Column {
                        AnimatedVisibility(
                            visible = !state.isLoading || isRefreshing,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Guardias", fontSize = 13.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(state.totalGuards.toString(), fontSize = 24.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(if (state.activeShifts > 0) SuccessColor else Color.Gray, CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text("${state.activeShifts} en jornada", fontSize = 11.sp, color = if (state.activeShifts > 0) SuccessColor else TextSecondary)
                                    }
                                }
                            }
                            
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timeline, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Rondas Hoy", fontSize = 13.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(state.totalRoundsToday.toString(), fontSize = 24.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column {
                                        Text("${state.roundsInProgress} en progreso", fontSize = 11.sp, color = SuccessColor)
                                        Text("${state.completedRoundsToday} finalizadas", fontSize = 11.sp, color = DangerColor)
                                    }
                                }
                            }
                        }
                    }
                    }
                }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Instalaciones",
                        value = state.activeInstallations.toString(),
                        subtitle = "Activas actualmente",
                        icon = Icons.Default.LocationOn,
                        iconColor = PrimaryVariant,
                        iconBg = Color.Transparent
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Alertas",
                        value = state.totalIncidents.toString(),
                        subtitle = "${state.pendingIncidents} sin atender",
                        subtitleColor = DangerColor,
                        icon = Icons.Default.Warning,
                        iconColor = DangerColor,
                        iconBg = Color.Transparent
                    )
                }
            }

            item {
                Text("Accesos Rápidos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(title = "Mapa en Vivo", icon = Icons.Default.LocationOn, containerColor = PrimaryColor.copy(alpha = 0.05f), contentColor = PrimaryColor, modifier = Modifier.weight(1f), onClick = { onNavigate("MAP") })
                    QuickAccessButton(title = "Alertas", icon = Icons.Default.Warning, containerColor = DangerColor.copy(alpha = 0.05f), contentColor = DangerColor, modifier = Modifier.weight(1f), onClick = { onNavigate("ALERTS") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(title = "Gestión", icon = Icons.Default.Settings, containerColor = SuccessColor.copy(alpha = 0.05f), contentColor = SuccessColor, modifier = Modifier.weight(1f), onClick = { onNavigate("MANAGEMENT") })
                    QuickAccessButton(title = "Bitácora", icon = Icons.Default.History, containerColor = Color(0xFF8B5CF6).copy(alpha = 0.05f), contentColor = Color(0xFF8B5CF6), modifier = Modifier.weight(1f), onClick = { onNavigate(com.siscontrol.mobile.presentation.Destinos.adminIncidentLogRoute(token, role)) })
                }
            }

            item {
                Text("Rondas en Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))
                if (state.activeRounds.isEmpty()) {
                    Text("No hay rondas en curso actualmente.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            items(state.activeRounds) { round ->
                ActiveRoundCard(guardName = round.guardName, location = round.location, progress = round.progress, progressText = round.progressText, status = round.status, onCancel = { showCancelRoundDialog = round })
            }

            item {
                Text("Jornadas en Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp, top = 8.dp))
                if (state.activeShiftsList.isEmpty()) {
                    Text("No hay guardias en jornada actualmente.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            items(state.activeShiftsList) { shift ->
                ActiveShiftCard(
                    guardName = shift.guardName,
                    location = shift.location,
                    entryTime = shift.entryTime,
                    onCancel = { showCancelShiftDialog = shift },
                    onDownloadPdf = {
                        isGeneratingPdf = true
                        viewModel.getShiftReport(shift.id) { report ->
                            if (report != null) {
                                scope.launch {
                                    try {
                                        val file = com.siscontrol.mobile.core.PdfManager.generateConsolidatedShiftReport(context, report)
                                        if (file != null) {
                                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.siscontrol.mobile.fileprovider", file)
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/pdf")
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(intent, "Abrir Reporte"))
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("PDF_ERROR", "Crash al abrir PDF: ${e.message}")
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            } else {
                                isGeneratingPdf = false
                            }
                        }
                    }
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = PrimaryColor
        )
        } // Cierra el Box de PullToRefresh

        // OVERLAY DE CARGA PDF
        if (isGeneratingPdf) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryColor)
                        Spacer(Modifier.height(16.dp))
                        Text("Generando Reporte Maestro...", fontWeight = FontWeight.Bold)
                        Text("Esto puede tardar unos segundos si hay fotos.", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showCancelRoundDialog != null) {
        AlertDialog(
            onDismissRequest = { showCancelRoundDialog = null; cancelReason = "" },
            title = { Text("Cancelar Ronda") },
            text = {
                Column {
                    Text("¿Deseas finalizar la ronda de ${showCancelRoundDialog?.guardName}?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = cancelReason, onValueChange = { cancelReason = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { Button(onClick = { viewModel.cancelRound(showCancelRoundDialog!!.id, cancelReason); showCancelRoundDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = DangerColor)) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { showCancelRoundDialog = null }) { Text("Cancelar") } }
        )
    }

    if (showCancelShiftDialog != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelShiftDialog = null 
                cancelReason = ""
            },
            title = { Text("Cerrar Jornada Administrativamente") },
            text = { 
                Column {
                    Text("¿Deseas finalizar la asistencia de ${showCancelShiftDialog?.guardName}?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo del cierre", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Ej: Retiro anticipado autorizado", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = { 
                Button(
                    onClick = { 
                        val motivo = if (cancelReason.isNotBlank()) cancelReason else "Cierre Administrativo"
                        viewModel.cancelShift(showCancelShiftDialog!!.id, motivo)
                        showCancelShiftDialog = null
                        cancelReason = ""
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                    enabled = cancelReason.isNotBlank()
                ) { Text("Confirmar Cierre") } 
            },
            dismissButton = { 
                TextButton(onClick = { 
                    showCancelShiftDialog = null 
                    cancelReason = ""
                }) { Text("Cancelar") } 
            }
        )
    }
}

@Composable
fun KpiCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, subtitleColor: Color = TextSecondary, icon: ImageVector, iconColor: Color, iconBg: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = subtitleColor)
        }
    }
}

@Composable
fun ActiveRoundCard(guardName: String, location: String, progress: Float, progressText: String, status: String, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(guardName, fontWeight = FontWeight.Bold)
                    Text(location, fontSize = 12.sp, color = TextSecondary)
                }
                IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = DangerColor) }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = PrimaryColor)
            Text(progressText, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun ActiveShiftCard(guardName: String, location: String, entryTime: String, onCancel: () -> Unit, onDownloadPdf: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(guardName, fontWeight = FontWeight.Bold)
                Text(location, fontSize = 12.sp, color = TextSecondary)
                Text("Entrada: ${entryTime.formatDateToDisplay()}", fontSize = 11.sp, color = TextSecondary)
            }
            IconButton(onClick = onDownloadPdf) { Icon(Icons.Default.PictureAsPdf, null, tint = PrimaryColor) }
            IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = DangerColor) }
        }
    }
}

@Composable
fun QuickAccessButton(title: String, icon: ImageVector, containerColor: Color, contentColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.height(80.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = containerColor), onClick = onClick) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = contentColor)
            Text(title, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
