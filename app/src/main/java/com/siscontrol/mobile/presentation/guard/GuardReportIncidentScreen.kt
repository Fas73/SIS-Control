package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.siscontrol.mobile.core.CameraUtils
import com.siscontrol.mobile.presentation.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardReportIncidentScreen(
    roundExecutionId: Long,
    viewModel: IncidentViewModel,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var severity by rememberSaveable { mutableStateOf("MEDIA") }
    var type by rememberSaveable { mutableStateOf("HALLAZGO") }
    
    // Camera logic
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri = tempUri
        }
    }

    val state by viewModel.state
    val severityOptions = listOf("BAJA", "MEDIA", "ALTA")
    val typeOptions = listOf("ROBO", "VANDALISMO", "HALLAZGO", "MANTENCION", "OTRO")

    var expandedSeverity by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryVariant)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    "Reportar Incidente",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Documenta evidencia de lo sucedido en la ronda",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        }

        // ── Scrollable Form ──────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Detalles del Incidente", fontWeight = FontWeight.Bold, color = PrimaryColor)

                        // Título
                        Column {
                            Text("Título breve", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("Ej: Puerta forzada") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Title, null, tint = PrimaryColor) }
                            )
                        }

                        // Tipo (Dropdown)
                        Column {
                            Text("Categoría", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            ExposedDropdownMenuBox(
                                expanded = expandedType,
                                onExpandedChange = { expandedType = !expandedType }
                            ) {
                                OutlinedTextField(
                                    value = type,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Icon(Icons.Default.Category, null, tint = PrimaryColor) }
                                )
                                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                                    typeOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = { type = option; expandedType = false }
                                        )
                                    }
                                }
                            }
                        }

                        // Gravedad (Dropdown)
                        Column {
                            Text("Severidad", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            ExposedDropdownMenuBox(
                                expanded = expandedSeverity,
                                onExpandedChange = { expandedSeverity = !expandedSeverity }
                            ) {
                                OutlinedTextField(
                                    value = severity,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeverity) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Icon(Icons.Default.PriorityHigh, null, tint = if (severity == "ALTA") DangerColor else PrimaryColor) }
                                )
                                ExposedDropdownMenu(expanded = expandedSeverity, onDismissRequest = { expandedSeverity = false }) {
                                    severityOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = { severity = option; expandedSeverity = false }
                                        )
                                    }
                                }
                            }
                        }

                        // Fotografía (Cámara Real)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Evidencia Fotográfica", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                OutlinedButton(
                                    onClick = {
                                        val uri = CameraUtils.createTempImageUri(context)
                                        tempUri = uri
                                        cameraLauncher.launch(uri)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tomar Foto", fontSize = 12.sp)
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (capturedImageUri != null) {
                                    AsyncImage(
                                        model = capturedImageUri,
                                        contentDescription = "Vista previa",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                    )
                                    IconButton(
                                        onClick = { capturedImageUri = null },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                        Text("Sin imagen capturada", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        // Descripción
                        Column {
                            Text("Descripción detallada", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = { Text("Describe detalladamente lo sucedido...") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 5
                            )
                        }
                    }
                }
            }

            // Info hint
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "La evidencia fotográfica será vinculada automáticamente a la ronda en curso.",
                            color = PrimaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (state.error != null) {
                item {
                    Surface(
                        color = DangerColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(state.error!!, color = DangerColor, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Action buttons ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    viewModel.reportIncident(title, description, severity, type, roundExecutionId, capturedImageUri, onSaveSuccess)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && description.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("GUARDAR INCIDENTE", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading
            ) {
                Text("CANCELAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
