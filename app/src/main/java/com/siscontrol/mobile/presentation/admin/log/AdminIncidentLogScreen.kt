package com.siscontrol.mobile.presentation.admin.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.siscontrol.mobile.domain.model.Incident
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.formatDateToDisplay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIncidentLogScreen(
    paddingValues: PaddingValues,
    viewModel: AdminIncidentLogViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Día, 1: Semana, 2: Mes
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedIncidentId by remember { mutableStateOf<Long?>(null) }

    val selectedIncident = remember(state.allIncidents, selectedIncidentId) {
        state.allIncidents.find { it.id == selectedIncidentId }
    }

    val filteredIncidents = remember(state.allIncidents, selectedTab, searchQuery) {
        val now = LocalDateTime.now()
        
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
                    0 -> ChronoUnit.DAYS.between(incidentDate, now) == 0L
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
            .background(BackgroundColor)
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
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.DarkGray
                )
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PrimaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryColor
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Hoy") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Semana") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Mes") })
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredIncidents.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No se encontraron incidentes", color = TextSecondary)
                        }
                    }
                }

                items(filteredIncidents) { incident ->
                    IncidentLogCard(incident) {
                        selectedIncidentId = incident.id
                    }
                }
            }
        }
    }

    if (selectedIncident != null) {
        IncidentDetailDialog(
            incident = selectedIncident,
            iaLoadingIncidentId = state.iaLoadingIncidentId,
            iaError = state.iaError,
            onAnalyzeClick = { viewModel.analizarIncidenteConIa(selectedIncident.id ?: 0) },
            onDismiss = {
                viewModel.clearIaError()
                selectedIncidentId = null
            }
        )
    }
}

@Composable
fun IncidentLogCard(incident: Incident, onClick: () -> Unit) {
    val isPanic = incident.severity.uppercase() == "ALTA" || incident.title.contains("PÁNICO", ignoreCase = true)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if(isPanic) DangerColor.copy(alpha = 0.5f) else Color(0xFFE5E7EB))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isPanic) DangerColor.copy(alpha = 0.1f) else PrimaryColor.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPanic) Icons.Default.Warning else Icons.Default.Report,
                    null,
                    tint = if (isPanic) DangerColor else PrimaryColor
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(incident.title.uppercase(), fontWeight = FontWeight.Bold, color = if(isPanic) DangerColor else TextPrimary, fontSize = 14.sp)
                Text(incident.clientName ?: "Instalación General", color = TextSecondary, fontSize = 12.sp)
                Text("Por: ${incident.username ?: "Guardia SIS"}", color = TextSecondary, fontSize = 12.sp)
            }
            Text(incident.createdAt?.take(10) ?: "", color = TextPlaceholder, fontSize = 11.sp)
        }
    }
}

@Composable
fun IncidentDetailDialog(
    incident: Incident,
    iaLoadingIncidentId: Long?,
    iaError: String?,
    onAnalyzeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                Text("CERRAR")
            }
        },
        title = {
            Text(incident.title, fontWeight = FontWeight.Black, color = TextPrimary)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    DetailRow(Icons.Default.Business, "Instalación", incident.clientName ?: "General")
                    DetailRow(Icons.Default.Person, "Guardia", incident.username ?: "No registrado")
                    DetailRow(Icons.Default.Event, "Fecha y Hora", (incident.createdAt ?: "").formatDateToDisplay())
                    DetailRow(Icons.Default.PriorityHigh, "Gravedad", incident.severity)

                    Spacer(Modifier.height(8.dp))
                    Text("OBSERVACIONES / COMENTARIOS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = TextSecondary)
                    Text(incident.description, color = TextPrimary, fontSize = 14.sp)

                    if (incident.imageUrl != null) {
                        Spacer(Modifier.height(16.dp))
                        Text("EVIDENCIA FOTOGRÁFICA", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            AsyncImage(
                                model = incident.imageUrl,
                                contentDescription = "Evidencia",
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    AIAnalysisSection(
                        incident = incident,
                        isLoading = iaLoadingIncidentId == incident.id,
                        error = if (iaLoadingIncidentId == null) iaError else null,
                        onAnalyzeClick = onAnalyzeClick
                    )
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AIAnalysisSection(
    incident: Incident,
    isLoading: Boolean,
    error: String?,
    onAnalyzeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado IA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Gemini AI",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "ANÁLISIS GEMINI AI",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF4F46E5),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Analizando novedad con Inteligencia Artificial...",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                incident.estadoAnalisisIA == "ANALIZADO" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Badges de prioridad y atención
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val prioColor = when (incident.prioridadIA?.uppercase()) {
                                "ALTA" -> DangerColor
                                "MEDIA" -> Color(0xFFD97706)
                                else -> Color(0xFF059669)
                            }
                            val prioBg = when (incident.prioridadIA?.uppercase()) {
                                "ALTA" -> Color(0xFFFEF2F2)
                                "MEDIA" -> Color(0xFFFFFBEB)
                                else -> Color(0xFFECFDF5)
                            }
                            Surface(
                                color = prioBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, prioColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "PRIORIDAD: ${incident.prioridadIA ?: "MEDIA"}",
                                    color = prioColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (incident.requiereAtencionInmediata == true) {
                                Surface(
                                    color = Color(0xFFFFF7ED),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEA580C), modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("CRÍTICO", color = Color(0xFFEA580C), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        DetailItem("Clasificación de IA:", incident.tipoIncidenteIA ?: "No clasificado")

                        Column {
                            Text("RESUMEN DE SITUACIÓN", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
                            Spacer(Modifier.height(2.dp))
                            Text(incident.resumenIA ?: "", fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                        }

                        Column {
                            Text("ACCIÓN RECOMENDADA POR IA", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
                            Spacer(Modifier.height(2.dp))
                            Text(incident.accionSugeridaIA ?: "", fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                        }
                    }
                }
                else -> {
                    if (error != null) {
                        Text(
                            text = "⚠ Error: $error",
                            color = DangerColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = onAnalyzeClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (error != null) "REINTENTAR ANÁLISIS IA" else "GENERAR REPORTE IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Spacer(Modifier.width(4.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
    }
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

