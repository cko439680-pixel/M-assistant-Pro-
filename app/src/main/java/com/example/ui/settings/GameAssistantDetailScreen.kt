package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAssistantDetailScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(repository.gameAssistantMasterEnabled.value) }
    val selectedPanelStyle by repository.selectedPanelStyle.collectAsState()
    val activeAccentColor = Color(0xFF22C55E)

    // Animated swipe gesture demo
    val swipeOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        swipeOffset.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Game Assistant Side Panel",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated graphic frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Phone screen graphic
                    val phoneW = w * 0.7f
                    val phoneH = h * 0.75f
                    val left = (w - phoneW) / 2f
                    val top = (h - phoneH) / 2f

                    drawRoundRect(
                        color = Color(0xFF333333),
                        topLeft = Offset(left, top),
                        size = Size(phoneW, phoneH),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )

                    // Side handle
                    val handleX = left + 4.dp.toPx()
                    val handleY = top + phoneH * 0.3f
                    drawRoundRect(
                        color = Color(0xFF22C55E),
                        topLeft = Offset(handleX, handleY),
                        size = Size(6.dp.toPx(), 32.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Moving blue swipe dot
                    val dotProgress = swipeOffset.value
                    val dotX = handleX + (phoneW * 0.35f) * dotProgress
                    val dotY = handleY + 16.dp.toPx()

                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 10.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "When Game Assistant is on, swipe inwards from the side of the screen while gaming to open Game Assistant and use tools or claim rewards. When it's off, these features won't be available.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Enable Game Side Panel",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isEnabled) "Floating Game Assistant active" else "Game Assistant overlay disabled",
                        color = if (isEnabled) Color(0xFF22C55E) else Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        repository.gameAssistantMasterEnabled.value = checked
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            Toast.makeText(context, "Grant 'Display over other apps' for floating side bar!", Toast.LENGTH_LONG).show()
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF22C55E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isLiquidGlass by repository.isLiquidGlassMode.collectAsState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Enable Liquid Glass Mode",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLiquidGlass) "Real-time frosted glass shader active" else "Classic opaque panel background",
                        color = if (isLiquidGlass) Color(0xFF22C55E) else Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isLiquidGlass,
                    onCheckedChange = { checked ->
                        repository.updateLiquidGlassMode(context, checked)
                        Toast.makeText(
                            context,
                            if (checked) "✨ Liquid Glass Mode Enabled" else "🌑 Liquid Glass Mode Disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF22C55E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // SECTION: CHOOSE PANEL STYLE (STRICTLY INSIDE SIDE PANEL SUBMENU)
            // -------------------------------------------------------------
            Text(
                text = "CHOOSE PANEL STYLE",
                color = activeAccentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Option A: Hyper Telemetry Panel
                val isHyperSelected = selectedPanelStyle == "Hyper Telemetry" || selectedPanelStyle == "Hyper Telemetry Panel" || selectedPanelStyle == "Classic / Legacy Panel"
                PanelStyleOptionCardDetail(
                    title = "\"Hyper Telemetry\" Panel",
                    subtitle = "Classic vertical sidebar with live performance metrics, FPS & tools rail across all games",
                    isSelected = isHyperSelected,
                    accentColor = activeAccentColor,
                    isPreviewClassic = true,
                    onSelect = {
                        repository.updatePanelStyle(context, "Hyper Telemetry")
                        Toast.makeText(context, "⚡ Overlay layout set to Hyper Telemetry Panel", Toast.LENGTH_SHORT).show()
                    }
                )

                // Option B: M Assistant Panel
                val isMAssistantSelected = selectedPanelStyle == "M Assistant" || selectedPanelStyle == "M Assistant Panel" || selectedPanelStyle == "Modern / Unified Landscape Panel"
                PanelStyleOptionCardDetail(
                    title = "\"M Assistant\" Panel",
                    subtitle = "Modern landscape HUD panel with dual stat cards & quick sliders across all games",
                    isSelected = isMAssistantSelected,
                    accentColor = activeAccentColor,
                    isPreviewClassic = false,
                    onSelect = {
                        repository.updatePanelStyle(context, "M Assistant")
                        Toast.makeText(context, "🚀 Overlay layout set to M Assistant Panel", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun PanelStyleOptionCardDetail(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    isPreviewClassic: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1A261D) else Color(0xFF1C1C1C)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Thumbnail Graphic Box
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF121212))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    if (isPreviewClassic) {
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                            size = Size(14.dp.toPx(), h - 8.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                        val startX = 24.dp.toPx()
                        for (row in 0..1) {
                            for (col in 0..1) {
                                drawRoundRect(
                                    color = Color(0xFF2A2A2A),
                                    topLeft = Offset(startX + col * 16.dp.toPx(), 8.dp.toPx() + row * 16.dp.toPx()),
                                    size = Size(12.dp.toPx(), 12.dp.toPx()),
                                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                )
                            }
                        }
                    } else {
                        drawRoundRect(
                            color = Color(0xFF2A2A2A),
                            topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
                            size = Size(32.dp.toPx(), 18.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF2A2A2A),
                            topLeft = Offset(42.dp.toPx(), 6.dp.toPx()),
                            size = Size(32.dp.toPx(), 18.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(6.dp.toPx(), 32.dp.toPx()),
                            size = Size(68.dp.toPx(), 6.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = Color.Gray
                )
            )
        }
    }
}
