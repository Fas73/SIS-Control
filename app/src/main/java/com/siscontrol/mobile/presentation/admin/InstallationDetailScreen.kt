package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationDetailScreen(
    installationId: Long,
    viewModel: AdminInstallationsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state
    val installation = state.installations.find { it.id == installationId }

    // Cargar checkpoints al entrar
    LaunchedEffect(installationId) {
        viewModel.loadCheckpointsForInstallation(installationId)
    }

    if (installation == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Instalación no encontrada")
        }
        return
    }

    // Estados para edición
    var name by remember { mutableStateOf(installation.name) }
    var address by remember { mutableStateOf(installation.address) }
    var clientName by remember { mutableStateOf(installation.clientName ?: "") }
    var location by remember { mutableStateOf(installation.location ?: "") }
    var status by remember { mutableStateOf(installation.status ?: 1) }

    val isActive = status == 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Instalación", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                actions = {
                    if (isActive) {
                        IconButton(onClick = {
                            viewModel.updateInstallation(
                                installation.copy(
                                    name = name,
                                    address = address,
                                    clientName = clientName,
                                    location = location,
                                    status = status
                                )
                            )
                        }) {
                            Icon(Icons.Default.Save, "Guardar", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = BackgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Datos de Instalación
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Datos Generales", fontWeight = FontWeight.Bold, color = PrimaryColor)

                    // Checkbox de Estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isActive,
                            onCheckedChange = { 
                                val newStatus = if (it) 1 else 0
                                status = newStatus
                                // Actualizar inmediatamente el estado
                                viewModel.updateInstallation(installation.copy(status = newStatus))
                            }
                        )
                        Text(
                            text = if (isActive) "Estado: ACTIVA" else "Estado: INACTIVA",
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) SuccessColor else DangerColor
                        )
                    }

                    // Campos editables (solo si está activa)
                    DetailField(label = "Nombre Empresa", value = name, enabled = isActive) { name = it }
                    DetailField(label = "Dirección", value = address, enabled = isActive) { address = it }
                    DetailField(label = "Cliente", value = clientName, enabled = isActive) { clientName = it }
                    DetailField(label = "Ubicación", value = location, enabled = isActive) { location = it }
                }
            }

            // Sección de Checkpoints
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Puntos de Control (Checkpoints)", fontWeight = FontWeight.Bold, color = PrimaryColor)
                    if (isActive) {
                        Button(
                            onClick = { /* Abrir diálogo o navegar para crear */ },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Text("Añadir", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (state.isDetailLoading) {
                item { CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) }
            } else {
                items(state.currentInstallationCheckpoints) { checkpoint ->
                    CheckpointItem(
                        checkpoint = checkpoint,
                        installationActive = isActive,
                        onUpdate = { updated ->
                            viewModel.updateCheckpoint(updated, installationId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailField(label: String, value: String, enabled: Boolean, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray,
                disabledBorderColor = Color.LightGray
            )
        )
    }
}

@Composable
fun CheckpointItem(
    checkpoint: CheckpointDto,
    installationActive: Boolean,
    onUpdate: (CheckpointDto) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(checkpoint.name) }
    var code by remember { mutableStateOf(checkpoint.nfcTagCode) }
    var desc by remember { mutableStateOf(checkpoint.locationDescription) }
    var status by remember { mutableStateOf(checkpoint.status ?: 1) }

    val isActive = status == 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = {
                        if (installationActive) {
                            val newStatus = if (it) 1 else 0
                            status = newStatus
                            onUpdate(checkpoint.copy(status = newStatus))
                        }
                    },
                    enabled = installationActive
                )
                Text(
                    checkpoint.name, 
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) TextPrimary else Color.Gray
                )
            }
            
            if (installationActive && isActive) {
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                }
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailField(label = "Nombre Punto", value = name, enabled = true) { name = it }
            DetailField(label = "Código NFC", value = code, enabled = true) { code = it }
            DetailField(label = "Descripción", value = desc, enabled = true) { desc = it }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onUpdate(checkpoint.copy(name = name, nfcTagCode = code, locationDescription = desc))
                    isEditing = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Actualizar Punto")
            }
        } else {
            Text("NFC: ${checkpoint.nfcTagCode}", fontSize = 12.sp, color = TextSecondary)
            Text(checkpoint.locationDescription, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
