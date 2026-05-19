package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase

@Composable
fun GuardHomeScreen(
    paddingValues: PaddingValues,
    onNavigate: (String) -> Unit,
    userName: String = "Guardia",
    userStatus: Int = 1 // 1: Activo, 0: Inactivo
) {
    val formattedName = userName.toTitleCase()
    val isActive = userStatus == 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        // Header profile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.DarkGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(formattedName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        SISBadge(
                            if (isActive) "Activo" else "Inactivo", 
                            containerColor = if (isActive) Color.White else Color(0xFFFEE2E2), 
                            contentColor = if (isActive) SuccessColor else DangerColor
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha=0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                }
            }
        }
        
        // El nombre de la instalación se oculta hasta que se inicia jornada en un punto específico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(14.dp))
                 Spacer(modifier = Modifier.width(4.dp))
                 Text("Seleccione instalación para comenzar", color = Color.White, fontSize = 14.sp)
             }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // "Marcar Entrada / Iniciar Turno" Card (Cambio solicitado por el jefe)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) PrimaryColor else Color(0xFF94A3B8)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Iniciar Jornada / Turno", 
                            color = Color.White, 
                            fontSize = 22.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isActive) "Marca tu asistencia e inicia tus servicios" 
                                   else "Tu cuenta está inactiva. Contacta a soporte.", 
                            color = Color.White.copy(alpha = 0.8f), 
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { onNavigate("START_ROUND") },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) SuccessColor else Color.Gray
                            ),
                            enabled = isActive,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Marcar Entrada / Iniciar Turno", 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Resumen de Hoy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumen de Hoy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryItem(icon = Icons.Default.Schedule, title = "0", subtitle = "Rondas", color = PrimaryColor, bgColor = PrimaryColor.copy(alpha = 0.1f))
                            SummaryItem(icon = Icons.Default.CheckCircle, title = "0", subtitle = "Checkpoints", color = SuccessColor, bgColor = SuccessColor.copy(alpha = 0.1f))
                            SummaryItem(icon = Icons.Default.LocationOn, title = "0.0", subtitle = "km", color = Color(0xFF8B5CF6), bgColor = Color(0xFF8B5CF6).copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, color: Color, bgColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        Text(subtitle, fontSize = 12.sp, color = TextSecondary)
    }
}
