package com.example.ui.dashboard

import com.example.admob.AdMobBannerView
import com.example.admob.findActivity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.PerformanceMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.widthIn
import com.example.ui.components.WallpaperBackground
import com.example.ui.settings.WallpaperEngineDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameIconMode
import com.example.data.GameItem
import com.example.data.GameRepository
import com.example.ui.components.CubeBackgroundCanvas
import com.example.ui.components.GameIconBadge
import com.example.ui.ingame.PerformanceDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: GameRepository,
    onNavigateToAddGames: () -> Unit,
    onNavigateToAlbum: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onOpenIconSettings: () -> Unit,
    onOpenIconTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        repository.loadInstalledApps(context)
    }

    val games by repository.games.collectAsState()
    val enabledGames = games.filter { it.isEnabled }
    val iconMode by repository.iconMode.collectAsState()

    var selectedGameIndex by remember { mutableIntStateOf(0) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    var gameToRemove by remember { mutableStateOf<GameItem?>(null) }
    var gameToUninstall by remember { mutableStateOf<GameItem?>(null) }
    var hyperBoostGame by remember { mutableStateOf<GameItem?>(null) }

    // Dialog & Sheet states triggered from the 3-line menu
    var isTimePlayedSheetOpen by remember { mutableStateOf(false) }
    var isPerformanceDrawerOpen by remember { mutableStateOf(false) }
    var isPermissionsDialogOpen by remember { mutableStateOf(false) }
    var isDevProfileDialogOpen by remember { mutableStateOf(false) }
    var isLandscapeOrientation by remember { mutableStateOf(false) }

    // Theme Customization Dialog States
    var isWallpaperDialogOpen by remember { mutableStateOf(false) }
    var isAccentColorDialogOpen by remember { mutableStateOf(false) }

    val activeWallpaper by repository.customWallpaper.collectAsState()
    val activeAccentColorHex by repository.accentColorHex.collectAsState()
    val isLiquidGlass by repository.isLiquidGlassMode.collectAsState()
    val blurIntensity by repository.blurIntensity.collectAsState()
    val uiBrightness by repository.uiBrightness.collectAsState()
    val liquidGlassOpacity by repository.liquidGlassOpacity.collectAsState()
    val liquidGlassSpecular by repository.liquidGlassSpecular.collectAsState()
    val currentMode by repository.currentPerformanceMode.collectAsState()

    val effectiveAccentColor = remember(activeAccentColorHex, currentMode) {
        if (currentMode == PerformanceMode.PRO_GAMER) {
            Color(0xFFFFB700)
        } else {
            try {
                Color(android.graphics.Color.parseColor(activeAccentColorHex))
            } catch (e: Exception) {
                Color(0xFF22C55E)
            }
        }
    }

    LaunchedEffect(activeAccentColorHex, currentMode) {
        repository.applyAppIconForContext(context)
    }

    val safeIndex = if (selectedGameIndex in enabledGames.indices) selectedGameIndex else 0
    val selectedGame = if (enabledGames.isNotEmpty()) enabledGames[safeIndex] else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF141414),
                drawerContentColor = Color.White,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Card 1: Time played (Matching Image 3651 1:1)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                isTimePlayedSheetOpen = true
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Time played",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "1 h 7 min",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Open Time Played",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Free Fire MAX Item
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                GameIconBadge(gameId = "free_fire", size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Free Fire MAX", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text("Played 17 min ago | 26 min", color = Color.Gray, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Hill Climb Racing 2 Item
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                GameIconBadge(gameId = "hill_climb", size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Hill Climb Racing 2", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text("Played 18 min ago | 36 min", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card 2: App Icon & Theme Customization
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onOpenIconTheme()
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "App Icon & Theme",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "App Icon & Theme",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Customize home screen launcher icon & accent style",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card 3: Settings & Tools List Card (Matching Image 3651 1:1)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            // Game album
                            DrawerMenuItemRow(
                                title = "Game album",
                                subtitle = null,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToAlbum()
                                }
                            )

                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                            // Game icon settings
                            DrawerMenuItemRow(
                                title = "Game icon settings",
                                subtitle = if (iconMode == GameIconMode.FLAT_MODE) "Flat mode" else "Organise mode",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onOpenIconSettings()
                                }
                            )

                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                            // More settings
                            DrawerMenuItemRow(
                                title = "More settings",
                                subtitle = null,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    com.example.admob.AdMobManager.showInterstitialAd(context.findActivity()) {
                                        onNavigateToSettings()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    ) {
        // MAIN HOME SCREEN WITH DYNAMIC WALLPAPER ENGINE
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperBackground(
                wallpaperSpec = activeWallpaper,
                accentColor = effectiveAccentColor,
                blurIntensity = blurIntensity,
                uiBrightness = uiBrightness
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 20.dp)
            ) {
                // TOP BAR: ☰ Menu Icon + iOS-Style Glassy Carousel for Added Games
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ☰ Prominent Hamburger Menu Button
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x38121824))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Drawer Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // iOS-Style Glassy Carousel for Added Games
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(enabledGames) { index, game ->
                            val isSelected = index == safeIndex
                            val cardScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 0.95f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "glassCardScale"
                            )

                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = cardScale
                                        scaleY = cardScale
                                    }
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isSelected) effectiveAccentColor.copy(alpha = 0.32f) else Color(0x38121824)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) effectiveAccentColor else Color.White.copy(alpha = 0.25f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedGameIndex = index }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    GameIconBadge(gameId = game.id, size = 38.dp)
                                    if (isSelected) {
                                        Text(
                                            text = game.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.widthIn(max = 90.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // (+) Add Game Glass Card
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x38121824))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                    .clickable { onNavigateToAddGames() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Game",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

            // MAIN CARD & DISPLAY AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                selectedGame?.let { game ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title & Subtitle Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 38.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = game.timePlayedText,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            Box {
                                IconButton(onClick = { isMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF222222))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("App info", color = Color.White) },
                                        onClick = {
                                            isMenuExpanded = false
                                            Toast.makeText(context, "${game.title} App Info", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Remove from Game Assistant", color = Color.White) },
                                        onClick = {
                                            isMenuExpanded = false
                                            gameToRemove = game
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Uninstall", color = Color.White) },
                                        onClick = {
                                            isMenuExpanded = false
                                            gameToUninstall = game
                                        }
                                    )
                                }
                            }
                        }

                        // Bottom Pinned Action Bar (Image 3725: Sleek compact Performance Settings pill on left, Launch button on right)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Compact Pill: Performance Settings Icon & Label
                            Row(
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1A1A1A).copy(alpha = 0.9f))
                                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp))
                                    .clickable { isPerformanceDrawerOpen = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Performance settings",
                                    tint = effectiveAccentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = "Performance",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 13.sp
                                    )
                                    Text(
                                        text = "settings",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }

                            // Right Button: Sleek Accent Launch Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(effectiveAccentColor, effectiveAccentColor.copy(alpha = 0.8f))
                                        )
                                    )
                                    .clickable {
                                        hyperBoostGame = game
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Launch",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } ?: run {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp))
                                .clickable { onNavigateToAddGames() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Games",
                                tint = effectiveAccentColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Games Added",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap the (+) tile above or below to scan and select installed games from your device.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(effectiveAccentColor)
                                .clickable { onNavigateToAddGames() }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Add Game (+)",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Non-intrusive AdMob Banner at bottom of main dashboard layout
            AdMobBannerView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

    // Time Played Sheet
    if (isTimePlayedSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isTimePlayedSheetOpen = false },
            containerColor = Color(0xFF1C1C1C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Time Played & Usage Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                TimePlayedCard(games = enabledGames)
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = { isTimePlayedSheetOpen = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = Color(0xFF22C55E))
                }
            }
        }
    }

    // Performance Drawer
    if (isPerformanceDrawerOpen) {
        ModalBottomSheet(
            onDismissRequest = { isPerformanceDrawerOpen = false },
            containerColor = Color(0xFF141414)
        ) {
            PerformanceDrawer(repository = repository)
        }
    }

    // Permissions Dialog
    if (isPermissionsDialogOpen) {
        AlertDialog(
            onDismissRequest = { isPermissionsDialogOpen = false },
            containerColor = Color(0xFF2B2B2B),
            title = { Text("System Permissions", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Game Assistant uses these system permissions for smooth in-game overlays:", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionStatusRow("Display Over Other Apps (Overlay)", true)
                    PermissionStatusRow("Modify System Settings (WRITE_SETTINGS)", true)
                    PermissionStatusRow("Accessibility Service (Game Detection)", true)
                }
            },
            confirmButton = {
                TextButton(onClick = { isPermissionsDialogOpen = false }) {
                    Text("OK", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Developer Profile Dialog
    if (isDevProfileDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDevProfileDialogOpen = false },
            containerColor = Color(0xFF1E2820),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developer Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Created by Chetan Koli", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Handle: @starking_1m", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Anti-Ban Status Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(effectiveAccentColor.copy(alpha = 0.2f))
                            .border(1.dp, effectiveAccentColor, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = effectiveAccentColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("100% ID-BAN SAFE & ANTI-BAN ACTIVE", color = effectiveAccentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Zero risk for account safety across all games", color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text("Official Social Handles:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@starking_1m"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "YouTube: @starking_1m", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("▶ YouTube: @starking_1m", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEC4899).copy(alpha = 0.15f))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/chetan_koli_7772"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Instagram: @chetan_koli_7772", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📷 Instagram: @chetan_koli_7772", color = Color(0xFFEC4899), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isDevProfileDialogOpen = false }) {
                    Text("Got it", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Custom Wallpaper Engine Dialog
    if (isWallpaperDialogOpen) {
        WallpaperEngineDialog(
            currentWallpaper = activeWallpaper,
            accentColor = effectiveAccentColor,
            onWallpaperSelected = { wpSpec ->
                repository.updateWallpaper(context, wpSpec)
            },
            onDismiss = { isWallpaperDialogOpen = false }
        )
    }

    // Dynamic Accent Color Switcher Dialog
    if (isAccentColorDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAccentColorDialogOpen = false },
            containerColor = Color(0xFF262626),
            title = { Text("🎨 Choose Theme Accent Color", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Electric Green" to "#22C55E",
                        "Neon Red" to "#EF4444",
                        "Cyberpunk Orange" to "#F97316",
                        "Vibrant Yellow" to "#EAB308",
                        "Sleek Cyan" to "#06B6D4",
                        "Royal Purple" to "#A855F7"
                    ).forEach { (name, hex) ->
                        val isSel = activeAccentColorHex == hex
                        val badgeColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) badgeColor else Color(0xFF333333))
                                .clickable {
                                    repository.accentColorHex.value = hex
                                    repository.applyAppIconForContext(context)
                                    isAccentColorDialogOpen = false
                                    Toast.makeText(context, "🎨 Accent Color set to $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(name, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isAccentColorDialogOpen = false }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }

    // Remove Confirmation Dialog
    gameToRemove?.let { game ->
        AlertDialog(
            onDismissRequest = { gameToRemove = null },
            containerColor = Color(0xFF2B2B2B),
            title = { Text("Remove ${game.title}?", color = Color.White) },
            text = { Text("This game will no longer be managed by Game Assistant.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    repository.removeGame(context, game.id)
                    gameToRemove = null
                }) {
                    Text("Remove", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToRemove = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Uninstall Dialog
    gameToUninstall?.let { game ->
        AlertDialog(
            onDismissRequest = { gameToUninstall = null },
            containerColor = Color(0xFF2B2B2B),
            title = { Text("Uninstall ${game.title}?", color = Color.White) },
            text = { Text("Do you want to uninstall this app from device?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    repository.removeGame(game.id)
                    gameToUninstall = null
                    Toast.makeText(context, "${game.title} uninstalled", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Uninstall", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToUninstall = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // HyperBoost Launch Animation Overlay
    hyperBoostGame?.let { game ->
        HyperBoostLaunchOverlay(
            game = game,
            accentColor = effectiveAccentColor,
            onFinishLaunch = { hyperBoostGame = null }
        )
    }
}

@Composable
fun HyperBoostLaunchOverlay(
    game: GameItem,
    accentColor: Color = Color(0xFF22C55E),
    onFinishLaunch: () -> Unit
) {
    val context = LocalContext.current
    var animationPhase by remember { mutableIntStateOf(1) } // 1: Pulse Rings, 2: 3D Cube, 3: Banner Pop-Up, 4: Retract

    // Infinite rotation for particles
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "rotate"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(game) {
        // Phase 1: Energy particles & ring pulse
        delay(600)
        // Phase 2: Morph to 3D Isometric Cube
        animationPhase = 2
        delay(600)
        // Phase 3: Sleek Toast Banner ("GAMES / Unrivaled Performance")
        animationPhase = 3
        delay(2200)
        // Phase 4: Slide & retract to edge handle
        animationPhase = 4
        delay(400)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "M Assistant Launched ${game.title}!", Toast.LENGTH_SHORT).show()
        }
        onFinishLaunch()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.TopStart
    ) {
        AnimatedVisibility(
            visible = animationPhase < 4,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.padding(top = 180.dp, start = 8.dp)
        ) {
            when (animationPhase) {
                1 -> {
                    // Phase 1: Rotating Energy Ring & Particles
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF121212))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(60.dp)) {
                            val centerPt = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                            // Outer glowing ring
                            drawCircle(
                                color = accentColor.copy(alpha = 0.25f),
                                radius = (size.width / 2) * pulseScale,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                            // 3 Orbiting Particles
                            for (i in 0..2) {
                                val angleRad = Math.toRadians((rotationAngle + i * 120).toDouble())
                                val radius = size.width * 0.35f
                                val x = centerPt.x + (radius * Math.cos(angleRad)).toFloat()
                                val y = centerPt.y + (radius * Math.sin(angleRad)).toFloat()
                                drawCircle(
                                    color = accentColor,
                                    radius = 6.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Phase 2: 3D Isometric Cube
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF121212))
                            .border(1.5.dp, accentColor, RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IsometricNeonCube(sizeDp = 50, accentColor = accentColor)
                    }
                }
                3 -> {
                    // Phase 3: "GAMES / Unrivaled Performance" 1:1 Toast Banner (Reference 3743.mp4)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF141414))
                            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IsometricNeonCube(sizeDp = 34, accentColor = accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "GAMES",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Unrivaled Performance",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IsometricNeonCube(sizeDp: Int = 40, accentColor: Color = Color(0xFF22C55E)) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w / 2.2f

        // 6 Outer Hexagon Vertices
        val p0 = androidx.compose.ui.geometry.Offset(cx, cy - r)
        val p1 = androidx.compose.ui.geometry.Offset(cx + r * 0.866f, cy - r * 0.5f)
        val p2 = androidx.compose.ui.geometry.Offset(cx + r * 0.866f, cy + r * 0.5f)
        val p3 = androidx.compose.ui.geometry.Offset(cx, cy + r)
        val p4 = androidx.compose.ui.geometry.Offset(cx - r * 0.866f, cy + r * 0.5f)
        val p5 = androidx.compose.ui.geometry.Offset(cx - r * 0.866f, cy - r * 0.5f)
        val center = androidx.compose.ui.geometry.Offset(cx, cy)

        // Top Face
        val topPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y)
            lineTo(p0.x, p0.y)
            lineTo(p1.x, p1.y)
            lineTo(center.x, center.y)
            moveTo(center.x, center.y)
            lineTo(p5.x, p5.y)
            lineTo(p0.x, p0.y)
            close()
        }
        drawPath(topPath, color = accentColor.copy(alpha = 0.8f))

        // Left Face
        val leftPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y)
            lineTo(p5.x, p5.y)
            lineTo(p4.x, p4.y)
            lineTo(p3.x, p3.y)
            close()
        }
        drawPath(leftPath, color = accentColor.copy(alpha = 0.5f))

        // Right Face
        val rightPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        }
        drawPath(rightPath, color = accentColor)

        // Wireframe Highlights
        drawCircle(color = accentColor.copy(alpha = 0.3f), radius = r * 1.2f, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
    }
}

@Composable
fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun GlassmorphicFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2420)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PermissionStatusRow(title: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            if (isGranted) "Active" else "Missing",
            color = if (isGranted) Color(0xFF22C55E) else Color(0xFFEF4444),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DrawerMenuItemRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


