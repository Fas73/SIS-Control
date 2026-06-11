package com.siscontrol.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

enum class ButtonVariant {
    PRIMARY, DANGER, WARNING, SUCCESS, SECONDARY
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    fullWidth: Boolean = true
) {
    val isPrimary = variant == ButtonVariant.PRIMARY

    val containerColor = when (variant) {
        ButtonVariant.PRIMARY -> Color.Transparent // Usamos el Box con degradado en su lugar
        ButtonVariant.DANGER -> DangerColor
        ButtonVariant.WARNING -> WarningColor
        ButtonVariant.SUCCESS -> SuccessColor
        ButtonVariant.SECONDARY -> Color(0xFFE5E7EB) // gray-200
    }

    val contentColor = when (variant) {
        ButtonVariant.SECONDARY -> TextPrimary
        else -> Color.White
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), // Importante para que el fondo llene el botón
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFFD1D5DB), // gray-300
            disabledContentColor = Color(0xFF9CA3AF) // gray-400
        )
    ) {
        if (isPrimary && enabled) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(PrimaryColor, PrimaryVariant)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
