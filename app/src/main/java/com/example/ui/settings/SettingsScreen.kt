package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.admob.AdMobBannerView
import com.example.ui.components.LiquidGlassCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.PerformanceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: GameRepository,
    onNavigateToDetail: () -> Unit,
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToIconTheme: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showInfoBanner by remember { mutableStateOf(true) }

    var launchAnim by remember { mutableStateOf(repository.launchAnimationEnabled.value) }
    var gameAlbum by remember { mutableStateOf(repository.gameAlbumEnabled.value) }
    var autoRes by remember { mutableStateOf(repository.autoAdjustResolution.value) }

    val selectedPanelStyle by repository.selectedPanelStyle.collectAsState()
    val isLiquidGlass by repository.isLiquidGlassMode.collectAsState()
    val blurIntensity by repository.blurIntensity.collectAsState()
    val uiBrightness by repository.uiBrightness.collectAsState()
    val liquidGlassOpacity by repository.liquidGlassOpacity.collectAsState()
    val liquidGlassSpecular by repository.liquidGlassSpecular.collectAsState()
    val activeAccentHex by repository.accentColorHex.collectAsState()
    val activeWallpaper by repository.customWallpaper.collectAsState()
    val currentMode by repository.currentPerformanceMode.collectAsState()

    var isLandscapeOrientation by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showPerfDialog by remember { mutableStateOf(false) }
    var showDevProfileDialog by remember { mutableStateOf(false) }
    var showUserAgreementDialog by remember { mutableStateOf(false) }
    var showPrivacyNoticeDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    val activeAccentColor = remember(activeAccentHex) {
        try { Color(android.graphics.Color.parseColor(activeAccentHex)) }
        catch (e: Exception) { Color(0xFF22C55E) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text("More Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Black,
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = activeAccentColor,
                        height = 2.dp
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        "Settings",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        "About Game Assistant",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            )
        }

        if (selectedTabIndex == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Info Banner
                if (showInfoBanner) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2E20)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(activeAccentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Configure your Game Assistant overlay, panel styles, performance modes, theme accents, and system permissions here.",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(activeAccentColor)
                                        .clickable { showInfoBanner = false }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Got it", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // GAME ASSISTANT & GAME SETTINGS
                // -------------------------------------------------------------
                Text(
                    text = "GAME ASSISTANT & GAME SETTINGS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDetail() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Game Assistant Side Panel",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Enable floating gaming overlay & gestures",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Detail",
                        tint = Color.Gray
                    )
                }

                SettingToggleRow(
                    title = "Launch animation",
                    subtitle = "Show splash animation when launching Game Assistant.",
                    checked = launchAnim,
                    onCheckedChange = {
                        launchAnim = it
                        repository.launchAnimationEnabled.value = it
                    },
                    accentColor = activeAccentColor
                )

                SettingToggleRow(
                    title = "Game album",
                    subtitle = "Save game screenshots and screen recordings to album.",
                    checked = gameAlbum,
                    onCheckedChange = {
                        gameAlbum = it
                        repository.gameAlbumEnabled.value = it
                    },
                    accentColor = activeAccentColor
                )

                SettingToggleRow(
                    title = "Automatically adjust resolution",
                    subtitle = "Adjust resolution based on device temperature & battery.",
                    checked = autoRes,
                    onCheckedChange = {
                        autoRes = it
                        repository.autoAdjustResolution.value = it
                    },
                    accentColor = activeAccentColor
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // -------------------------------------------------------------
                // SECTION 3: RELOCATED ADVANCED OPTIONS
                // -------------------------------------------------------------
                Text(
                    text = "ADVANCED CONTROLS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                // 1. App Permissions
                ClickableSettingRow(
                    icon = Icons.Default.Security,
                    title = "App Permissions",
                    subtitle = "Manage System Draw, Battery Saver & Write Settings permissions",
                    onClick = onNavigateToPermissions
                )

                // 2. Screen Orientation Toggle
                SettingToggleRow(
                    title = "Screen Orientation Lock",
                    subtitle = if (isLandscapeOrientation) "Landscape Gaming Mode Active" else "Portrait System Mode Active",
                    checked = isLandscapeOrientation,
                    onCheckedChange = { checked ->
                        isLandscapeOrientation = checked
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            activity.requestedOrientation = if (checked) {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                        }
                        Toast.makeText(
                            context,
                            if (checked) "🔄 Screen Locked to Landscape" else "📱 Screen Reset to Portrait",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    accentColor = activeAccentColor
                )

                // 3. Performance Settings / Ultra FPS
                ClickableSettingRow(
                    icon = Icons.Default.Speed,
                    title = "Performance Settings / Ultra FPS",
                    subtitle = "Current: ${currentMode.name.replace("_", " ")}",
                    onClick = { showPerfDialog = true }
                )

                // 4. Developer Profile
                ClickableSettingRow(
                    icon = Icons.Default.Person,
                    title = "Developer Profile",
                    subtitle = "Chetan Koli (@starking_1m)",
                    onClick = { showDevProfileDialog = true }
                )

                // 5. Custom Wallpaper Switcher
                ClickableSettingRow(
                    icon = Icons.Default.Wallpaper,
                    title = "Custom Wallpaper",
                    subtitle = "Current: $activeWallpaper",
                    onClick = { showWallpaperDialog = true }
                )

                // Dedicated App Icon & Theme Page
                ClickableSettingRow(
                    icon = Icons.Default.Palette,
                    title = "Change App Icon & Theme",
                    subtitle = "Customize home screen launcher icon variant & system palette",
                    onClick = onNavigateToIconTheme
                )

                // 6. Theme Accent Color Switcher
                ClickableSettingRow(
                    icon = Icons.Default.Palette,
                    title = "Theme Accent Color",
                    subtitle = when (activeAccentHex) {
                        "#EF4444" -> "Neon Red"
                        "#F97316" -> "Cyberpunk Orange"
                        "#EAB308" -> "Vibrant Yellow"
                        "#06B6D4" -> "Sleek Cyan"
                        "#A855F7" -> "Royal Purple"
                        else -> "Electric Green (Default)"
                    },
                    onClick = { showAccentDialog = true }
                )

                // 7. Liquid Glass Mode Switcher
                SettingToggleRow(
                    title = "Liquid Glass Mode",
                    subtitle = if (isLiquidGlass) "Real-time Frosted Glass Shader ON" else "Classic Opaque Glass Background",
                    checked = isLiquidGlass,
                    onCheckedChange = { checked ->
                        repository.updateLiquidGlassMode(context, checked)
                        Toast.makeText(
                            context,
                            if (checked) "✨ Liquid Glass Mode Activated" else "🌑 Classic Dark Mode Active",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    accentColor = activeAccentColor
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "LIQUID GLASS & DISPLAY CUSTOMIZATION",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                // 1. Blur Intensity Slider
                LiquidGlassCard(
                    blurIntensity = blurIntensity,
                    liquidGlassOpacity = liquidGlassOpacity,
                    liquidGlassSpecular = liquidGlassSpecular,
                    glowingBorder = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Blur Intensity", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Strength / radius of GPU Gaussian blur", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text("${(blurIntensity * 100).toInt()}%", color = activeAccentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Slider(
                            value = blurIntensity,
                            onValueChange = { repository.updateBlurIntensity(context, it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeAccentColor,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = Color(0xFF333333)
                            )
                        )
                    }
                }

                // 2. Brightness Slider
                LiquidGlassCard(
                    blurIntensity = blurIntensity,
                    liquidGlassOpacity = liquidGlassOpacity,
                    liquidGlassSpecular = liquidGlassSpecular,
                    glowingBorder = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Display Brightness & Tint", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Luminance & dark/light tint of UI panels", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text("${(uiBrightness * 100).toInt()}%", color = activeAccentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Slider(
                            value = uiBrightness,
                            onValueChange = { repository.updateUiBrightness(context, it) },
                            valueRange = 0.2f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeAccentColor,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = Color(0xFF333333)
                            )
                        )
                    }
                }

                // 3. Liquid Glass Customization Slider
                LiquidGlassCard(
                    blurIntensity = blurIntensity,
                    liquidGlassOpacity = liquidGlassOpacity,
                    liquidGlassSpecular = liquidGlassSpecular,
                    glowingBorder = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Liquid Glass Fine-Tuning", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Frosted glass opacity and specular border intensity", color = Color.Gray, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Frosted Glass Opacity", color = Color.LightGray, fontSize = 13.sp)
                            Text("${(liquidGlassOpacity * 100).toInt()}%", color = activeAccentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Slider(
                            value = liquidGlassOpacity,
                            onValueChange = { repository.updateLiquidGlassOpacity(context, it) },
                            valueRange = 0.1f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeAccentColor,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = Color(0xFF333333)
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Specular Rim Highlight", color = Color.LightGray, fontSize = 13.sp)
                            Text("${(liquidGlassSpecular * 100).toInt()}%", color = activeAccentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Slider(
                            value = liquidGlassSpecular,
                            onValueChange = { repository.updateLiquidGlassSpecular(context, it) },
                            valueRange = 0.1f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeAccentColor,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = Color(0xFF333333)
                            )
                        )
                    }
                }
            }
        } else {
            // ABOUT TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Developer & System Info", color = activeAccentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Created by Chetan Koli (@starking_1m)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Game Assistant Engine v10.35.8", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccentColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("WRITE_SETTINGS: Granted", color = activeAccentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("SYSTEM_ALERT_WINDOW: Active", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                AboutRowItem(title = "Version", value = "10.35.8", onClick = {
                    Toast.makeText(context, "M Assistant Pro v10.35.8 (Build 103508)", Toast.LENGTH_SHORT).show()
                })
                AboutRowItem(title = "Developer", value = "Chetan Koli (@starking_1m)", onClick = {
                    showDevProfileDialog = true
                })
                AboutRowItem(title = "User Agreement", hasArrow = true, onClick = {
                    showUserAgreementDialog = true
                })
                AboutRowItem(title = "Privacy Notice", hasArrow = true, onClick = {
                    showPrivacyNoticeDialog = true
                })
                AboutRowItem(title = "Help & support", hasArrow = true, onClick = {
                    showHelpSupportDialog = true
                })
                AboutRowItem(title = "Open Source Software Licence", hasArrow = true, onClick = {
                    showLicensesDialog = true
                })
            }
        }
    }

    // -------------------------------------------------------------------------
    // DIALOGS FOR PERFORMANCE, WALLPAPER, ACCENT & DEV PROFILE
    // -------------------------------------------------------------------------

    // Performance Settings Dialog
    if (showPerfDialog) {
        AlertDialog(
            onDismissRequest = { showPerfDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Performance Mode / Ultra FPS", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerformanceOptionRow("Pro Gamer (Max Performance)", currentMode == PerformanceMode.PRO_GAMER) {
                        repository.currentPerformanceMode.value = PerformanceMode.PRO_GAMER
                        showPerfDialog = false
                        Toast.makeText(context, "🚀 Pro Gamer Ultra FPS Mode Activated!", Toast.LENGTH_SHORT).show()
                    }
                    PerformanceOptionRow("Balanced (Default)", currentMode == PerformanceMode.BALANCED) {
                        repository.currentPerformanceMode.value = PerformanceMode.BALANCED
                        showPerfDialog = false
                        Toast.makeText(context, "⚖️ Balanced Performance Mode Activated", Toast.LENGTH_SHORT).show()
                    }
                    PerformanceOptionRow("Power Saving", currentMode == PerformanceMode.POWER_SAVING) {
                        repository.currentPerformanceMode.value = PerformanceMode.POWER_SAVING
                        showPerfDialog = false
                        Toast.makeText(context, "🔋 Power Saving Mode Activated", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPerfDialog = false }) {
                    Text("Close", color = activeAccentColor)
                }
            }
        )
    }

    // Custom Wallpaper Engine Dialog
    if (showWallpaperDialog) {
        WallpaperEngineDialog(
            currentWallpaper = activeWallpaper,
            accentColor = activeAccentColor,
            onWallpaperSelected = { wpSpec ->
                repository.updateWallpaper(context, wpSpec)
            },
            onDismiss = { showWallpaperDialog = false }
        )
    }

    // Theme Accent Color Dialog
    if (showAccentDialog) {
        val colors = listOf(
            "#22C55E" to "Electric Green (Default)",
            "#EF4444" to "Neon Red",
            "#F97316" to "Cyberpunk Orange",
            "#EAB308" to "Vibrant Yellow",
            "#06B6D4" to "Sleek Cyan",
            "#A855F7" to "Royal Purple"
        )
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Select Theme Accent Color", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { (hex, label) ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeAccentHex == hex) c.copy(alpha = 0.2f) else Color(0xFF2A2A2A))
                                .clickable {
                                    repository.accentColorHex.value = hex
                                    showAccentDialog = false
                                    Toast.makeText(context, "Theme Accent set to $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(c)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, color = Color.White, fontWeight = if (activeAccentHex == hex) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Developer Profile Dialog
    if (showDevProfileDialog) {
        AlertDialog(
            onDismissRequest = { showDevProfileDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Developer Profile", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(activeAccentColor.copy(alpha = 0.2f))
                            .border(2.dp, activeAccentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CK", color = activeAccentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Chetan Koli", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("@starking_1m", color = Color(0xFF38BDF8), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Lead Android Engineer & Creator of Game Assistant Engine.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevProfileDialog = false }) {
                    Text("Close", color = activeAccentColor)
                }
            }
        )
    }

    // User Agreement Dialog
    if (showUserAgreementDialog) {
        AlertDialog(
            onDismissRequest = { showUserAgreementDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("User Agreement & Terms", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "1. License & Scope\nM Assistant Pro grants you a personal, non-transferable license to use gaming overlay telemetry, crosshair tools, and performance controls on your device.\n\n" +
                        "2. Overlay Permissions\nThe app utilizes System Overlay (SYSTEM_ALERT_WINDOW) exclusively to display floating gaming HUDs and quick settings. No user interaction or screen content outside the app is recorded or intercepted.\n\n" +
                        "3. Fair Play Compliance\nM Assistant Pro is a utility helper and does not modify game memory, inject code into third-party binaries, or bypass anti-cheat mechanisms.\n\n" +
                        "4. Service Availability\nThe software is provided 'as is' without warranties. Updates and performance profiles may be updated periodically.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    launchWebUrl(context, "https://github.com/starking-1m/M-Assistant-Pro/blob/main/TERMS.md")
                }) {
                    Text("Open Online Terms", color = Color(0xFF38BDF8))
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserAgreementDialog = false }) {
                    Text("I Agree", color = activeAccentColor)
                }
            }
        )
    }

    // Privacy Notice Dialog
    if (showPrivacyNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyNoticeDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Privacy Notice & GDPR", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "M Assistant Pro strictly respects your privacy:\n\n" +
                        "• Local Data Only: Custom wallpapers, blur preferences, liquid glass settings, and accent colors are stored strictly on your device.\n\n" +
                        "• No Data Harvesting: We do not collect, sell, or transmit personal data, contacts, location, or telemetry to external servers.\n\n" +
                        "• Advertising & Consent: Google AdMob and User Messaging Platform (UMP) SDKs manage regional privacy choices (GDPR/CCPA) and serve advertisements in compliance with Google Play Developer Policies.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    launchWebUrl(context, "https://github.com/starking-1m/M-Assistant-Pro/blob/main/PRIVACY_POLICY.md")
                }) {
                    Text("Open Privacy Link", color = Color(0xFF38BDF8))
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyNoticeDialog = false }) {
                    Text("Close", color = activeAccentColor)
                }
            }
        )
    }

    // Help & Support Dialog
    if (showHelpSupportDialog) {
        AlertDialog(
            onDismissRequest = { showHelpSupportDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Help & Support", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Frequently Asked Questions:\n\n" +
                        "Q: Floating sidebar is not showing?\n" +
                        "A: Ensure 'Display over other apps' (SYSTEM_ALERT_WINDOW) permission is granted in device settings.\n\n" +
                        "Q: How to adjust liquid glass / blur?\n" +
                        "A: Go to More Settings > Liquid Glass UI & Background Engine to customize blur, specular highlights, and opacity.\n\n" +
                        "Support Contact: Starkingdev9@gmail.com",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:Starkingdev9@gmail.com"))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "M Assistant Pro Support Request")
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Support Email: Starkingdev9@gmail.com", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Contact Email", color = Color(0xFF38BDF8))
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpSupportDialog = false }) {
                    Text("Close", color = activeAccentColor)
                }
            }
        )
    }

    // Open Source Software Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Open Source Software Licenses", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "• Android Jetpack Compose - Apache License 2.0\n" +
                        "• Google Play Services & AdMob SDK - Google Software License\n" +
                        "• Kotlin Coroutines & Serialization - Apache License 2.0\n" +
                        "• Coil Image Loader - Apache License 2.0\n" +
                        "• Material Design 3 Components - Apache License 2.0",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", color = activeAccentColor)
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLES FOR PANEL STYLE CARD & SETTINGS ROWS
// -----------------------------------------------------------------------------

@Composable
private fun PanelStyleOptionCard(
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
                        // CLASSIC PANEL PREVIEW: Left side rail + tool tiles
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                            size = Size(14.dp.toPx(), h - 8.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                        // Grid tiles
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
                        // MODERN UNIFIED PANEL PREVIEW: Dual stat cards + slider bars
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
                        // Accent line representing top slider bar
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

@Composable
private fun ClickableSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
private fun PerformanceOptionRow(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF2A2A2A))
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E))
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color = Color(0xFF22C55E)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}

@Composable
private fun AboutRowItem(
    title: String,
    value: String? = null,
    hasArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 15.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(value, color = Color.Gray, fontSize = 14.sp)
            }
            if (hasArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

private fun launchWebUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch web browser", Toast.LENGTH_SHORT).show()
    }
}
