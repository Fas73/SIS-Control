package com.siscontrol.mobile.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

@Composable
fun SeverityPieChart(
    high: Int,
    medium: Int,
    low: Int
) {
    val total = high + medium + low
    if (total == 0) return

    val highAngle = (high.toFloat() / total) * 360f
    val mediumAngle = (medium.toFloat() / total) * 360f
    val lowAngle = (low.toFloat() / total) * 360f

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            Canvas(modifier = Modifier.size(100.dp)) {
                drawArc(
                    color = DangerColor,
                    startAngle = -90f,
                    sweepAngle = highAngle,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFFD97706), // Warning color for medium
                    startAngle = -90f + highAngle,
                    sweepAngle = mediumAngle,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = PrimaryColor,
                    startAngle = -90f + highAngle + mediumAngle,
                    sweepAngle = lowAngle,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(total.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Total", fontSize = 10.sp, color = TextSecondary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendItem("Alta", high, DangerColor)
            LegendItem("Media", medium, Color(0xFFD97706))
            LegendItem("Baja", low, PrimaryColor)
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).padding(2.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", fontSize = 12.sp, color = TextSecondary)
        Text(count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
