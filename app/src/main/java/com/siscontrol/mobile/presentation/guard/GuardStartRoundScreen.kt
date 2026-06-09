package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardStartRoundScreen(
    paddingValues: PaddingValues,
    viewModel: GuardInstallationsViewModel, 
    onBack: () -> Unit,
    onStartRound: (Long, Long, String) -> Unit
) {
    val state by viewModel.state
    val context = LocalContext.current
    val deviceLoc = viewModel.deviceLocation

    LaunchedEffect(Unit) {
        viewModel.updateLocation(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Iniciar Turno",
            subtitle = "Selecciona la instalación",
            showAdminLogo = false,
            actions = {
                IconButton(onClick = { viewModel.loadInstallations() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (deviceLoc != null) SuccessColor else DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (deviceLoc != null) "Ubicación GPS detectada correctamente" else "Detectando ubicación GPS...",
                            color = PrimaryColor, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Text("Instalaciones Disponibles", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(top = 8.dp))
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
            } else {
                if (state.isActionLoading) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (state.error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Atención", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF991B1B))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(text = state.error!!, color = Color(0xFFB91C1C), fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    }
                }

                val activeInstallations = state.installations.filter { (it.status ?: 1) == 1 }
                
                items(activeInstallations) { inst ->
                    val installationId = inst.id ?: 0L
                    val installationName = inst.clientName ?: inst.name ?: "Sede"
                    
                    val isActiveSession = state.activeInstallationId == installationId
                    val hasAnotherActiveSession = state.activeInstallationId != 0L && !isActiveSession

                    // CALCULO DE DISTANCIA (Si no hay GPS, asumimos que estás cerca para no bloquearte)
                    var distanceMeters = 0 
                    var distanceText = "Calculando..."
                    
                    if (deviceLoc != null && inst.latitude != null && inst.longitude != null && inst.latitude != 0.0) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(deviceLoc.latitude, deviceLoc.longitude, inst.latitude, inst.longitude, results)
                        distanceMeters = results[0].toInt()
                        distanceText = if (distanceMeters < 1000) "${distanceMeters}m" else String.format(java.util.Locale.getDefault(), "%.1fkm", distanceMeters / 1000.0)
                    } else if (inst.latitude != null && inst.latitude != 0.0) {
                        distanceText = "Cerca (GPS detectando...)"
                    }
                    
                    val cpCount = viewModel.checkpointCounts[installationId]
                    val cpText = if (cpCount != null) "$cpCount puntos" else "Cargando..."

                    InstallationCard(
                        name = installationName, 
                        distance = distanceText, 
                        distanceMeters = distanceMeters,
                        checkpoints = cpText,
                        isActiveSession = isActiveSession,
                        hasAnotherActiveSession = hasAnotherActiveSession,
                        onStartRound = { 
                            if (isActiveSession) {
                                if (state.activeRoundId != 0L) {
                                    onStartRound(state.activeRoundId, installationId, installationName)
                                } else {
                                    onBack()
                                }
                            } else {
                                viewModel.startShiftOnly(context, installationId, installationName) {
                                    onBack()
                                }
                            }
                        }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFEFCE8), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(8.dp)).padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Requisitos previos obligatorios:", color = Color(0xFF92400E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            VerificationItem("GPS activado y dentro del perímetro")
                            VerificationItem("NFC habilitado para escaneo")
                            VerificationItem("Conexión a internet activa")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallationCard(
    name: String, 
    distance: String, 
    distanceMeters: Int,
    checkpoints: String,
    isActiveSession: Boolean = false,
    hasAnotherActiveSession: Boolean = false,
    onStartRound: () -> Unit
) {
    // Si la distancia es 0 (no hay GPS aún) o menor a 500m, dejamos entrar.
    val canStart = (distanceMeters == 0 || distanceMeters <= 500) && !hasAnotherActiveSession
    val isTooFar = distanceMeters > 500

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isActiveSession || canStart) { onStartRound() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActiveSession) Color(0xFFF0FDF4) else Color.White),
        border = BorderStroke(width = if (isActiveSession) 2.dp else 1.dp, color = if (isActiveSession) SuccessColor else if (isTooFar) Color.LightGray else Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActiveSession) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(if (isActiveSession) SuccessColor.copy(alpha=0.1f) else PrimaryColor.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isActiveSession) SuccessColor else PrimaryColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, color = if (isTooFar && !isActiveSession) Color.Gray else TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (isActiveSession) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(color = SuccessColor, shape = RoundedCornerShape(4.dp)) {
                                    Text("EN CURSO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isTooFar && !isActiveSession) Color.Gray else DangerColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(distance, color = if (isTooFar && !isActiveSession) Color.Gray else TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = if (isTooFar && !isActiveSession) Color.Gray else TextSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(checkpoints, color = if (isTooFar && !isActiveSession) Color.Gray else TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = if (isTooFar && !isActiveSession) Color.LightGray else TextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val buttonText = when {
                isActiveSession -> "Continuar Jornada / Ronda"
                hasAnotherActiveSession -> "Tienes otra jornada activa"
                isTooFar -> "Fuera de rango (${distance})"
                else -> "Iniciar Turno en Instalación"
            }

            val buttonColor = if (isActiveSession || (!isTooFar && !hasAnotherActiveSession)) SuccessColor else Color(0xFFE2E8F0)
            val contentColor = if (isActiveSession || (!isTooFar && !hasAnotherActiveSession)) Color.White else Color.Gray

            Button(
                onClick = onStartRound,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = contentColor),
                enabled = isActiveSession || canStart,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = buttonText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VerificationItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(4.dp).background(Color(0xFF92400E), CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color(0xFF92400E), fontSize = 13.sp)
    }
}
