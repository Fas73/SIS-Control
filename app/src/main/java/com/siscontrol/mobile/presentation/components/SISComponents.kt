package com.siscontrol.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.PrimaryColor
import com.siscontrol.mobile.presentation.theme.PrimaryVariant
import com.siscontrol.mobile.presentation.theme.SuccessColor

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.siscontrol.mobile.R

@Composable
fun SISTopBar(
    title: String,
    subtitle: String? = null,
    showAdminLogo: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(PrimaryColor, PrimaryVariant)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showAdminLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_degrade_sis_control),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .shadow(4.dp, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SIS", color = PrimaryColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}

@Composable
fun SISCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SISBadge(
    text: String,
    containerColor: Color = SuccessColor.copy(alpha = 0.2f),
    contentColor: Color = SuccessColor
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SISPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PrimaryColor
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
