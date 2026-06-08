package com.siscontrol.mobile.presentation.admin.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.presentation.components.FullScreenImageDialog
import com.siscontrol.mobile.presentation.components.ShimmerCardItem
import com.siscontrol.mobile.presentation.components.EmptyState
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.formatDateToDisplay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AdminIncidentLogScreen(
    paddingValues: PaddingValues,
    viewModel: AdminIncidentLogViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Día, 1: Semana, 2: Mes
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedIncident by remember { mutableStateOf<IncidentDto?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.loadIncidents()
                delay(800)
                isRefreshing = false
            }
        }
    )

    val filteredIncidents = remember(state.allIncidents, selectedTab, searchQuery) {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        
        state.allIncidents.filter { incident ->
            val incidentDate = try {
                if (incident.createdAt?.contains("T") == true) {
                    LocalDateTime.parse(incident.createdAt.substringBefore("."), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } else if (incident.createdAt != null) {
                    LocalDateTime.parse(incident.createdAt + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } else null
            } catch (e: Exception) {
                null
            }

            val isInTimeRange = if (incidentDate == null) false else {
                when (selectedTab) {
                    0 -> incidentDate.toLocalDate().isEqual(today)
                    1 -> ChronoUnit.DAYS.between(incidentDate, now) <= 7L
                    2 -> ChronoUnit.DAYS.between(incidentDate, now) <= 30L
                    else -> true
                }
            }

            val matchesSearch = searchQuery.isEmpty() ||
                    (incident.clientName ?: "").contains(searchQuery, ignoreCase = true) ||
                    (incident.username ?: "").contains(searchQuery, ignoreCase = true) ||
                    (incident.title).contains(searchQuery, ignoreCase = true)

            isInTimeRange && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // Custom Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(PrimaryColor, PrimaryVariant)))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Column {
                    Text("Bitácora de Incidentes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Historial de novedades y pánicos", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por instalación o guardia...", color = TextPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryColor) },
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.DarkGray
                )
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryColor
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Hoy") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Semana") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Mes") })
        }

        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoading && !isRefreshing) {
                    items(5) {
                        ShimmerCardItem()
                    }
                } else {
                    if (filteredIncidents.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Sin Incidentes",
                                description = "No hay novedades registradas en este periodo.",
                                icon = Icons.Default.EventNote,
                                modifier = Modifier.padding(top = 40.dp)
                            )
                        }
                    }

                    items(filteredIncidents, key = { it.id ?: it.hashCode() }) { incident ->
                        Column {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                            ) {
                                IncidentLogCard(incident) {
                                    selectedIncident = incident
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = PrimaryColor
            )
        }
    }

    if (selectedIncident != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        IncidentDetailDialog(
            incident = selectedIncident!!,
            onDismiss = { selectedIncident = null },
            onImageClick = { fullScreenImageUrl = it },
            onDownloadPdf = { shiftId ->
                selectedIncident = null // Cierra el cuadro flotante
                isGeneratingPdf = true
                viewModel.getShiftReportForAlert(shiftId) { report ->
                    if (report != null) {
                        scope.launch {
                            try {
                                val file = com.siscontrol.mobile.core.PdfManager.generateConsolidatedShiftReport(context, report)
                                if (file != null) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context, "com.siscontrol.mobile.fileprovider", file
                                    )
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
        )
    }

    if (fullScreenImageUrl != null) {
        FullScreenImageDialog(imageUrl = fullScreenImageUrl!!) {
            fullScreenImageUrl = null
        }
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
                    Text("Recuperando Auditoría...", fontWeight = FontWeight.Bold)
                    Text("Ensamblando evidencias y análisis IA", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun IncidentLogCard(incident: IncidentDto, onClick: () -> Unit) {
    val isPanic = incident.severity.uppercase() == "ALTA" || incident.title.contains("PÁNICO", ignoreCase = true)
    val isSystemAlert = incident.title.contains("COMPLETADA", ignoreCase = true) || incident.title.contains("FINALIZADA", ignoreCase = true)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemAlert) Color(0xFFF0FDF4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if(isPanic) DangerColor.copy(alpha = 0.5f) 
            else if(isSystemAlert) SuccessColor.copy(alpha = 0.3f)
            else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when {
                            isPanic -> DangerColor.copy(alpha = 0.1f)
                            isSystemAlert -> SuccessColor.copy(alpha = 0.1f)
                            else -> PrimaryColor.copy(alpha = 0.1f)
                        }, 
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isPanic -> Icons.Default.Warning
                        isSystemAlert -> Icons.Default.CheckCircle
                        else -> Icons.Default.Report
                    },
                    contentDescription = null,
                    tint = when {
                        isPanic -> DangerColor
                        isSystemAlert -> SuccessColor
                        else -> PrimaryColor
                    }
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Ajuste según instrucción Backend: title para cabecera, checkpointName secundario
                Text(incident.title.uppercase(), fontWeight = FontWeight.Bold, color = if(isPanic) DangerColor else TextPrimary, fontSize = 14.sp)
                
                val locationInfo = if (!incident.checkpointName.isNullOrBlank() && incident.checkpointName != "N/A") {
                    "${incident.clientName ?: "Instalación"} • ${incident.checkpointName}"
                } else {
                    incident.clientName ?: "Instalación General"
                }
                
                Text(locationInfo, color = TextSecondary, fontSize = 12.sp)
                Text("Por: ${incident.username ?: "Guardia SIS"}", color = TextSecondary, fontSize = 12.sp)
                
                Text(
                    text = incident.createdAt?.formatDateToDisplay() ?: "", 
                    color = PrimaryColor, 
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!incident.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                AsyncImage(
                    model = incident.imageUrl,
                    contentDescription = "Miniatura",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun IncidentDetailDialog(
    incident: IncidentDto, 
    onImageClick: (String) -> Unit,
    onDismiss: () -> Unit,
    onDownloadPdf: ((Long) -> Unit)? = null
) {
    val isShiftEnd = incident.title.contains("FINALIZADA", ignoreCase = true) || incident.title.contains("SALIDA", ignoreCase = true)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (isShiftEnd && incident.roundExecutionId != null && onDownloadPdf != null) {
                Button(
                    onClick = { onDownloadPdf(incident.roundExecutionId) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VER INFORME FINAL", fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Button(
                    onClick = onDismiss, 
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CERRAR", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (isShiftEnd && incident.roundExecutionId != null && onDownloadPdf != null) {
                TextButton(onClick = onDismiss) {
                    Text("CERRAR", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        },
        title = {
            Text(incident.title, fontWeight = FontWeight.Black, color = TextPrimary)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    DetailRow(Icons.Default.Business, "Instalación", incident.clientName ?: "General")
                    
                    if (incident.checkpointName != null && incident.checkpointName != "N/A") {
                        val orderText = if (incident.checkpointOrder != null) "N°${incident.checkpointOrder} - " else ""
                        DetailRow(Icons.Default.Place, "Punto Omitido", "$orderText${incident.checkpointName}")
                    }

                    DetailRow(Icons.Default.Person, "Guardia", incident.username ?: "No registrado")
                    
                    // Mostrar tiempos si es una Ronda o Jornada
                    val startTime = incident.roundExecution?.startTime
                    val endTime = incident.roundExecution?.endTime ?: incident.createdAt
                    
                    if (startTime != null) {
                        DetailRow(Icons.Default.PlayArrow, "Hora Inicio", startTime.formatDateToDisplay())
                        DetailRow(Icons.Default.Stop, "Hora Término", (endTime ?: "").formatDateToDisplay())
                    } else {
                        DetailRow(Icons.Default.Event, "Fecha y Hora", (incident.createdAt ?: "").formatDateToDisplay())
                    }

                    DetailRow(Icons.Default.PriorityHigh, "Gravedad", incident.severity)
                    
                    Spacer(Modifier.height(8.dp))
                    Text("OBSERVACIONES / COMENTARIOS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = TextSecondary)
                    
                    val displayDescription = when {
                        incident.description.contains("[CANCELACIÓN ADMINISTRATIVA]") -> {
                            val motivo = incident.description.substringAfter("[CANCELACIÓN ADMINISTRATIVA]").trim()
                            "Jornada cerrada administrativamente por Jefatura.\nMotivo: ${motivo.ifBlank { "No especificado" }}"
                        }
                        incident.description.contains("[CIERRE AUTOMÁTICO]") -> {
                            "Jornada finalizada automáticamente por el sistema (Cumplimiento de horario)."
                        }
                        else -> incident.description
                    }

                    Text(displayDescription, color = TextPrimary, fontSize = 14.sp)
                    
                    if (!incident.imageUrl.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text("EVIDENCIA FOTOGRÁFICA", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.clickable { onImageClick(incident.imageUrl) },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            AsyncImage(
                                model = incident.imageUrl,
                                contentDescription = "Evidencia",
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Black)
        }
    }
}
