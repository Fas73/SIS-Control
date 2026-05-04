package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
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
            title = "Dashboard ADMIN",
            subtitle = "Bienvenido, Carlos Martínez",
            showAdminLogo = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Guardias",
                        value = "24",
                        subtitle = "8 en ronda",
                        subtitleColor = SuccessColor,
                        icon = Icons.Default.People,
                        iconColor = PrimaryVariant,
                        iconBg = Color.Transparent
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Rondas Hoy",
                        value = "47",
                        subtitle = "3 en progreso",
                        subtitleColor = TextSecondary,
                        icon = Icons.Default.Timeline,
                        iconColor = PrimaryVariant,
                        iconBg = Color.Transparent
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Instalaciones",
                        value = "12",
                        subtitle = "Todas activas",
                        subtitleColor = TextSecondary,
                        icon = Icons.Default.LocationOn,
                        iconColor = PrimaryVariant,
                        iconBg = Color.Transparent
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Alertas",
                        value = "3",
                        subtitle = "1 sin atender",
                        subtitleColor = DangerColor,
                        icon = Icons.Default.Warning,
                        iconColor = DangerColor,
                        iconBg = Color.Transparent
                    )
                }
            }

            item {
                Text(
                    "Accesos Rápidos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Mapa en Vivo",
                        icon = Icons.Default.LocationOn,
                        containerColor = PrimaryColor.copy(alpha = 0.05f),
                        contentColor = PrimaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("MAP") }
                    )
                    QuickAccessButton(
                        title = "Alertas",
                        icon = Icons.Default.Warning,
                        containerColor = DangerColor.copy(alpha = 0.05f),
                        contentColor = DangerColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("ALERTS") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Gestión",
                        icon = Icons.Default.Settings,
                        containerColor = SuccessColor.copy(alpha = 0.05f),
                        contentColor = SuccessColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("MANAGEMENT") }
                    )
                    QuickAccessButton(
                        title = "Rondas Prueba",
                        icon = Icons.Default.PlayCircle,
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.05f),
                        contentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                }
            }

            item {
                Text(
                    "Rondas Activas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ActiveRoundCard(
                    guardName = "Juan Pérez",
                    location = "Plaza Centro",
                    progress = 0.6f,
                    progressText = "60% completado",
                    status = "En Ronda"
                )
            }
        }
    }
}

@Composable
fun KpiCard(
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = subtitleColor)
        }
    }
}

@Composable
fun ActiveRoundCard(
    guardName: String,
    location: String,
    progress: Float,
    progressText: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(guardName, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                SISBadge(status, containerColor = PrimaryColor.copy(alpha = 0.1f), contentColor = PrimaryColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = PrimaryColor,
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(progressText, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun QuickAccessButton(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
