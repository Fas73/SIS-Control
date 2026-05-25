package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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
    
    val context = LocalContext.current
    
    // PERSISTENCIA DE URIS COMO STRING
    var capturedImageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCameraUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // LANZADORES
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUriStr != null) capturedImageUriStr = tempCameraUriStr
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) capturedImageUriStr = uri.toString()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            CameraUtils.createTempImageUri(context)?.let { uri ->
                tempCameraUriStr = uri.toString()
                cameraLauncher.launch(uri)
            }
        }
    }

    val state by viewModel.state
    val severityOptions = listOf("BAJA", "MEDIA", "ALTA")
    val typeOptions = listOf("ROBO", "VANDALISMO", "HALLAZGO", "MANTENCION", "OTRO")

    var expandedSeverity by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryVariant))).statusBarsPadding().padding(horizontal = 16.dp, vertical = 20.dp)) {
            Column {
                Text("Reportar Incidente", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Documenta evidencia de lo sucedido", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Detalles del Incidente", fontWeight = FontWeight.Bold, color = PrimaryColor)

                        Column {
                            Text("Título breve", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("Ej: Puerta forzada", color = TextPlaceholder) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                        }

                        Column {
                            Text("Categoría", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                                OutlinedTextField(value = type, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, tint = PrimaryColor) }, textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                                    typeOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { type = option; expandedType = false }) }
                                }
                            }
                        }

                        Column {
                            Text("Severidad", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            ExposedDropdownMenuBox(expanded = expandedSeverity, onExpandedChange = { expandedSeverity = !expandedSeverity }) {
                                OutlinedTextField(value = severity, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeverity) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.PriorityHigh, null, tint = if (severity == "ALTA") DangerColor else PrimaryColor) }, textStyle = LocalTextStyle.current.copy(color = if (severity == "ALTA") DangerColor else TextPrimary, fontWeight = FontWeight.Bold), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (severity == "ALTA") DangerColor else TextPrimary, unfocusedTextColor = if (severity == "ALTA") DangerColor else TextPrimary, focusedBorderColor = if (severity == "ALTA") DangerColor else PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                                ExposedDropdownMenu(expanded = expandedSeverity, onDismissRequest = { expandedSeverity = false }) {
                                    severityOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { severity = option; expandedSeverity = false }) }
                                }
                            }
                        }

                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Evidencia Fotográfica", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                OutlinedButton(onClick = { showImageSourceDialog = true }, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor)) {
                                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp), tint = PrimaryColor)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Agregar Imagen", fontSize = 12.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)).border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                if (capturedImageUriStr != null) {
                                    AsyncImage(model = Uri.parse(capturedImageUriStr), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                    IconButton(onClick = { capturedImageUriStr = null }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(48.dp)); Text("Sin imagen capturada", fontSize = 13.sp, color = TextPlaceholder, fontWeight = FontWeight.Medium) }
                                }
                            }
                        }

                        Column {
                            Text("Descripción detallada", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = description, onValueChange = { description = it }, placeholder = { Text("Describa lo sucedido...", color = TextPlaceholder) }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryColor, unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { 
                val finalUri = capturedImageUriStr?.let { Uri.parse(it) }
                viewModel.reportIncident(context, title, description, severity, type, roundExecutionId, finalUri, onSaveSuccess)
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SuccessColor), shape = RoundedCornerShape(12.dp), enabled = title.isNotBlank() && description.isNotBlank() && !state.isLoading) {
                if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("GUARDAR INCIDENTE", fontWeight = FontWeight.Bold) }
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(52.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp)) { Text("CANCELAR", fontWeight = FontWeight.Bold, color = TextPrimary) }
        }
    }

    if (showImageSourceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp, start = 24.dp, end = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Evidencia Fotográfica", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = TextPrimary
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                CameraUtils.createTempImageUri(context)?.let { uri -> tempCameraUriStr = uri.toString(); cameraLauncher.launch(uri) }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            showImageSourceDialog = false
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(44.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, null, tint = PrimaryColor)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Tomar Fotografía", fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            galleryLauncher.launch("image/*")
                            showImageSourceDialog = false
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(44.dp).background(SuccessColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = SuccessColor)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Elegir de Galería", fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
