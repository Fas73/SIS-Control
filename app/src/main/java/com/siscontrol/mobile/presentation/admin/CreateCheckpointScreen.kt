package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCheckpointScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var nfcCode by rememberSaveable { mutableStateOf("") }
    var installationName by rememberSaveable { mutableStateOf("") } 
    var order by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf("-33.4372") }
    var lng by rememberSaveable { mutableStateOf("-70.6506") }
    var instructions by rememberSaveable { mutableStateOf("") }
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    val installations = listOf("Plaza Centro", "Edificio Norte", "Bodega Sur")

    val isFormValid = name.isNotBlank() && nfcCode.isNotBlank() && installationName.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                "Nuevo Checkpoint", 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                "Configure el punto de control NFC", 
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                        Text("Volver", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.offset(x = (-8).dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryVariant
                )
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Purple Info Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAF5FF), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Los checkpoints son puntos de control que los guardias deben escanear durante sus rondas",
                    color = Color(0xFF6B21A8),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: Información del Checkpoint
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Información del Checkpoint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nombre del checkpoint", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ej: Entrada Principal", color = TextSecondary, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = PrimaryVariant
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Instalación", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = installationName,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar instalación", color = TextSecondary, fontSize = 14.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = PrimaryVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        installations.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    installationName = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Numbers, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Orden en la ronda", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = order,
                    onValueChange = { order = it },
                    placeholder = { Text("1, 2, 3...", color = TextSecondary, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = PrimaryVariant
                    )
                )
                Text(
                    "Posición del checkpoint en la secuencia de la ronda", 
                    color = TextSecondary, 
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Código NFC
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Código NFC",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Código del tag NFC", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nfcCode,
                        onValueChange = { nfcCode = it },
                        placeholder = { Text("NFC-XXXXXX", color = TextSecondary, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = PrimaryVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { nfcCode = "NFC-${(100000..999999).random()}" },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3E8FF),
                            contentColor = Color(0xFFA855F7)
                        )
                    ) {
                        Text("Generar", fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "Código único que identifica el tag NFC físico", 
                    color = TextSecondary, 
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Ubicación GPS (Opcional)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Ubicación GPS (Opcional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Latitud", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = lat,
                            onValueChange = { lat = it },
                            placeholder = { Text("-33.4372", color = TextSecondary, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = PrimaryVariant
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Longitud", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = lng,
                            onValueChange = { lng = it },
                            placeholder = { Text("-70.6506", color = TextSecondary, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = PrimaryVariant
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Get current location */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F6FF),
                        contentColor = PrimaryVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obtener ubicación actual", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 4: Instrucciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Instrucciones para el guardia",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    placeholder = { Text("Ej: Verificar que la puerta esté cerrada con llave. Revisar iluminación del área.", color = TextSecondary, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = PrimaryVariant
                    )
                )
                Text(
                    "Instrucciones que el guardia verá al llegar a este checkpoint", 
                    color = TextSecondary, 
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Yellow Info Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF9C3), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Nfc, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Tag NFC Físico:",
                        color = Color(0xFF854D0E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Una vez creado el checkpoint, deberás asociar el código generado con el tag NFC físico instalado en el lugar",
                        color = Color(0xFF854D0E),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons
            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = isFormValid
            ) {
                Text("Crear Checkpoint", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5E7EB),
                    contentColor = Color(0xFF111827)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* No dismiss on click outside */ },
                title = {
                    Text(text = "Éxito", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Checkpoint creado exitosamente!")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSuccessDialog = false
                        onCreate()
                    }) {
                        Text("Cerrar", color = PrimaryColor, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}
