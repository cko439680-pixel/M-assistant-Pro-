package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameItem
import com.example.ui.components.GameIconBadge

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import java.util.Calendar

@Composable
fun TimePlayedCard(
    games: List<GameItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Last 30 days") }

    val usageStatsMap = remember(selectedFilter) {
        getRealUsageStats(context, selectedFilter)
    }

    val updatedGames = remember(games, usageStatsMap) {
        games.map { game ->
            val realMillis = usageStatsMap[game.packageName] ?: 0L
            val realMins = (realMillis / (1000 * 60)).toInt()
            if (realMins > 0) {
                game.copy(playTimeMinutes = realMins)
            } else {
                game
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Time played",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    val totalMins = updatedGames.sumOf { it.playTimeMinutes }
                    val hours = totalMins / 60
                    val mins = totalMins % 60
                    val totalTimeString = if (hours > 0) "${hours} h ${mins} min" else "${mins} min"
                    Text(
                        text = totalTimeString,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                BoxWithFilter(
                    selectedFilter = selectedFilter,
                    expanded = expandedMenu,
                    onToggle = { expandedMenu = !expandedMenu },
                    onSelect = {
                        selectedFilter = it
                        expandedMenu = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (usageStatsMap.isEmpty()) {
                Button(
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Usage Access for Real Stats", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Played ${updatedGames.filter { it.playTimeMinutes > 0 }.size} games in total",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            updatedGames.filter { it.playTimeMinutes > 0 }.forEach { game ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameIconBadge(gameId = game.id, size = 42.dp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = game.lastPlayedAgo,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "${game.playTimeMinutes} min",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun getRealUsageStats(context: Context, filter: String): Map<String, Long> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()
    val calendar = Calendar.getInstance()
    val endTime = calendar.timeInMillis
    when (filter) {
        "Today" -> calendar.add(Calendar.DAY_OF_YEAR, -1)
        "Last 7 days" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
        else -> calendar.add(Calendar.DAY_OF_YEAR, -30)
    }
    val startTime = calendar.timeInMillis

    val stats = try {
        usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
    } catch (e: Exception) {
        null
    }

    val map = mutableMapOf<String, Long>()
    stats?.forEach { stat ->
        if (stat.totalTimeInForeground > 0) {
            map[stat.packageName] = (map[stat.packageName] ?: 0L) + stat.totalTimeInForeground
        }
    }
    return map
}

@Composable
private fun BoxWithFilter(
    selectedFilter: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2C))
                .clickable { onToggle() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedFilter,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Filter",
                tint = Color.White
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onToggle,
            modifier = Modifier.background(Color(0xFF2C2C2C))
        ) {
            listOf("Today", "Last 7 days", "Last 30 days").forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color.White) },
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}
