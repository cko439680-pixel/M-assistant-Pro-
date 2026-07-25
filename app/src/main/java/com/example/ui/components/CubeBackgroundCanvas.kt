package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CubeBackgroundCanvas(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF22C55E)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark background
        drawRect(color = Color(0xFF090C0A))

        // Draw isometric cubes with dynamic wireframe tint
        fun drawIsoCube(centerX: Float, centerY: Float, size: Float, alpha: Float) {
            val hSize = size * 0.866f

            // Top face
            val topPath = Path().apply {
                moveTo(centerX, centerY - size)
                lineTo(centerX + hSize, centerY - size / 2)
                lineTo(centerX, centerY)
                lineTo(centerX - hSize, centerY - size / 2)
                close()
            }

            // Left face
            val leftPath = Path().apply {
                moveTo(centerX - hSize, centerY - size / 2)
                lineTo(centerX, centerY)
                lineTo(centerX, centerY + size)
                lineTo(centerX - hSize, centerY + size / 2)
                close()
            }

            // Right face
            val rightPath = Path().apply {
                moveTo(centerX, centerY)
                lineTo(centerX + hSize, centerY - size / 2)
                lineTo(centerX + hSize, centerY + size / 2)
                lineTo(centerX, centerY + size)
                close()
            }

            // Draw faces with subtle tinted fills matched to performance accent color
            val topFill = accentColor.copy(alpha = alpha * 0.16f)
            val leftFill = accentColor.copy(alpha = alpha * 0.08f)
            val rightFill = accentColor.copy(alpha = alpha * 0.12f)

            drawPath(topPath, color = topFill)
            drawPath(leftPath, color = leftFill)
            drawPath(rightPath, color = rightFill)

            // Dynamic accent wireframe edges (Green -> Orange/Amber -> Cyan)
            val strokeColor = accentColor.copy(alpha = alpha * 0.95f)
            val strokeStyle = Stroke(width = 3.5f)

            drawPath(topPath, color = strokeColor, style = strokeStyle)
            drawPath(leftPath, color = strokeColor, style = strokeStyle)
            drawPath(rightPath, color = strokeColor, style = strokeStyle)
        }

        // Multiple overlapping 3D cubes
        val baseSize = w * 0.22f

        drawIsoCube(w * 0.5f, h * 0.38f, baseSize, 0.95f)
        drawIsoCube(w * 0.22f, h * 0.62f, baseSize * 0.9f, 0.85f)
        drawIsoCube(w * 0.78f, h * 0.68f, baseSize * 0.85f, 0.75f)
        drawIsoCube(w * 0.5f, h * 0.85f, baseSize * 0.95f, 0.90f)
    }
}

