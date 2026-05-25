package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.components.FullScreenImageDialog
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.formatDateToDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlertsScreen(
    paddingValues: PaddingValues,
    viewModel: AdminAlertsViewModel
) {
    val state by viewModel.state
    var selectedFilter by remember { mutableStateOf("Todas") }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    
    val panicCount = state.alerts.count { it.severity.uppercase() == "ALTA" || it.title.contains("PÁNICO", ignoreCase = true) }
    val advertenciaCount = state.alerts.count { it.severity.uppercase() == "MEDIA" }
    val infoCount = state.alerts.count { it.severity.uppercase() == "BAJA" || it.title.contains("completada", ignoreCase = true) }

    val filterOptions = listOf(
        "Todas" to state.alerts.size,
        "Pánico" to panicCount,
        "Advertencia" to advertenciaCount,
        "Info" to infoCount
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Forzado blanco
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Centro de Alertas",
            subtitle = "Monitoreo en tiempo real",
            showAdminLogo = true
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { (label, count) ->
                    val isSelected = label == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = label },
                        label = { Text("$label ($count)", color = if (isSelected) PrimaryColor else Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        border = null,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (state.isLoading && state.alerts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            val filteredAlerts = when (selectedFilter) {
                "Pánico" -> state.alerts.filter { it.severity.uppercase() == "ALTA" || it.title.contains("PÁNICO", ignoreCase = true) }
                "Advertencia" -> state.alerts.filter { it.severity.uppercase() == "MEDIA" }
                "Info" -> state.alerts.filter { it.severity.uppercase() == "BAJA" || it.title.contains("completada", ignoreCase = true) }
                else -> state.alerts
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredAlerts.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No hay alertas en esta categoría", color = TextSecondary)
                        }
                    }
                }

                items(filteredAlerts, key = { it.id ?: 0L }) { alert ->
                    val isPanic = alert.severity.uppercase() == "ALTA" || alert.title.contains("PÁNICO", ignoreCase = true)
                    val isWarning = alert.severity.uppercase() == "MEDIA"
                    
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.dismissAlert(alert.id)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.5f)
                                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) 
                                    Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                            }
                        }
                    ) {
                        // Lógica de visualización amigable para evitar #N/A
                        val isAccessRequest = alert.title.contains("ACCESO", ignoreCase = true)
                        
                        val userName = when {
                            !alert.username.isNullOrBlank() -> alert.username
                            alert.roundExecution?.worker?.fullName != null -> alert.roundExecution.worker.fullName
                            isAccessRequest -> "Usuario Solicitante"
                            else -> "Guardia SIS"
                        }
                        
                        val locationName = when {
                            !alert.clientName.isNullOrBlank() -> alert.clientName
                            alert.roundExecution?.installation?.clientName != null -> alert.roundExecution.installation.clientName
                            isAccessRequest -> "Acceso Remoto"
                            else -> "Instalación SIS"
                        }
                        
                        val checkpointInfo = if (alert.checkpointName != null && alert.checkpointName != "N/A") {
                            val orderText = if (alert.checkpointOrder != null) "N°${alert.checkpointOrder} - " else ""
                            "📍 Punto: $orderText${alert.checkpointName}"
                        } else null

                        val finalTitle = when {
                            isAccessRequest -> "SOLICITUD DE ACCESO"
                            !alert.checkpointName.isNullOrBlank() -> alert.checkpointName.uppercase()
                            alert.title.contains("Evidencia", ignoreCase = true) -> "CHECKPOINT NO ESCANEADO"
                            else -> alert.title.uppercase()
                        }

                        AlertCard(
                            title = finalTitle,
                            user = userName,
                            description = alert.description,
                            location = locationName,
                            checkpointInfo = checkpointInfo,
                            time = alert.createdAt?.formatDateToDisplay() ?: "Reciente",
                            imageUrl = alert.imageUrl,
                            onImageClick = { fullScreenImageUrl = it },
                            icon = when {
                                isPanic -> Icons.Default.Warning
                                isAccessRequest -> Icons.Default.VpnKey
                                !alert.checkpointName.isNullOrBlank() -> Icons.Default.LocationOff
                                isWarning -> Icons.Default.Notifications
                                else -> Icons.Default.Info
                            },
                            tintColor = when {
                                isPanic -> DangerColor
                                isAccessRequest -> PrimaryColor
                                isWarning -> Color(0xFFD97706)
                                else -> PrimaryColor
                            },
                            backgroundColor = when {
                                isPanic -> Color(0xFFFEF2F2)
                                isAccessRequest -> Color(0xFFEFF6FF)
                                isWarning -> Color(0xFFFFFBEB)
                                else -> Color(0xFFEFF6FF)
                            }
                        )
                    }
                }
            }
        }
    }

    if (fullScreenImageUrl != null) {
        FullScreenImageDialog(imageUrl = fullScreenImageUrl!!) {
            fullScreenImageUrl = null
        }
    }
}

@Composable
fun AlertCard(
    title: String,
    user: String,
    description: String,
    location: String,
    checkpointInfo: String? = null,
    time: String,
    imageUrl: String? = null,
    onImageClick: (String) -> Unit = {},
    icon: ImageVector,
    tintColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, tintColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = tintColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(24.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = tintColor, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("Usuario: $user", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Lógica para limpiar y separar el dato del NFC de forma profesional
                    val formattedDescription = description
                        .replace("[NFC Tag:", "\nNFC Tag:")
                        .replace("]", "")

                    Text(
                        text = formattedDescription,
                        color = TextPrimary, 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )

                    // Mostrar info del punto si existe
                    if (checkpointInfo != null) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = tintColor.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = checkpointInfo,
                                color = tintColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    if (!imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Evidencia",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, tintColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { onImageClick(imageUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = tintColor.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(location, color = TextSecondary, fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(time, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
