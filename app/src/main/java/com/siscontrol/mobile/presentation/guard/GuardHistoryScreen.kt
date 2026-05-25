package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.siscontrol.mobile.data.remote.dto.RoundHistoryItemDto
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardHistoryScreen(
    paddingValues: PaddingValues,
    viewModel: GuardHistoryViewModel,
    onNavigateToDetail: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Hoy", "Esta Semana", "Este Mes")
    val state by viewModel.state

    // Forzamos la recarga al entrar a la pantalla (Unit) y al cambiar el filtro
    LaunchedEffect(Unit, selectedFilter) {
        viewModel.loadHistory(selectedFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Historial de Rondas",
            subtitle = if (state.isLoading) "Cargando..." else "",
            showAdminLogo = false
        )

        // Filter Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters.size) { index ->
                    val filter = filters[index]
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, color = if (isSelected) TextPrimary else Color.White, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            containerColor = PrimaryVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (state.isLoading && state.history == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary row
                item {
                    val history = state.history
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        HistorySummaryCard(modifier = Modifier.weight(1f), value = history?.total?.toString() ?: "0", label = "Total", valueColor = TextPrimary)
                        HistorySummaryCard(modifier = Modifier.weight(1f), value = history?.completas?.toString() ?: "0", label = "Completas", valueColor = SuccessColor)
                        HistorySummaryCard(modifier = Modifier.weight(1f), value = history?.porcentajeExito ?: "0%", label = "Éxito", valueColor = TextPrimary)
                    }
                }

                // History list
                val rondas = state.history?.rondas ?: emptyList()
                if (rondas.isEmpty() && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No se encontraron rondas en este periodo.", color = TextSecondary)
                        }
                    }
                } else {
                    items(rondas.size) { index ->
                        val ronda = rondas[index]
                        // Si el servidor envía 0 o datos sospechosos, mostramos un guión o el valor real
                        val displayPoints = if (ronda.checkpointsTotal > 0) {
                            "${ronda.checkpointsExecuted}/${ronda.checkpointsTotal}"
                        } else {
                            "Consultando..."
                        }
                        
                        val shiftStart = formatTimeOnly(ronda.shiftStartTime)
                        val shiftEnd = if (ronda.shiftEndTime != null) {
                            if (ronda.shiftEndTime.contains(":")) formatTimeOnly(ronda.shiftEndTime) 
                            else ronda.shiftEndTime 
                        } else "En curso"
                        
                        val fullDateLabel = "${formatHistoryDate(ronda.startTime)} • ${formatTimeOnly(ronda.startTime)}"
                        
                        HistoryItemCard(
                            location = ronda.installationName,
                            status = ronda.statusDisplay ?: "Incompleta",
                            date = fullDateLabel,
                            duration = "${ronda.durationMinutes} min",
                            points = displayPoints,
                            shiftTime = "$shiftStart - $shiftEnd",
                            incidents = ronda.incidentCount,
                            isSuccess = ronda.statusDisplay?.equals("Completada", ignoreCase = true) == true,
                            onClick = { onNavigateToDetail(ronda.id) }
                        )
                    }
                }
            }
        }
    }
}

// Helpers for formatting
private fun formatHistoryDate(isoDate: String?): String {
    if (isoDate == null) return "N/A"
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("es", "ES"))
        val date = parser.parse(isoDate)
        if (date != null) formatter.format(date) else isoDate
    } catch (e: Exception) {
        isoDate.take(10)
    }
}

private fun formatTimeOnly(isoDate: String?): String {
    if (isoDate == null) return "--:--"
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val date = parser.parse(isoDate)
        if (date != null) formatter.format(date) else "--:--"
    } catch (e: Exception) {
        "--:--"
    }
}

@Composable
fun HistorySummaryCard(modifier: Modifier = Modifier, value: String, label: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = valueColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun HistoryItemCard(
    location: String, 
    status: String, 
    date: String, 
    duration: String, 
    points: String,
    shiftTime: String,
    incidents: Int,
    isSuccess: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.LocationOn, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(location, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                }
                SISBadge(status, containerColor = (if (isSuccess) SuccessColor else WarningColor).copy(alpha = 0.1f), contentColor = if (isSuccess) SuccessColor else WarningColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tiempos de Ronda
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Event, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(date, fontSize = 13.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(duration, fontSize = 13.sp, color = TextSecondary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))

            // Datos de la Jornada e Incidentes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Horario Jornada", fontSize = 11.sp, color = TextSecondary)
                    Text(shiftTime, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Marcajes", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val parts = points.split("/")
                        val scanned = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val total = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val isFullyScanned = scanned >= total && total > 0

                        Text(
                            text = points, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if(isFullyScanned) SuccessColor else if(scanned > 0) WarningColor else TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if(isFullyScanned) Icons.Default.VerifiedUser else Icons.Default.Pending,
                            contentDescription = null, 
                            tint = if(isFullyScanned) SuccessColor else Color.Gray, 
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    // Lógica visual para ignorar avisos informativos (como rondas completadas)
                    // Si el backend envía el número real de la BD, aquí mostramos lo crítico
                    val incidentText = when {
                        incidents <= 0 -> "Sin incidentes"
                        incidents == 1 -> "1 incidente crítico"
                        else -> "$incidents incidentes registrados"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (incidents > 0) {
                            Icon(Icons.Default.NotificationsActive, null, tint = DangerColor, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = incidentText,
                            fontSize = 11.sp, 
                            fontWeight = if (incidents > 0) FontWeight.Bold else FontWeight.Normal,
                            color = if(incidents > 0) DangerColor else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
