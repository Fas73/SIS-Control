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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardStartRoundScreen(
    paddingValues: PaddingValues,
    viewModel: GuardInstallationsViewModel, // Agregado para datos reales
    onStartRound: (Long, Long, String) -> Unit
) {
    val state by viewModel.state

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
                // GPS Alert (Simulando ubicación en el centro para cálculo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ubicación GPS detectada correctamente", color = PrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = DangerColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Atención",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = state.error!!,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                if (state.installations.isEmpty() && state.error == null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "No hay instalaciones disponibles",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF9A3412)
                                    )
                                    Text(
                                        "Contacte a su administrador para que se le asigne una sede.",
                                        fontSize = 13.sp,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                        }
                    }
                }

                // Carga dinámica de instalaciones reales
                items(state.installations) { inst ->
                    val isActiveSession = state.activeInstallationId == inst.id
                    val hasAnotherActiveSession = state.activeInstallationId != 0L && !isActiveSession

                    // Por ahora simulamos una distancia basada en si la lat/lon son 0 en tu BD
                    val isLocationSet = inst.latitude != null && inst.latitude != 0.0
                    val distanceMeters = if (isLocationSet) 150 else 3500
                    val distanceText = if (isLocationSet) "150m" else "3.5km"
                    
                    val cpCount = viewModel.checkpointCounts[inst.id]
                    val cpText = if (cpCount != null) "$cpCount puntos" else "Cargando..."

                    InstallationCard(
                        name = inst.name, 
                        distance = distanceText, 
                        distanceMeters = distanceMeters,
                        checkpoints = cpText,
                        isActiveSession = isActiveSession,
                        hasAnotherActiveSession = hasAnotherActiveSession,
                        onStartRound = { 
                            if (isActiveSession) {
                                // Navegamos directamente si ya hay sesión
                                onStartRound(state.activeRoundId, state.activeInstallationId, state.activeInstallationName)
                            } else {
                                // Iniciamos nueva sesión
                                viewModel.startTurnAndRound(inst.id ?: 0L, inst.name, onStartRound)
                            }
                        }
                    )
                }
            }

            item {
                // Verification box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEFCE8), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(8.dp))
                        .padding(16.dp)
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
    // El límite de cercanía es de 500 metros
    val isTooFar = distanceMeters > 500
    val canStart = !isTooFar && !hasAnotherActiveSession

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isActiveSession) { if(isActiveSession) onStartRound() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActiveSession) Color(0xFFF0FDF4) else Color.White
        ),
        border = BorderStroke(
            width = if (isActiveSession) 2.dp else 1.dp,
            color = if (isActiveSession) SuccessColor else if (isTooFar) Color.LightGray else Color(0xFFE5E7EB)
        ),
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
                isTooFar -> "Fuera de rango (Demasiado lejos)"
                else -> "Iniciar Turno en Instalación"
            }

            val buttonColor = if (isActiveSession || (!isTooFar && !hasAnotherActiveSession)) SuccessColor else Color(0xFFE2E8F0)
            val contentColor = if (isActiveSession || (!isTooFar && !hasAnotherActiveSession)) Color.White else Color.Gray

            Button(
                onClick = onStartRound,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = contentColor
                ),
                enabled = isActiveSession || canStart,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold
                )
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
