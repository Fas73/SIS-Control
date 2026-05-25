package com.siscontrol.mobile.presentation.admin.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.presentation.components.FullScreenImageDialog
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
    var selectedIncident by remember { mutableStateOf<IncidentDto?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

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
            .background(Color.White)
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
                        selectedIncident = incident
                    }
                }
            }
        }
    }

    if (selectedIncident != null) {
        IncidentDetailDialog(
            incident = selectedIncident!!,
            onDismiss = { selectedIncident = null },
            onImageClick = { fullScreenImageUrl = it }
        )
    }

    if (fullScreenImageUrl != null) {
        FullScreenImageDialog(imageUrl = fullScreenImageUrl!!) {
            fullScreenImageUrl = null
        }
    }
}

@Composable
fun IncidentLogCard(incident: IncidentDto, onClick: () -> Unit) {
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
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    
                    if (incident.checkpointName != null && incident.checkpointName != "N/A") {
                        val orderText = if (incident.checkpointOrder != null) "N°${incident.checkpointOrder} - " else ""
                        DetailRow(Icons.Default.Place, "Punto Omitido", "$orderText${incident.checkpointName}")
                    }

                    DetailRow(Icons.Default.Person, "Guardia", incident.username ?: "No registrado")
                    DetailRow(Icons.Default.Event, "Fecha y Hora", (incident.createdAt ?: "").formatDateToDisplay())
                    DetailRow(Icons.Default.PriorityHigh, "Gravedad", incident.severity)
                    
                    Spacer(Modifier.height(8.dp))
                    Text("OBSERVACIONES / COMENTARIOS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = TextSecondary)
                    Text(incident.description, color = TextPrimary, fontSize = 14.sp)
                    
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
