package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@Composable
fun SupervisorHomeScreen(
    paddingValues: PaddingValues,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Dashboard",
            subtitle = "Resumen Operativo SUPERVISOR"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SupervisorKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Mis Guardias",
                        value = "8",
                        subtitle = "3 en ronda",
                        subtitleColor = SuccessColor,
                        icon = Icons.Default.Person,
                        iconColor = PrimaryVariant,
                        iconBg = PrimaryVariant.copy(alpha = 0.1f)
                    )
                    SupervisorKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Rondas Hoy",
                        value = "12",
                        subtitle = "3 activas",
                        icon = Icons.Default.LocationOn,
                        iconColor = PrimaryColor,
                        iconBg = PrimaryColor.copy(alpha = 0.1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SupervisorKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Completadas",
                        value = "9",
                        subtitle = "100% éxito",
                        subtitleColor = SuccessColor,
                        icon = Icons.Default.CheckCircle,
                        iconColor = SuccessColor,
                        iconBg = SuccessColor.copy(alpha = 0.1f)
                    )
                    SupervisorKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Alertas",
                        value = "2",
                        subtitle = "Recientes",
                        subtitleColor = WarningColor,
                        icon = Icons.Default.Warning,
                        iconColor = WarningColor,
                        iconBg = WarningColor.copy(alpha = 0.1f)
                    )
                }
            }

            item {
                Text(
                    "Guardias en Ronda", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = TextPrimary, 
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                SupervisorGuardCard(
                    guardName = "Juan Pérez",
                    location = "Plaza Centro",
                    status = "En Ronda",
                    checkpoints = "4/8",
                    time = "15 min",
                    progress = 0.5f,
                    onNavigate = onNavigate
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                SupervisorGuardCard(
                    guardName = "María González",
                    location = "Bodega Norte",
                    status = "En Ronda",
                    checkpoints = "2/6",
                    time = "8 min",
                    progress = 0.33f,
                    onNavigate = onNavigate
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alertas Recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TextButton(onClick = { onNavigate("ALERTS") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Ver todas", color = PrimaryColor, fontSize = 14.sp)
                    }
                }
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningColor.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Checkpoint no escaneado", color = Color(0xFF92400E), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Hace 5 minutos - Bodega Norte", color = Color(0xFFB45309), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupervisorKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color = TextSecondary,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = subtitleColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SupervisorGuardCard(
    guardName: String,
    location: String,
    status: String,
    checkpoints: String,
    time: String,
    progress: Float,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(guardName.first().toString(), color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(guardName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(location, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
                SISBadge(status, containerColor = PrimaryVariant.copy(alpha = 0.15f), contentColor = PrimaryColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress }, 
                modifier = Modifier.fillMaxWidth().height(6.dp), 
                color = PrimaryVariant, 
                trackColor = Color(0xFFF3F4F6)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Checkpoints: $checkpoints", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
