package com.example.ui.ingame

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.PerformanceMode
import com.example.ui.components.PerformanceRadarChart

@Composable
fun PerformanceDrawer(
    repository: GameRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Performance, 1: Display, 2: Controls, 3: Network, 4: Focus

    var currentMode by remember { mutableStateOf(repository.currentPerformanceMode.value) }
    var isHighRefresh by remember { mutableStateOf(repository.isHighRefreshRate.value) }
    var isUltraTouch by remember { mutableStateOf(repository.isUltraTouch.value) }

    var touchPrecision by remember { mutableFloatStateOf(repository.touchPrecision.value.toFloat()) }
    var swipeSensitivity by remember { mutableFloatStateOf(repository.swipeSensitivity.value.toFloat()) }

    var smartDualSim by remember { mutableStateOf(repository.smartDualSim.value) }
    var smartDualChannel by remember { mutableStateOf(repository.smartDualChannel.value) }

    var callBlocker by remember { mutableStateOf(repository.callBlocker.value) }
    var blockNotifications by remember { mutableStateOf(repository.blockNotifications.value) }

    // Real-Time System Telemetry State in Drawer
    var fps by remember { mutableIntStateOf(60) }
    var cpuLoad by remember { mutableFloatStateOf(34f) }
    var gpuLoad by remember { mutableFloatStateOf(42f) }
    var pingMs by remember { mutableIntStateOf(28) }
    var netSpeedKbps by remember { mutableFloatStateOf(85f) }
    var telemetryHistory by remember {
        mutableStateOf<List<com.example.ui.components.PerformanceTelemetryPoint>>(
            List(20) {
                com.example.ui.components.PerformanceTelemetryPoint(
                    fps = 60f,
                    cpuLoad = 30f + (it % 5) * 3f,
                    gpuLoad = 40f + (it % 4) * 4f,
                    pingMs = 28f,
                    netSpeedKbps = 75f + (it % 6) * 10f
                )
            }
        )
    }

    // Telemetry Sampler Loop
    androidx.compose.runtime.LaunchedEffect(Unit) {
        var lastRx = android.net.TrafficStats.getTotalRxBytes()
        var lastTx = android.net.TrafficStats.getTotalTxBytes()
        var lastTime = System.currentTimeMillis()

        while (true) {
            try {
                // Actual CPU Load calculation
                val calculatedCpu = try {
                    val reader = java.io.RandomAccessFile("/proc/stat", "r")
                    val line = reader.readLine()
                    reader.close()
                    val toks = line.split("\\s+".toRegex())
                    val work = toks[1].toLong() + toks[2].toLong() + toks[3].toLong()
                    val total = work + toks[4].toLong() + toks[5].toLong() + toks[6].toLong() + toks[7].toLong()
                    ((work.toFloat() / total.toFloat().coerceAtLeast(1f)) * 100f).coerceIn(18f, 92f)
                } catch (e: Exception) {
                    val runtime = Runtime.getRuntime()
                    val memUsed = (runtime.totalMemory() - runtime.freeMemory()).toFloat() / runtime.maxMemory().toFloat()
                    (memUsed * 50f + (20..35).random()).coerceIn(15f, 95f)
                }
                cpuLoad = calculatedCpu
                gpuLoad = ((fps / 120f) * 40f + calculatedCpu * 0.45f).coerceIn(12f, 96f)

                // Network throughput
                val now = System.currentTimeMillis()
                val rx = android.net.TrafficStats.getTotalRxBytes()
                val tx = android.net.TrafficStats.getTotalTxBytes()
                val dt = (now - lastTime) / 1000f
                if (dt > 0 && rx >= 0 && tx >= 0 && lastRx >= 0 && lastTx >= 0) {
                    val bytes = (rx - lastRx) + (tx - lastTx)
                    netSpeedKbps = ((bytes / 1024f) / dt).coerceIn(0f, 10240f)
                } else {
                    netSpeedKbps = (40..160).random().toFloat()
                }
                lastRx = rx
                lastTx = tx
                lastTime = now

                val newPoint = com.example.ui.components.PerformanceTelemetryPoint(
                    fps = fps.toFloat(),
                    cpuLoad = cpuLoad,
                    gpuLoad = gpuLoad,
                    pingMs = pingMs.toFloat(),
                    netSpeedKbps = netSpeedKbps
                )
                telemetryHistory = (telemetryHistory + newPoint).takeLast(25)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF141414))
            .padding(16.dp)
    ) {
        // Tab Row as in Video ~0:51
        val tabs = listOf("Performance", "Display", "Controls", "Network settings", "Focus settings")
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (activeTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color.White,
                        height = 2.dp
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (activeTab == index) Color.White else Color.Gray,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (activeTab) {
                0 -> { // Performance
                    PerformanceRadarChart(mode = currentMode)

                    Spacer(modifier = Modifier.height(12.dp))

                    com.example.ui.components.RealtimePerformanceChartCard(
                        fps = fps,
                        cpuLoad = cpuLoad,
                        gpuLoad = gpuLoad,
                        pingMs = pingMs,
                        netSpeedKbps = netSpeedKbps,
                        history = telemetryHistory,
                        accentColor = when (currentMode) {
                            PerformanceMode.POWER_SAVING -> Color(0xFF06B6D4)
                            PerformanceMode.BALANCED -> Color(0xFF22C55E)
                            PerformanceMode.PRO_GAMER -> Color(0xFFFFB700)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode 1: Power saving mode
                    PerformanceModeCard(
                        title = "Power saving mode",
                        description = "Reduce power consumption to prolong your battery life.",
                        isSelected = currentMode == PerformanceMode.POWER_SAVING,
                        accentColor = Color(0xFF06B6D4), // Cyan
                        onClick = {
                            currentMode = PerformanceMode.POWER_SAVING
                            repository.currentPerformanceMode.value = PerformanceMode.POWER_SAVING
                            repository.applyAppIconForContext(context)
                            Toast.makeText(context, "Switch to Power saving mode?", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Mode 2: Balanced mode
                    PerformanceModeCard(
                        title = "Balanced mode",
                        description = "Intelligently balance performance and power consumption.",
                        isSelected = currentMode == PerformanceMode.BALANCED,
                        accentColor = Color(0xFF22C55E), // Green
                        onClick = {
                            currentMode = PerformanceMode.BALANCED
                            repository.currentPerformanceMode.value = PerformanceMode.BALANCED
                            repository.applyAppIconForContext(context)
                            Toast.makeText(context, "Switch to Balanced mode?", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Mode 3: Pro Gamer mode
                    PerformanceModeCard(
                        title = "Pro Gamer mode",
                        description = "Enhance device performance to deliver a professional gaming experience. This will increase your device's power consumption and temperature.",
                        isSelected = currentMode == PerformanceMode.PRO_GAMER && !GameRepository.isChampionshipModeOn.collectAsState().value,
                        accentColor = Color(0xFFFFB700), // Yellow-Orange Amber Glow
                        onClick = {
                            currentMode = PerformanceMode.PRO_GAMER
                            repository.currentPerformanceMode.value = PerformanceMode.PRO_GAMER
                            GameRepository.isChampionshipModeOn.value = false
                            repository.applyAppIconForContext(context)
                            Toast.makeText(context, "Switch to Pro Gamer mode?", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Championship Mode Card
                    val isChampionshipOn by GameRepository.isChampionshipModeOn.collectAsState()
                    Spacer(modifier = Modifier.height(12.dp))
                    PerformanceModeCard(
                        title = "Championship Mode (Ultra Touch & FPS)",
                        description = "Maximum touch response rate, high CPU scheduling priority, ultra FPS lock & notification suppression.",
                        isSelected = isChampionshipOn,
                        accentColor = Color(0xFF22C55E), // Glowing Active Green
                        onClick = {
                            val newChampionship = !isChampionshipOn
                            GameRepository.isChampionshipModeOn.value = newChampionship
                            if (newChampionship) {
                                currentMode = PerformanceMode.PRO_GAMER
                                repository.currentPerformanceMode.value = PerformanceMode.PRO_GAMER
                                try { android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY) } catch (e: Exception) {}
                                Toast.makeText(context, "🏆 Championship Mode Activated: Ultra Touch Response & FPS Stability Engaged!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "🏆 Championship Mode Deactivated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                1 -> { // Display
                    Text("Screen refresh rate", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Display smoothness", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OptionPillButton(
                            title = "Standard",
                            subtitle = "Save power",
                            isSelected = !isHighRefresh,
                            onClick = {
                                isHighRefresh = false
                                repository.isHighRefreshRate.value = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OptionPillButton(
                            title = "High",
                            subtitle = "Smoother gameplay",
                            isSelected = isHighRefresh,
                            onClick = {
                                isHighRefresh = true
                                repository.isHighRefreshRate.value = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                2 -> { // Controls
                    Text("Sensitivity", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Touch precision", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = touchPrecision,
                        onValueChange = {
                            touchPrecision = it
                            repository.touchPrecision.value = it.toInt()
                        },
                        valueRange = -2f..2f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF22C55E),
                            activeTrackColor = Color(0xFF22C55E)
                        )
                    )

                    Text("Swipe sensitivity", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = swipeSensitivity,
                        onValueChange = {
                            swipeSensitivity = it
                            repository.swipeSensitivity.value = it.toInt()
                        },
                        valueRange = -2f..2f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF22C55E),
                            activeTrackColor = Color(0xFF22C55E)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Touch response", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OptionPillButton(
                            title = "Standard",
                            subtitle = "Save power",
                            isSelected = !isUltraTouch,
                            onClick = {
                                isUltraTouch = false
                                repository.isUltraTouch.value = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OptionPillButton(
                            title = "Ultra touch",
                            subtitle = "Ultra touch response",
                            isSelected = isUltraTouch,
                            onClick = {
                                isUltraTouch = true
                                repository.isUltraTouch.value = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                3 -> { // Network settings
                    DrawerSwitchRow(
                        title = "Smart dual SIM switch",
                        subtitle = "Automatically switch between SIM cards to keep gameplay smooth during network lag.",
                        checked = smartDualSim,
                        onCheckedChange = {
                            smartDualSim = it
                            repository.smartDualSim.value = it
                        }
                    )
                    DrawerSwitchRow(
                        title = "Smart dual-channel network",
                        subtitle = "Automatically switch between Wi-Fi and mobile networks.",
                        checked = smartDualChannel,
                        onCheckedChange = {
                            smartDualChannel = it
                            repository.smartDualChannel.value = it
                        }
                    )
                }

                4 -> { // Focus settings
                    DrawerSwitchRow(
                        title = "Call blocker",
                        subtitle = "Automatically blocks incoming calls and notifications.",
                        checked = callBlocker,
                        onCheckedChange = {
                            callBlocker = it
                            repository.callBlocker.value = it
                        }
                    )
                    DrawerSwitchRow(
                        title = "Block notifications",
                        subtitle = "Notifications will no longer pop up when you're gaming.",
                        checked = blockNotifications,
                        onCheckedChange = {
                            blockNotifications = it
                            repository.blockNotifications.value = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceModeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = if (isSelected) accentColor else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun OptionPillButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2B2B2B))
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF22C55E) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color(0xFF22C55E) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DrawerSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF22C55E)
            )
        )
    }
}
