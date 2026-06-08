package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMapScreen(
    paddingValues: PaddingValues,
    viewModel: AdminMapViewModel
) {
    val state by viewModel.state
    val context = androidx.compose.ui.platform.LocalContext.current

    // Posición inicial (Centro de Santiago de Chile como fallback)
    val santiago = LatLng(-33.4489, -70.6693)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santiago, 12f)
    }

    // Recargar datos y activar audio al entrar
    LaunchedEffect(Unit) {
        viewModel.loadMapData(context)
        viewModel.startAutoRefresh(context)
    }

    // Centrado automático de cámara cuando se cargan guardias por primera vez
    LaunchedEffect(state.activeGuards) {
        if (state.activeGuards.isNotEmpty()) {
            val firstGuard = state.activeGuards.first()
            if (firstGuard.latitude != -33.4489) { // Solo si tiene coordenadas reales
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                        LatLng(firstGuard.latitude, firstGuard.longitude), 
                        15f
                    )
                )
            }
        }
    }

    // Verificación de permisos de ubicación para evitar cierres (SecurityException)
    val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    // Configuración del Mapa
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    val uiSettings by remember { mutableStateOf(MapUiSettings(myLocationButtonEnabled = hasFineLocation || hasCoarseLocation, zoomControlsEnabled = false)) }
    val properties by remember(mapType, hasFineLocation, hasCoarseLocation) { 
        mutableStateOf(MapProperties(
            mapType = mapType, 
            isMyLocationEnabled = hasFineLocation || hasCoarseLocation
        )) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
    ) {
        // Header profesional con degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryVariant)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mapa en Vivo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Monitoreo geográfico de personal", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                IconButton(onClick = { viewModel.loadMapData(context) }) {
                    Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
                }
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            
            // --- GOOGLE MAPS REAL ---
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = properties
            ) {
                // Dibujar marcadores para cada guardia activo
                state.activeGuards.forEach { guard ->
                    val position = LatLng(guard.latitude, guard.longitude)
                    val statusColor = if (guard.status == "En Ronda") 
                        android.graphics.Color.parseColor("#1E3A8A") // PrimaryColor
                        else android.graphics.Color.parseColor("#16A34A") // SuccessColor

                    Marker(
                        state = MarkerState(position = position),
                        icon = com.siscontrol.mobile.core.MapUtils.createCustomMarker(
                            context, 
                            guard.guardName, 
                            statusColor
                        ),
                        title = guard.guardName,
                        snippet = "${guard.status} en ${guard.installationName}"
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            // Selector de tipo de mapa (Botón flotante)
            FloatingActionButton(
                onClick = { 
                    mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL 
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                containerColor = Color.White,
                contentColor = PrimaryColor,
                shape = CircleShape
            ) {
                Icon(if (mapType == MapType.NORMAL) Icons.Default.Layers else Icons.Default.Map, "Tipo de Mapa")
            }

            // Lista inferior de Guardias (Panel deslizable)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Personal en Terreno", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.activeGuards.isEmpty()) {
                            item { Text("No hay actividad detectada.", color = TextSecondary, fontSize = 14.sp) }
                        } else {
                            items(state.activeGuards.size) { index ->
                                val guard = state.activeGuards[index]
                                GuardMapListItem(
                                    name = guard.guardName, 
                                    status = guard.status, 
                                    statusColor = if (guard.status == "En Ronda") PrimaryColor else SuccessColor,
                                    onClick = {
                                        // Centrar cámara en el guardia seleccionado
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                            LatLng(guard.latitude, guard.longitude), 
                                            16f
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Leyenda de estados
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapBadge("EN RONDA", PrimaryColor)
                MapBadge("ACTIVO", SuccessColor)
            }
        }
    }
}

@Composable
fun MapBadge(text: String, color: Color) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
    }
}

@Composable
fun GuardMapListItem(name: String, status: String, statusColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(statusColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase(), color = statusColor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(name, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
            SISBadge(status, containerColor = statusColor.copy(alpha = 0.1f), contentColor = statusColor)
        }
    }
}
