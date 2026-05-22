package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase

import com.siscontrol.mobile.core.formatDateToDisplay

@OptIn(ExperimentalMaterial3Api::class)
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
    
    var showCancelRoundDialog by remember { mutableStateOf<DashboardActiveRound?>(null) }
    var showCancelShiftDialog by remember { mutableStateOf<DashboardActiveShift?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Dashboard ADMIN",
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
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Tarjeta 1: Guardias (Totales y En Jornada)
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
                        
                        // Tarjeta 2: Rondas Hoy con doble subtexto
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
                    Spacer(modifier = Modifier.height(16.dp))
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
                }
            }

            item {
                Text(
                    "Accesos Rápidos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
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
                        title = "Alertas",
                        icon = Icons.Default.Warning,
                        containerColor = DangerColor.copy(alpha = 0.05f),
                        contentColor = DangerColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("ALERTS") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Gestión",
                        icon = Icons.Default.Settings,
                        containerColor = SuccessColor.copy(alpha = 0.05f),
                        contentColor = SuccessColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("MANAGEMENT") }
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

            item {
                Text(
                    "Rondas en Curso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (state.activeRounds.isEmpty()) {
                    Text(
                        "No hay rondas en curso actualmente.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
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

            item {
                Text(
                    "Jornadas en Curso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                )
                if (state.activeShiftsList.isEmpty()) {
                    Text(
                        "No hay guardias en jornada actualmente.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(state.activeShiftsList) { shift ->
                ActiveShiftCard(
                    guardName = shift.guardName,
                    location = shift.location,
                    entryTime = shift.entryTime,
                    onCancel = { showCancelShiftDialog = shift }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // --- DIÁLOGOS DE CANCELACIÓN ---
    
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
                        val motivo = if (cancelReason.isNotBlank()) cancelReason else "Finalizada por Administrador"
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
                        val motivo = if (cancelReason.isNotBlank()) cancelReason else "Cierre forzado por Administrador"
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

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color = TextSecondary,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = subtitleColor)
        }
    }
}

@Composable
fun ActiveRoundCard(
    guardName: String,
    location: String,
    progress: Float,
    progressText: String,
    status: String,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(guardName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SISBadge(status, containerColor = PrimaryColor.copy(alpha = 0.1f), contentColor = PrimaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancelar Ronda", tint = DangerColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = PrimaryColor,
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(progressText, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun ActiveShiftCard(
    guardName: String,
    location: String,
    entryTime: String,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(guardName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Jornada", tint = DangerColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Entrada: ${entryTime.formatDateToDisplay()}", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun QuickAccessButton(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
