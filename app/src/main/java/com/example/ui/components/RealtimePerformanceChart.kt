package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PerformanceTelemetryPoint(
    val fps: Float,
    val cpuLoad: Float, // 0 to 100%
    val gpuLoad: Float, // 0 to 100%
    val pingMs: Float,  // ms
    val netSpeedKbps: Float // KB/s
)

@Composable
fun RealtimePerformanceChartCard(
    fps: Int,
    cpuLoad: Float,
    gpuLoad: Float,
    pingMs: Int,
    netSpeedKbps: Float,
    history: List<PerformanceTelemetryPoint>,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF22C55E),
    isLiquidGlass: Boolean = false
) {
    var showFps by remember { mutableStateOf(true) }
    var showCpu by remember { mutableStateOf(true) }
    var showGpu by remember { mutableStateOf(true) }
    var showNet by remember { mutableStateOf(true) }

    val fpsColor = Color(0xFF22C55E) // Green
    val cpuColor = Color(0xFF06B6D4) // Cyan
    val gpuColor = Color(0xFFF97316) // Orange
    val netColor = Color(0xFFA855F7) // Purple

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLiquidGlass) Color(0x38121824) else Color(0xFF18181C))
            .border(
                1.dp,
                if (isLiquidGlass) Color.White.copy(alpha = 0.25f) else Color(0xFF2C2C2C),
                RoundedCornerShape(14.dp)
            )
            .padding(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REAL-TIME MONITOR",
                color = Color.LightGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "LIVE TELEMETRY",
                    color = Color(0xFF22C55E),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Compact 2x2 Metric Badges
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TelemetryMetricPill("FPS", "$fps", "fps", fpsColor, showFps) { showFps = !showFps }
                TelemetryMetricPill("CPU", "${cpuLoad.toInt()}%", "load", cpuColor, showCpu) { showCpu = !showCpu }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TelemetryMetricPill("GPU", "${gpuLoad.toInt()}%", "load", gpuColor, showGpu) { showGpu = !showGpu }
                TelemetryMetricPill(
                    "NET",
                    if (netSpeedKbps >= 1024) String.format("%.1fMB/s", netSpeedKbps / 1024f) else String.format("%dKB/s", netSpeedKbps.toInt()),
                    "$pingMs ms",
                    netColor,
                    showNet
                ) { showNet = !showNet }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Compact Line Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF101012))
                .padding(6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw horizontal grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = h * (i / gridLines.toFloat())
                    drawLine(
                        color = Color(0xFF222226),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                if (history.size < 2) return@Canvas

                val stepX = w / (history.size - 1).coerceAtLeast(1)

                fun drawChannelPath(
                    getValue: (PerformanceTelemetryPoint) -> Float,
                    maxVal: Float,
                    color: Color
                ) {
                    val path = Path()
                    val fillPath = Path()

                    history.forEachIndexed { index, point ->
                        val rawVal = getValue(point)
                        val normalized = (rawVal / maxVal).coerceIn(0f, 1f)
                        val x = index * stepX
                        val y = h - (normalized * h * 0.85f + h * 0.08f)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevPoint = history[index - 1]
                            val prevNorm = (getValue(prevPoint) / maxVal).coerceIn(0f, 1f)
                            val prevY = h - (prevNorm * h * 0.85f + h * 0.08f)

                            val midX = (prevX + x) / 2f
                            val midY = (prevY + y) / 2f
                            path.quadraticTo(prevX, prevY, midX, midY)
                            fillPath.quadraticTo(prevX, prevY, midX, midY)
                        }

                        if (index == history.lastIndex) {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                            fillPath.lineTo(x, h)
                            fillPath.close()
                        }
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    val lastPt = history.last()
                    val lastNorm = (getValue(lastPt) / maxVal).coerceIn(0f, 1f)
                    val lastX = (history.size - 1) * stepX
                    val lastY = h - (lastNorm * h * 0.85f + h * 0.08f)

                    drawCircle(
                        color = color,
                        radius = 3.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.5.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }

                if (showFps) drawChannelPath({ it.fps }, 120f, fpsColor)
                if (showCpu) drawChannelPath({ it.cpuLoad }, 100f, cpuColor)
                if (showGpu) drawChannelPath({ it.gpuLoad }, 100f, gpuColor)
                if (showNet) drawChannelPath({ it.netSpeedKbps.coerceAtLeast(10f) }, 500f, netColor)
            }
        }
    }
}

@Composable
private fun RowScope.TelemetryMetricPill(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) accentColor.copy(alpha = 0.16f) else Color(0xFF222226))
            .border(
                1.dp,
                if (isActive) accentColor else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(if (isActive) accentColor else Color.Gray)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    color = if (isActive) Color.White else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = value,
                color = if (isActive) accentColor else Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
