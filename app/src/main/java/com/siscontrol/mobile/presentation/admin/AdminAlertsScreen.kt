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
import kotlinx.coroutines.launch
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
    var riskAnalysisResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    
    val panicCount = state.alerts.count { it.severity.uppercase() == "ALTA" || it.title.contains("PÁNICO", ignoreCase = true) }
    val advertenciaCount = state.alerts.count { it.severity.uppercase() == "MEDIA" }
    val infoCount = state.alerts.count { it.severity.uppercase() == "BAJA" || it.title.contains("COMPLETADA", ignoreCase = true) || it.title.contains("FINALIZADA", ignoreCase = true) }

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
                "Info" -> state.alerts.filter { it.severity.uppercase() == "BAJA" || it.title.contains("COMPLETADA", ignoreCase = true) || it.title.contains("FINALIZADA", ignoreCase = true) }
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
                            // AJUSTE DE SENSIBILIDAD: Solo eliminar si el swipe supera el 60% de la pantalla
                            val isDismissed = it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd
                            if (isDismissed) {
                                viewModel.dismissAlert(alert.id)
                                true
                            } else false
                        },
                        positionalThreshold = { totalDistance -> totalDistance * 0.6f } // Requiere deslizar más de la mitad
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
                        val isShiftEnd = alert.title.contains("FINALIZADA", ignoreCase = true) || alert.title.contains("SALIDA", ignoreCase = true)
                        
                        val userName = when {
                            !alert.username.isNullOrBlank() -> alert.username
                            alert.roundExecution?.worker?.fullName != null -> alert.roundExecution?.worker?.fullName ?: "Guardia"
                            isAccessRequest -> "Usuario Solicitante"
                            else -> "Guardia SIS"
                        }
                        
                        val locationName = when {
                            !alert.clientName.isNullOrBlank() -> alert.clientName
                            alert.roundExecution?.installation?.clientName != null -> alert.roundExecution?.installation?.clientName ?: "Sede"
                            isAccessRequest -> "Acceso Remoto"
                            else -> "Instalación SIS"
                        }
                        
                        val checkpointInfo = if (!alert.checkpointName.isNullOrBlank() && alert.checkpointName != "N/A") {
                            val orderText = if (alert.checkpointOrder != null) "N°${alert.checkpointOrder} - " else ""
                            "📍 Punto: $orderText${alert.checkpointName}"
                        } else null

                        // Ajuste según instrucción Backend: title para cabecera, checkpointName secundario
                        val finalTitle = alert.title.uppercase()

                        val context = androidx.compose.ui.platform.LocalContext.current

                        AlertCard(
                            title = finalTitle,
                            user = userName,
                            description = alert.description,
                            location = locationName,
                            checkpointInfo = checkpointInfo,
                            time = alert.createdAt?.formatDateToDisplay() ?: "Reciente",
                            imageUrl = alert.imageUrl,
                            onImageClick = { fullScreenImageUrl = it },
                            onManage = { viewModel.manageAlert(alert.id) },
                            onDismiss = { viewModel.dismissAlert(alert.id) },
                            onAnalyzeRisk = { text ->
                                riskAnalysisResult = com.siscontrol.mobile.core.AIManager.performRiskAnalysis(text)
                            },
                            onDownloadPdf = if (isShiftEnd && alert.roundExecutionId != null) {
                                {
                                    isGeneratingPdf = true
                                    viewModel.getShiftReportForAlert(alert.roundExecutionId!!) { report ->
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
                                                        context.startActivity(android.content.Intent.createChooser(intent, "Ver Reporte Final"))
                                                    }
                                                } finally {
                                                    isGeneratingPdf = false
                                                }
                                            }
                                        } else {
                                            isGeneratingPdf = false
                                        }
                                    }
                                }
                            } else null,
                            icon = when {
                                isPanic -> Icons.Default.Warning
                                isAccessRequest -> Icons.Default.VpnKey
                                isShiftEnd -> Icons.Default.AssignmentTurnedIn
                                !alert.checkpointName.isNullOrBlank() -> Icons.Default.LocationOff
                                isWarning -> Icons.Default.Notifications
                                else -> Icons.Default.Info
                            },
                            tintColor = when {
                                isPanic -> DangerColor
                                isAccessRequest -> PrimaryColor
                                isShiftEnd -> SuccessColor
                                isWarning -> Color(0xFFD97706)
                                else -> PrimaryColor
                            },
                            backgroundColor = when {
                                isPanic -> Color(0xFFFEF2F2)
                                isAccessRequest -> Color(0xFFEFF6FF)
                                isShiftEnd -> Color(0xFFF0FDF4)
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

    if (riskAnalysisResult != null) {
        AlertDialog(
            onDismissRequest = { riskAnalysisResult = null },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = PrimaryVariant)
                    Spacer(Modifier.width(10.dp))
                    Text("Auditoría IA de Riesgo", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = riskAnalysisResult!!,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { riskAnalysisResult = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("ENTENDIDO")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

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
                    Text("Generando Informe...", fontWeight = FontWeight.Bold)
                    Text("Descargando evidencias fotográficas", fontSize = 11.sp, color = TextSecondary)
                }
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
    checkpointInfo: String? = null,
    time: String,
    imageUrl: String? = null,
    onImageClick: (String) -> Unit = {},
    onManage: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onAnalyzeRisk: (String) -> Unit = {},
    onDownloadPdf: (() -> Unit)? = null,
    icon: ImageVector,
    tintColor: Color,
    backgroundColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, tintColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Icono decorativo de la alerta
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = tintColor, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 0.5.sp, modifier = Modifier.weight(1f))
                        
                        // BOTÓN DESCARGA PDF (SI APLICA)
                        if (onDownloadPdf != null) {
                            IconButton(onClick = onDownloadPdf, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.PictureAsPdf, "Descargar Informe", tint = PrimaryColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        // BOTÓN ANÁLISIS IA (Exclusivo Admin/Supervisor)
                        IconButton(onClick = { onAnalyzeRisk(description) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.AutoAwesome, "Análisis IA", tint = PrimaryVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("Usuario: $user", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Lógica de formateo profesional para Jefatura
                    val displayDescription = when {
                        description.contains("[CANCELACIÓN ADMINISTRATIVA]") -> {
                            val motivo = description.substringAfter("[CANCELACIÓN ADMINISTRATIVA]").trim()
                            "Jornada cerrada administrativamente por Jefatura.\nMotivo: ${motivo.ifBlank { "No especificado" }}"
                        }
                        description.contains("[CIERRE AUTOMÁTICO]") -> {
                            "Jornada finalizada automáticamente por el sistema (Cumplimiento de horario)."
                        }
                        else -> {
                            description.replace("[NFC Tag:", "\nNFC Tag:").replace("]", "")
                        }
                    }

                    Text(
                        text = displayDescription,
                        color = TextPrimary, 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                    // ... (rest of the card content remains the same)

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

            // Sección de Acciones
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = tintColor.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Acciones:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                OutlinedButton(
                    onClick = onManage,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                    border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Gestionada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                    border = BorderStroke(1.dp, DangerColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
