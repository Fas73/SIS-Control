package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlertsScreen(
    paddingValues: PaddingValues
) {
    var selectedFilter by remember { mutableStateOf("Todas (3)") }
    val filters = listOf("Todas (3)", "Pánico (1)", "Advertencia (1)", "Info (1)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Centro de Alertas",
            subtitle = "",
            showAdminLogo = true
        )

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
                        label = { Text(filter, color = if (isSelected) PrimaryColor else Color.White, fontWeight = FontWeight.Medium) },
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
            item {
                AlertCard(
                    title = "BOTÓN DE PÁNICO ACTIVADO",
                    user = "Juan Pérez",
                    description = "Solicitud de ayuda inmediata",
                    location = "Plaza Centro - Checkpoint 4",
                    time = "Hace 2 minutos",
                    icon = Icons.Default.Warning,
                    tintColor = DangerColor,
                    backgroundColor = DangerColor.copy(alpha = 0.05f)
                )
            }
            item {
                AlertCard(
                    title = "Checkpoint no escaneado",
                    user = "María González",
                    description = "Ronda incompleta detectada",
                    location = "Bodega Norte",
                    time = "Hace 15 minutos",
                    icon = Icons.Default.Notifications,
                    tintColor = Color(0xFFD97706), // Amber/Warning
                    backgroundColor = Color(0xFFFEF3C7)
                )
            }
            item {
                AlertCard(
                    title = "Ronda completada",
                    user = "Pedro Sánchez",
                    description = "Todos los checkpoints verificados",
                    location = "Edificio Sur",
                    time = "Hace 30 minutos",
                    icon = Icons.Default.Info,
                    tintColor = PrimaryColor,
                    backgroundColor = PrimaryColor.copy(alpha = 0.05f)
                )
            }
        }
    }
}

@Composable
fun AlertCard(
    title: String,
    user: String,
    description: String,
    location: String,
    time: String,
    icon: ImageVector,
    tintColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, tintColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = tintColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Usuario: $user", color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(description, color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(time, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
