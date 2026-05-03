package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMapScreen(
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2FE)) // Light blue map background
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Mapa en Vivo",
            subtitle = "",
            showAdminLogo = true
        )
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Simulated grid for map
            // ... omitting grid drawing for simplicity, rely on background color

            // Legend
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Leyenda", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem("En ronda", PrimaryColor)
                    LegendItem("Activo", SuccessColor)
                    LegendItem("Inactivo", TextSecondary)
                }
            }

            // Map Pins (simulated)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(x = (-30).dp, y = (-50).dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(36.dp))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(x = 50.dp, y = 30.dp)) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("María González", fontSize = 12.sp, color = TextPrimary)
                            SISBadge("Activo", containerColor = SuccessColor.copy(alpha = 0.1f), contentColor = SuccessColor)
                        }
                    }
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(36.dp))
                }
            }

            // Bottom sheet list
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .heightIn(max = 240.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Guardias en Vivo (3)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            GuardMapListItem("Juan Pérez", "En Ronda", PrimaryColor)
                        }
                        item {
                            GuardMapListItem("María González", "Activo", SuccessColor)
                        }
                        item {
                            GuardMapListItem("Pedro Sánchez", "En Ronda", PrimaryColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun GuardMapListItem(name: String, status: String, statusColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, fontSize = 14.sp, color = TextPrimary)
        }
        SISBadge(status, containerColor = statusColor.copy(alpha = 0.1f), contentColor = statusColor)
    }
}
