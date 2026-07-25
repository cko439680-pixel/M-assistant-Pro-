package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PerformanceMode
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PerformanceRadarChart(
    mode: PerformanceMode,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(180.dp)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(center.x, center.y) * 0.65f

            // 5 axes for: Touch control, Image quality, Battery, Heating, Smoothness
            val numAxes = 5
            val angleStep = (2 * Math.PI / numAxes).toFloat()
            val startAngle = (-Math.PI / 2).toFloat() // top

            // Background concentric polygons
            for (level in 1..3) {
                val levelRadius = radius * (level / 3f)
                val bgPath = Path()
                for (i in 0 until numAxes) {
                    val angle = startAngle + i * angleStep
                    val x = center.x + levelRadius * cos(angle)
                    val y = center.y + levelRadius * sin(angle)
                    if (i == 0) bgPath.moveTo(x, y) else bgPath.lineTo(x, y)
                }
                bgPath.close()
                drawPath(bgPath, color = Color(0xFF2E3B32), style = Stroke(width = 2f))
            }

            // Draw axis lines
            for (i in 0 until numAxes) {
                val angle = startAngle + i * angleStep
                val endX = center.x + radius * cos(angle)
                val endY = center.y + radius * sin(angle)
                drawLine(
                    color = Color(0xFF2E3B32),
                    start = center,
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 2f
                )
            }

            // Values according to selected mode: [Touch, Image, Battery, Heating, Smoothness]
            val values = when (mode) {
                PerformanceMode.POWER_SAVING -> floatArrayOf(0.5f, 0.5f, 0.95f, 0.4f, 0.5f)
                PerformanceMode.BALANCED -> floatArrayOf(0.75f, 0.8f, 0.7f, 0.6f, 0.8f)
                PerformanceMode.PRO_GAMER -> floatArrayOf(0.98f, 0.98f, 0.4f, 0.95f, 0.98f)
            }

            val dataPath = Path()
            for (i in 0 until numAxes) {
                val angle = startAngle + i * angleStep
                val r = radius * values[i]
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            val mainColor = when (mode) {
                PerformanceMode.POWER_SAVING -> Color(0xFF22C55E)
                PerformanceMode.BALANCED -> Color(0xFF38BDF8)
                PerformanceMode.PRO_GAMER -> Color(0xFFEF4444)
            }

            drawPath(dataPath, color = mainColor.copy(alpha = 0.35f))
            drawPath(dataPath, color = mainColor, style = Stroke(width = 4f))
        }

        // Labels
        Text(
            text = "Touch control",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "Image quality",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = "Battery",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
        Text(
            text = "Heating ⓘ",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.BottomStart)
        )
        Text(
            text = "Smoothness",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
