package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstallationScreen(
    viewModel: AdminInstallationsViewModel, // Conectamos el ViewModel
    onBack: () -> Unit
) {
    // Obtenemos el estado del ViewModel
    val state by viewModel.state

    // Estados locales para el formulario (Sincronizados con tu Postman y BD)
    var name by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var clientName by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var radius by rememberSaveable { mutableStateOf("100") }

    // Validación: name, address, clientName, lat y lon son obligatorios y radio debe ser positivo
    val isFormValid = name.isNotBlank() && 
                     address.isNotBlank() && 
                     clientName.isNotBlank() && 
                     latitude.toDoubleOrNull() != null && 
                     longitude.toDoubleOrNull() != null &&
                     (radius.toDoubleOrNull() ?: -1.0) >= 0.0

    // Estado para el diálogo del mapa
    var showMap by rememberSaveable { mutableStateOf(false) }
    var tempLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Observamos si la creación fue exitosa para volver atrás
    LaunchedEffect(state.isCreateSuccess) {
        if (state.isCreateSuccess) {
            viewModel.resetCreateState()
            onBack()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(), // Añade espacio para el teclado
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nueva Instalación", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Datos según registro oficial", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryVariant)
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de Formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField(label = "Nombre de Instalación", value = name, onValueChange = { name = it }, placeholder = "Ej: Planta Industrial Norte")
                FormField(label = "Dirección", value = address, onValueChange = { address = it }, placeholder = "Ej: Av. Industrial 500")
                FormField(label = "Nombre del Cliente", value = clientName, onValueChange = { clientName = it }, placeholder = "Ej: Manufacturas S.A.")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormField(label = "Latitud", value = latitude, onValueChange = { latitude = it }, placeholder = "Ej: -33.1234", keyboardType = KeyboardType.Decimal)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormField(label = "Longitud", value = longitude, onValueChange = { longitude = it }, placeholder = "Ej: -70.5678", keyboardType = KeyboardType.Decimal)
                    }
                }
                
                // Botón Obtener Coordenadas (Bajo los campos)
                Button(
                    onClick = { showMap = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Mapa", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("📍 Obtener Coordenadas del Mapa", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                
                FormField(label = "Radio de Tolerancia (Metros)", value = radius, onValueChange = { radius = it }, placeholder = "Ej: 100", keyboardType = KeyboardType.Decimal)
                if ((radius.toDoubleOrNull() ?: 0.0) < 0.0) {
                    Text("El radio no puede ser negativo", color = Color.Red, fontSize = 12.sp)
                }
            }

            // Error del Backend si existe
            if (state.error != null) {
                Text(state.error!!, color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            // Botón de Acción
            Button(
                onClick = {
                    viewModel.createInstallation(
                        name = name,
                        address = address,
                        clientName = clientName,
                        latitude = latitude.toDoubleOrNull() ?: 0.0,
                        longitude = longitude.toDoubleOrNull() ?: 0.0,
                        radius = radius.toDoubleOrNull() ?: 100.0
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(8.dp),
                enabled = isFormValid && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Crear Instalación", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancelar")
            }
        }
    }

    // Diálogo del Mapa
    if (showMap) {
        Dialog(
            onDismissRequest = { showMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Pantalla completa
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                val santiago = LatLng(-33.4489, -70.6693)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(santiago, 12f)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            tempLatLng = latLng
                        }
                    ) {
                        tempLatLng?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = "Ubicación Seleccionada"
                            )
                        }
                    }

                    // Panel superior (Instrucciones y cerrar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.9f))
                            .statusBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Toca el mapa para fijar la ubicación",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { showMap = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DangerColor)
                        }
                    }

                    // Botón inferior para confirmar
                    if (tempLatLng != null) {
                        Button(
                            onClick = {
                                latitude = String.format(Locale.US, "%.6f", tempLatLng!!.latitude)
                                longitude = String.format(Locale.US, "%.6f", tempLatLng!!.longitude)
                                showMap = false
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 48.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Confirmar Coordenadas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold) // Texto en Negrita
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            // FORZAR COLORES OSCUROS PARA MÁXIMA VISIBILIDAD
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedLabelColor = PrimaryColor,
                unfocusedLabelColor = TextSecondary,
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = PrimaryColor
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType)
        )
    }
}
