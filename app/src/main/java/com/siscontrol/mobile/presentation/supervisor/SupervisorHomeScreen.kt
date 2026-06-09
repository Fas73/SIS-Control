package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siscontrol.mobile.presentation.admin.*
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.components.SeverityPieChart
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase
import com.siscontrol.mobile.di.AppModule

private class SupervisorHomeViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminHomeViewModel() as T
}

@Composable
fun SupervisorHomeScreen(
    paddingValues: PaddingValues,
    userName: String,
    token: String,
    role: String,
    onNavigate: (String) -> Unit
) {
    val viewModel: AdminHomeViewModel = viewModel(factory = SupervisorHomeViewModelFactory())
    val state by viewModel.state
    val formattedName = userName.toTitleCase()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showCancelRoundDialog by remember { mutableStateOf<DashboardActiveRound?>(null) }
    var showCancelShiftDialog by remember { mutableStateOf<DashboardActiveShift?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Panel Supervisor",
            subtitle = "Bienvenido, $formattedName",
            showAdminLogo = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
            } else if (state.error != null) {
                item {
                    Text(state.error!!, color = DangerColor, modifier = Modifier.padding(16.dp))
                }
            } else {
                // KPIs Primera Fila
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Tarjeta 1: Mis Guardias
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = PrimaryVariant, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardias", fontSize = 13.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(state.totalGuards.toString(), fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (state.activeShifts > 0) SuccessColor else Color.Gray, CircleShape)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("${state.activeShifts} en jornada", fontSize = 11.sp, color = if (state.activeShifts > 0) SuccessColor else TextSecondary)
                                }
                            }
                        }
                        
                        // Tarjeta 2: Rondas Hoy
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timeline, contentDescription = null, tint = PrimaryVariant, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rondas Hoy", fontSize = 13.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(state.totalRoundsToday.toString(), fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Column {
                                    Text("${state.roundsInProgress} en progreso", fontSize = 11.sp, color = SuccessColor)
                                    Text("${state.completedRoundsToday} finalizadas", fontSize = 11.sp, color = DangerColor)
                                }
                            }
                        }
                    }
                }

                // KPIs Segunda Fila
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            title = "Instalaciones",
                            value = state.activeInstallations.toString(),
                            subtitle = "Activas actualmente",
                            subtitleColor = TextSecondary,
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

                    if (state.totalIncidents > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Distribución por Gravedad", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                SeverityPieChart(
                                    high = state.highSeverityCount,
                                    medium = state.mediumSeverityCount,
                                    low = state.lowSeverityCount
                                )
                            }
                        }
                    }
                }
            }

            // Quick Access
            item {
                Text("Accesos Rápidos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Mapa en Vivo",
                        icon = Icons.Default.LocationOn,
                        containerColor = PrimaryColor.copy(alpha = 0.05f),
                        contentColor = PrimaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("MAP") }
                    )
                    QuickAccessButton(
                        title = "Mis Guardias",
                        icon = Icons.Default.Shield,
                        containerColor = SuccessColor.copy(alpha = 0.05f),
                        contentColor = SuccessColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("USERS") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Alertas",
                        icon = Icons.Default.Warning,
                        containerColor = DangerColor.copy(alpha = 0.05f),
                        contentColor = DangerColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("ALERTS") }
                    )
                    QuickAccessButton(
                        title = "Bitácora",
                        icon = Icons.Default.History,
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.05f),
                        contentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(com.siscontrol.mobile.presentation.Destinos.adminIncidentLogRoute(token, role)) }
                    )
                }
            }

            // Active Rounds
            item {
                Text("Rondas en Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (state.activeRounds.isEmpty()) {
                    Text("No hay rondas en curso actualmente.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            items(state.activeRounds) { round ->
                ActiveRoundCard(
                    guardName = round.guardName,
                    location = round.location,
                    progress = round.progress,
                    progressText = round.progressText,
                    status = round.status,
                    onCancel = { showCancelRoundDialog = round }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Active Shifts
            item {
                Text("Jornadas en Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                        val file = com.siscontrol.mobile.core.PdfManager.generateShiftReport(
                            context = context,
                            workerName = shift.guardName,
                            entry = shift.entryTime,
                            exit = null,
                            location = shift.location
                        )
                        if (file != null) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", file
                            )
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Ver Jornada"))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // --- DIÁLOGOS DE CANCELACIÓN (Supervisores también pueden cancelar según RoundService.java) ---
    
    if (showCancelRoundDialog != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelRoundDialog = null 
                cancelReason = ""
            },
            title = { Text("Cancelar Ronda") },
            text = { 
                Column {
                    Text("¿Deseas finalizar administrativamente la ronda de ${showCancelRoundDialog?.guardName}?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo de la cancelación", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Ej: Guardia abandonó puesto", color = Color.Gray) },
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
                        val motivo = if (cancelReason.isNotBlank()) cancelReason else "Finalizada por Supervisor"
                        viewModel.cancelRound(showCancelRoundDialog!!.id, motivo)
                        showCancelRoundDialog = null
                        cancelReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelRoundDialog = null 
                    cancelReason = ""
                }) { Text("Cancelar") }
            }
        )
    }

    if (showCancelShiftDialog != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelShiftDialog = null 
                cancelReason = ""
            },
            title = { Text("Cerrar Jornada") },
            text = { 
                Column {
                    Text("¿Deseas cerrar forzadamente la asistencia de ${showCancelShiftDialog?.guardName}?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo del cierre", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Ej: Término de turno manual", color = Color.Gray) },
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
                        val motivo = if (cancelReason.isNotBlank()) cancelReason else "Cierre forzado por Supervisor"
                        viewModel.cancelShift(showCancelShiftDialog!!.id, motivo)
                        showCancelShiftDialog = null
                        cancelReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                ) { Text("Confirmar") }
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
