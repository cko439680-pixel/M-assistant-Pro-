package com.example.ui.settings

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WallpaperBackground

@Composable
fun WallpaperEngineDialog(
    currentWallpaper: String,
    accentColor: Color,
    onWallpaperSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Launcher for Gallery Image Picker (Option A)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val wallpaperSpec = "gallery:${it.toString()}"
            onWallpaperSelected(wallpaperSpec)
            Toast.makeText(context, "✨ Gallery Wallpaper Applied!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    // Launcher for Gallery Video Picker (Option C)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val wallpaperSpec = "video:${it.toString()}"
            onWallpaperSelected(wallpaperSpec)
            Toast.makeText(context, "🎬 Custom Video Loop Wallpaper Applied!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141822),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wallpaper Engine Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Preview Thumbnail Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    WallpaperBackground(
                        wallpaperSpec = currentWallpaper,
                        accentColor = accentColor
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Active: ${formatWallpaperName(currentWallpaper)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Source Tabs: 0: Gallery Photo, 1: Live Animated, 2: Video Loop
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E2430),
                    contentColor = accentColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = accentColor,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Live", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Video", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    // Option A: Gallery Image Wallpaper
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick Gallery Photo", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Text("Presets & Dynamic Themes:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                            listOf("Cyberpunk Neon", "Gold Gamer Aura", "Dark Obsidian Matrix", "Cosmic Starlight").forEach { wp ->
                                WallpaperPresetItem(
                                    name = wp,
                                    isSelected = currentWallpaper == wp,
                                    accentColor = accentColor,
                                    onClick = {
                                        onWallpaperSelected(wp)
                                        Toast.makeText(context, "Wallpaper set to $wp", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    // Option B: Live Wallpaper Engine
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "System Live Wallpaper Picker unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, accentColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = accentColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("System Live Wallpaper Chooser", color = Color.White, fontSize = 12.sp)
                            }

                            Text("Interactive Animated Engines:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                            listOf(
                                "live:cyber_matrix" to "Matrix Rain Particle Engine",
                                "live:neon_pulse" to "Neon Grid Pulse Engine",
                                "live:aurora_glow" to "Aurora Energy Field Engine",
                                "live:hyperspace" to "Hyperspace Starfield Engine"
                            ).forEach { (spec, label) ->
                                WallpaperPresetItem(
                                    name = label,
                                    isSelected = currentWallpaper == spec,
                                    accentColor = accentColor,
                                    onClick = {
                                        onWallpaperSelected(spec)
                                        Toast.makeText(context, "✨ Live Engine Activated: $label", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    // Option C: Video Loop Wallpaper Engine
                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick Video Loop from Storage", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Text("Preset Video Loops:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                            listOf(
                                "video:preset_cyber_loop" to "Futuristic Cyber Loop (Muted)"
                            ).forEach { (spec, label) ->
                                WallpaperPresetItem(
                                    name = label,
                                    isSelected = currentWallpaper == spec,
                                    accentColor = accentColor,
                                    onClick = {
                                        onWallpaperSelected(spec)
                                        Toast.makeText(context, "🎬 Video Loop Applied!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = accentColor, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun WallpaperPresetItem(
    name: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color(0xFF1E2430))
            .border(1.dp, if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else Color.Gray)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        }
    }
}

private fun formatWallpaperName(spec: String): String {
    return when {
        spec.startsWith("gallery:") -> "Custom Gallery Photo"
        spec.startsWith("video:") -> "Custom Video Loop"
        spec.startsWith("live:") -> spec.removePrefix("live:").replace("_", " ").uppercase()
        else -> spec
    }
}
