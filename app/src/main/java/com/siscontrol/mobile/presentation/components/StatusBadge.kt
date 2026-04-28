package com.siscontrol.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

enum class BadgeStatus {
    ACTIVE, INACTIVE, EN_RONDA, ALERTA, COMPLETADA
}

@Composable
fun StatusBadge(
    status: BadgeStatus,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, text) = when (status) {
        BadgeStatus.ACTIVE -> Triple(Color(0xFFD1FAE5), SuccessColor, "ACTIVO")
        BadgeStatus.INACTIVE -> Triple(Color(0xFFF3F4F6), TextSecondary, "INACTIVO")
        BadgeStatus.EN_RONDA -> Triple(Color(0xFFDBEAFE), PrimaryVariant, "EN RONDA")
        BadgeStatus.ALERTA -> Triple(Color(0xFFFEE2E2), DangerColor, "ALERTA")
        BadgeStatus.COMPLETADA -> Triple(Color(0xFFD1FAE5), SuccessColor, "COMPLETADA")
    }

    Box(
        modifier = modifier
            .background(color = containerColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
