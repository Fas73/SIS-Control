package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardHistoryScreen(
    paddingValues: PaddingValues
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Hoy", "Esta Semana", "Este Mes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Historial de Rondas",
            subtitle = "",
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary row
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HistorySummaryCard(modifier = Modifier.weight(1f), value = "24", label = "Total", valueColor = TextPrimary)
                    HistorySummaryCard(modifier = Modifier.weight(1f), value = "23", label = "Completas", valueColor = SuccessColor)
                    HistorySummaryCard(modifier = Modifier.weight(1f), value = "96%", label = "Éxito", valueColor = TextPrimary)
                }
            }

            // History list
            item {
                HistoryItemCard(
                    location = "Plaza Centro",
                    status = "Completada",
                    date = "26 Abril 2026",
                    duration = "28 min",
                    timeWindow = "14:30 - 14:58",
                    points = "8/8"
                )
            }
            item {
                HistoryItemCard(
                    location = "Plaza Centro",
                    status = "Completada",
                    date = "26 Abril 2026",
                    duration = "25 min",
                    timeWindow = "10:00 - 10:25",
                    points = "8/8"
                )
            }
            item {
                HistoryItemCard(
                    location = "Bodega Norte",
                    status = "Completada",
                    date = "25 Abril 2026",
                    duration = "20 min",
                    timeWindow = "18:15 - 18:35",
                    points = "6/6"
                )
            }
            item {
                HistoryItemCard(
                    location = "Plaza Centro",
                    status = "Completada",
                    date = "25 Abril 2026",
                    duration = "32 min",
                    timeWindow = "14:00 - 14:32",
                    points = "8/8"
                )
            }
            item {
                HistoryItemCard(
                    location = "Edificio Sur",
                    status = "Completada",
                    date = "24 Abril 2026",
                    duration = "45 min",
                    timeWindow = "09:00 - 09:45",
                    points = "10/10"
                )
            }
        }
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
fun HistoryItemCard(location: String, status: String, date: String, duration: String, timeWindow: String, points: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(location, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                SISBadge(status, containerColor = SuccessColor.copy(alpha = 0.1f), contentColor = SuccessColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(date, fontSize = 13.sp, color = TextSecondary)
                }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(duration, fontSize = 13.sp, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeWindow, fontSize = 13.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(points, fontSize = 13.sp, color = SuccessColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
