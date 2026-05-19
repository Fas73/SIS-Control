package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.presentation.Destinos
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationDetailScreen(
    navController: NavController,
    token: String,
    role: String,
    installationId: Long,
    viewModel: AdminInstallationsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state
    val installation = state.installations.find { it.id == installationId }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Activos, 1: Inactivos

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
    var name by remember(installation) { mutableStateOf(installation.name) }
    var address by remember(installation) { mutableStateOf(installation.address) }
    var clientName by remember(installation) { mutableStateOf(installation.clientName ?: "") }
    var latitude by remember(installation) { mutableStateOf(installation.latitude?.toString() ?: "0.0") }
    var longitude by remember(installation) { mutableStateOf(installation.longitude?.toString() ?: "0.0") }
    var radius by remember(installation) { mutableStateOf(installation.radiusInMeters?.toString() ?: "100.0") }

    val isActive = (installation.status ?: 1) == 1

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryVariant)))
                    .statusBarsPadding()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(
                        text = "Detalle de Instalación",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Datos de Instalación
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Datos Generales", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 17.sp)
                            
                            if (isActive) {
                                if (!isEditMode) {
                                    TextButton(onClick = { isEditMode = true }) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Editar")
                                    }
                                } else {
                                    val isRadiusValid = (radius.toDoubleOrNull() ?: -1.0) >= 0.0
                                    Button(
                                        onClick = {
                                            viewModel.updateInstallation(
                                                installation.copy(
                                                    name = name,
                                                    address = address,
                                                    clientName = clientName,
                                                    latitude = latitude.toDoubleOrNull() ?: 0.0,
                                                    longitude = longitude.toDoubleOrNull() ?: 0.0,
                                                    radiusInMeters = radius.toDoubleOrNull()
                                                )
                                            )
                                            isEditMode = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = isRadiusValid
                                    ) {
                                        Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Checkbox de Estado estilizado
                        Surface(
                            color = if (isActive) SuccessColor.copy(alpha = 0.06f) else DangerColor.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isActive,
                                    onCheckedChange = { viewModel.toggleInstallationStatus(installationId) },
                                    colors = CheckboxDefaults.colors(checkedColor = SuccessColor)
                                )
                                Text(
                                    text = if (isActive) "Empresa en Servicio" else "Empresa Inactiva",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) SuccessColor else DangerColor,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        DetailFieldPolished(label = "Nombre de la Empresa", value = name, enabled = isEditMode, icon = Icons.Default.Business) { name = it }
                        DetailFieldPolished(label = "Dirección Física", value = address, enabled = isEditMode, icon = Icons.Default.LocationOn) { address = it }
                        DetailFieldPolished(label = "Contacto / Cliente", value = clientName, enabled = isEditMode, icon = Icons.Default.Person) { clientName = it }
                        
                        if (isEditMode) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.weight(1f)) {
                                    DetailFieldPolished(label = "Latitud", value = latitude, enabled = true, icon = Icons.Default.MyLocation) { latitude = it }
                                }
                                Box(Modifier.weight(1f)) {
                                    DetailFieldPolished(label = "Longitud", value = longitude, enabled = true, icon = Icons.Default.MyLocation) { longitude = it }
                                }
                            }
                            DetailFieldPolished(label = "Radio de Tolerancia (metros)", value = radius, enabled = true, icon = Icons.Default.RadioButtonChecked) { radius = it }
                            if ((radius.toDoubleOrNull() ?: 0.0) < 0.0) {
                                Text("El radio no puede ser negativo", color = DangerColor, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        } else {
                            HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ubicación GPS", fontSize = 12.sp, color = TextSecondary)
                                    Text("$latitude, $longitude", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.End) {
                                    Text("Radio", fontSize = 12.sp, color = TextSecondary)
                                    Text("$radius m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Sección de Checkpoints
            item {
                val activeCount = state.currentInstallationCheckpoints.count { (it.status ?: 1) == 1 }
                val inactiveCount = state.currentInstallationCheckpoints.count { (it.status ?: 1) == 0 }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Puntos de Control", 
                            fontWeight = FontWeight.Bold, 
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        
                        if (isActive) {
                            Button(
                                onClick = { 
                                    navController.navigate(
                                        Destinos.adminCreateCheckpointRoute(token, role, installationId, installation.name)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.AddLocationAlt, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("NUEVO", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Pestañas (Tabs)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        TabButtonPolished(
                            text = "Activos",
                            count = activeCount,
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f),
                            color = PrimaryColor
                        )
                        TabButtonPolished(
                            text = "Inactivos",
                            count = inactiveCount,
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f),
                            color = DangerColor
                        )
                    }
                }
            }

            if (state.isDetailLoading) {
                item { 
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
            } else {
                val activeList = state.currentInstallationCheckpoints.filter { (it.status ?: 1) == 1 }
                val inactiveList = state.currentInstallationCheckpoints.filter { (it.status ?: 1) == 0 }

                val currentList = if (selectedTab == 0) activeList.sortedBy { it.executionOrder } else inactiveList

                if (currentList.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if(selectedTab == 0) "Sin puntos activos" else "Sin puntos inactivos",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(currentList, key = { it.id }) { checkpoint ->
                        CheckpointCardPolished(
                            checkpoint = checkpoint,
                            installationActive = isActive,
                            allCheckpoints = state.currentInstallationCheckpoints,
                            selectedTab = selectedTab,
                            onUpdate = { updated ->
                                viewModel.updateCheckpoint(updated, installationId)
                            },
                            onToggleStatus = {
                                viewModel.toggleCheckpointStatus(checkpoint.id, installationId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButtonPolished(
    text: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else Color.Transparent,
            contentColor = if (selected) Color.White else TextSecondary
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Surface(
                color = if (selected) Color.White.copy(alpha = 0.25f) else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = if (selected) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
fun DetailFieldPolished(
    label: String, 
    value: String, 
    enabled: Boolean, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            leadingIcon = { Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(22.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                disabledContainerColor = Color(0xFFF9FAFB),
                disabledBorderColor = Color(0xFFF3F4F6),
                disabledTextColor = TextPrimary
            )
        )
    }
}

@Composable
fun CheckpointCardPolished(
    checkpoint: CheckpointDto,
    installationActive: Boolean,
    allCheckpoints: List<CheckpointDto>,
    selectedTab: Int,
    onUpdate: (CheckpointDto) -> Unit,
    onToggleStatus: () -> Unit
) {
    var isEditing by remember(checkpoint.id, selectedTab) { mutableStateOf(false) }
    var name by remember(checkpoint) { mutableStateOf(checkpoint.name) }
    var order by remember(checkpoint) { mutableStateOf(checkpoint.executionOrder.toString()) }
    var code by remember(checkpoint) { mutableStateOf(checkpoint.nfcTagCode) }
    var desc by remember(checkpoint) { mutableStateOf(checkpoint.locationDescription ?: "") }
    var instruction by remember(checkpoint) { mutableStateOf(checkpoint.instruction ?: "") }
    
    val isActive = (checkpoint.status ?: 1) == 1
    
    val isTextFieldOrderDuplicate = remember(order, allCheckpoints) {
        val newOrder = order.toIntOrNull()
        if (newOrder == null) false
        else allCheckpoints.any { it.id != checkpoint.id && (it.status ?: 1) == 1 && it.executionOrder == newOrder }
    }

    val isStoredOrderDuplicate = remember(checkpoint.executionOrder, allCheckpoints) {
        allCheckpoints.any { it.id != checkpoint.id && (it.status ?: 1) == 1 && it.executionOrder == checkpoint.executionOrder }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = {
                        if (installationActive && !isEditing) {
                            if (!isActive && isStoredOrderDuplicate) isEditing = true else onToggleStatus()
                        }
                    },
                    enabled = installationActive && !isEditing,
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                )
                
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = "${checkpoint.executionOrder}. ${checkpoint.name.toTitleCase()}", 
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) TextPrimary else Color.Gray,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text("Tag: ${checkpoint.nfcTagCode}", fontSize = 12.sp, color = TextSecondary)
                }
                
                if (installationActive && (isActive || isEditing)) {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit, 
                            contentDescription = null, 
                            tint = PrimaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                DetailFieldPolished(label = "Nombre Punto", value = name, enabled = true, icon = Icons.AutoMirrored.Filled.Label) { name = it }
                DetailFieldPolished(label = "Orden en la ronda", value = order, enabled = true, icon = Icons.Default.Reorder) { order = it }
                if (isTextFieldOrderDuplicate) {
                    Text("Orden ya ocupada por otro punto activo.", color = DangerColor, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                }
                DetailFieldPolished(label = "Código NFC", value = code, enabled = true, icon = Icons.Default.Nfc) { code = it }
                DetailFieldPolished(label = "Descripción", value = desc, enabled = true, icon = Icons.Default.Description) { desc = it }
                DetailFieldPolished(label = "Instrucción para Guardia", value = instruction, enabled = true, icon = Icons.Default.Info) { instruction = it }
                
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        onUpdate(checkpoint.copy(
                            name = name, 
                            executionOrder = order.toIntOrNull() ?: checkpoint.executionOrder,
                            nfcTagCode = code, 
                            locationDescription = desc,
                            instruction = if (instruction.isBlank()) null else instruction,
                            status = 1
                        ))
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isTextFieldOrderDuplicate && name.isNotBlank() && code.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(if (!isActive) "Corregir y Activar" else "Guardar Cambios", fontWeight = FontWeight.Bold)
                }
            } else if (checkpoint.locationDescription?.isNotBlank() == true || !checkpoint.instruction.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 48.dp)) {
                    if (checkpoint.locationDescription?.isNotBlank() == true) {
                        Text(checkpoint.locationDescription!!, fontSize = 13.sp, color = TextSecondary)
                    }
                    if (!checkpoint.instruction.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = PrimaryColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(checkpoint.instruction!!, fontSize = 12.sp, color = PrimaryColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
