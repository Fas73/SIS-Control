package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.formatDateToDisplay
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.siscontrol.mobile.presentation.components.FullScreenImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardRoundDetailScreen(
    roundId: Long,
    viewModel: GuardRoundViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(roundId) {
        viewModel.loadPastRoundDetail(roundId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Ronda #$roundId", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // BOTÓN DE DESCARGA PDF
                    IconButton(onClick = {
                        try {
                            // Generamos el objeto dummy para el PDF basado en el estado actual
                            val detail = com.siscontrol.mobile.data.remote.dto.RoundDetailResponseDto(
                                ronda = com.siscontrol.mobile.data.remote.dto.RoundResponseDto(
                                    id = roundId,
                                    observations = state.terminationReason,
                                    startTime = state.scanTimes.values.firstOrNull(),
                                    installation = com.siscontrol.mobile.data.remote.dto.InstallationDto(
                                        name = "Instalación SIS"
                                    )
                                ),
                                escaneos = state.checkpoints.filter { it.id in state.executedCheckpointIds }.map {
                                    com.siscontrol.mobile.data.remote.dto.ChecklogDto(
                                        id = it.id,
                                        checkpoint = it,
                                        scannedAt = state.scanTimes[it.id]
                                    )
                                }
                            )
                            val file = com.siscontrol.mobile.core.PdfManager.generateRoundReport(context, detail)
                            if (file != null) {
                                // Abrir el PDF generado
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "com.siscontrol.mobile.fileprovider",
                                    file
                                )
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Abrir Reporte"))
                            } else {
                                android.widget.Toast.makeText(context, "No se pudo generar el PDF", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PDF_ERROR", "Error al abrir PDF: ${e.message}")
                            android.widget.Toast.makeText(context, "Instale un lector de PDF para ver el reporte", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundColor
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // SECCIÓN 1: Resumen de Marcajes
                item {
                    Text("PUNTOS VERIFICADOS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
                }

                items(state.checkpoints.sortedBy { it.executionOrder }) { checkpoint ->
                    val isCompleted = checkpoint.id in state.executedCheckpointIds
                    CheckpointListItem(
                        number = checkpoint.executionOrder,
                        title = checkpoint.name ?: "Punto",
                        time = if (isCompleted) formatTime(state.scanTimes[checkpoint.id]) else "No escaneado",
                        isCompleted = isCompleted,
                        isActive = false
                    )
                }

                // SECCIÓN 2: Incidentes y Evidencias
                if (state.pastIncidents.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("INCIDENTES Y EVIDENCIAS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
                    }

                    items(state.pastIncidents) { incident ->
                        val isPanic = incident.title.contains("PÁNICO", ignoreCase = true)
                        
                        // Lógica Visual Pura: Confiamos 100% en lo que el Backend envíe "masticado"
                        val nombrePunto = if (!incident.checkpointName.isNullOrBlank()) {
                            incident.checkpointName.uppercase()
                        } else {
                            incident.title.uppercase()
                        }
                        
                        val ordenPunto = incident.checkpointOrder
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPanic) Color(0xFFFEF2F2) else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (isPanic) DangerColor.copy(alpha = 0.3f) else Color(0xFFE5E7EB)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (isPanic) Icons.Default.Warning else Icons.Default.LocationOff, 
                                        null, 
                                        tint = if (isPanic) DangerColor else PrimaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = nombrePunto,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = if (isPanic) DangerColor else TextPrimary
                                        )
                                        if (ordenPunto != null && ordenPunto > 0) {
                                            Text(
                                                text = "CHECKPOINT NO ESCANEADO: Punto de Control N° $ordenPunto",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                                
                                // Lógica de limpieza para el dato del NFC
                                val cleanDescription = incident.description
                                    .replace("[NFC Tag:", "\nNFC Tag:")
                                    .replace("]", "")

                                Text(
                                    text = cleanDescription,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    lineHeight = 20.sp
                                )

                                if (!incident.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = incident.imageUrl,
                                        contentDescription = "Evidencia",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { fullScreenImageUrl = incident.imageUrl },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                Text(
                                    text = incident.createdAt?.formatDateToDisplay() ?: "",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                // SECCIÓN 3: Inteligencia de Ronda (IA)
                if (!state.aiAnalysis.isNullOrBlank()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("INTELIGENCIA DE RONDA (IA)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Análisis de Desempeño", fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = state.aiAnalysis ?: "", 
                                    color = TextPrimary, 
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // SECCIÓN 4: Observaciones Finales
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("RESUMEN DE AUDITORÍA", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Observaciones del Sistema/Guardia:", fontWeight = FontWeight.Bold, color = PrimaryColor)
                            Text(
                                text = state.terminationReason ?: "Sin observaciones registradas.", 
                                color = TextPrimary, 
                                modifier = Modifier.padding(top = 4.dp),
                                lineHeight = 20.sp
                            )
                        }
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
