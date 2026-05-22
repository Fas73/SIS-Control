package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMapScreen(
    paddingValues: PaddingValues,
    viewModel: AdminMapViewModel
) {
    val state by viewModel.state

    // Recargar datos cada vez que se entra o manualmente
    LaunchedEffect(Unit) {
        viewModel.loadMapData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // Fondo gris muy claro
            .padding(paddingValues)
    ) {
        // Top Bar con Degradado y Refresh
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
                Text(
                    "Mapa en Vivo",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.loadMapData() }) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White)
                }
            }
        }
        
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // Contenedor del Mapa
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0F2FE)) 
                )

                // Leyenda
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(120.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Leyenda", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        LegendItem("En ronda", PrimaryColor)
                        LegendItem("Activo", SuccessColor)
                    }
                }

                // Pins Dinámicos
                state.activeGuards.forEach { guard ->
                    // Calculamos una posición visual simulada basada en las coordenadas
                    // Para un mapa real usaríamos Google Maps, pero aquí simulamos la dispersión
                    val xOffset = ((guard.longitude % 0.01) * 2000).dp
                    val yOffset = ((guard.latitude % 0.01) * 2000).dp

                    Column(
                        modifier = Modifier.align(Alignment.Center).offset(x = xOffset, y = yOffset),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(guard.guardName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(guard.installationName, fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                        MapPin(color = if (guard.status == "En Ronda") PrimaryColor else SuccessColor)
                    }
                }

                // Lista inferior de Guardias (Bottom Sheet)
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Guardias en Vivo (${state.activeGuards.size})", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (state.activeGuards.isEmpty()) {
                                Text("No hay guardias activos en este momento.", color = TextSecondary, fontSize = 14.sp)
                            } else {
                                state.activeGuards.forEach { guard ->
                                    GuardMapListItem(guard.guardName, guard.status, if (guard.status == "En Ronda") PrimaryColor else SuccessColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapPin(modifier: Modifier = Modifier, color: Color) {
    Icon(
        imageVector = Icons.Default.LocationOn,
        contentDescription = null,
        tint = color,
        modifier = modifier.size(40.dp)
    )
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GuardMapListItem(name: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(name, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                status, 
                color = statusColor, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
