package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.GameRepository
import com.example.ui.addgames.AddGamesScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.GameAlbumScreen
import com.example.ui.dashboard.GameIconSettingsBottomSheet
import com.example.ui.ingame.InGameOverlay
import com.example.ui.settings.GameAssistantDetailScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val repository = GameRepository()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.admob.AdMobManager.initializeWithConsent(this)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val navController = rememberNavController()

                    var isIconSettingsOpen by remember { mutableStateOf(false) }

                    val launchAnimEnabled by repository.launchAnimationEnabled.collectAsState()
                    val masterAssistantEnabled by repository.gameAssistantMasterEnabled.collectAsState()

                    val context = androidx.compose.ui.platform.LocalContext.current

                    androidx.compose.runtime.LaunchedEffect(masterAssistantEnabled) {
                        try {
                            val serviceIntent = android.content.Intent(context, com.example.service.GameAssistantService::class.java)
                            if (masterAssistantEnabled && android.provider.Settings.canDrawOverlays(context)) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(serviceIntent)
                                } else {
                                    context.startService(serviceIntent)
                                }
                            } else {
                                context.stopService(serviceIntent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        val prefs = remember { context.getSharedPreferences("game_assistant_prefs", android.content.Context.MODE_PRIVATE) }
                        var isFirstLaunch by remember { mutableStateOf(prefs.getBoolean("is_first_launch", true)) }

                        val initialRoute = if (isFirstLaunch) {
                            "onboarding"
                        } else if (launchAnimEnabled) {
                            "splash"
                        } else {
                            "dashboard"
                        }

                        NavHost(
                            navController = navController,
                            startDestination = initialRoute
                        ) {
                            composable("onboarding") {
                                com.example.ui.onboarding.OnboardingScreen(
                                    onAgree = {
                                        prefs.edit().putBoolean("is_first_launch", false).apply()
                                        isFirstLaunch = false
                                        val target = if (launchAnimEnabled) "splash" else "dashboard"
                                        navController.navigate(target) {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    },
                                    onDisagree = {
                                        (context as? android.app.Activity)?.finish()
                                    }
                                )
                            }

                            composable("splash") {
                                SplashScreen(
                                    onSplashFinished = {
                                        navController.navigate("dashboard") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("dashboard") {
                                DashboardScreen(
                                    repository = repository,
                                    onNavigateToAddGames = { navController.navigate("add_games") },
                                    onNavigateToAlbum = { navController.navigate("game_album") },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToPermissions = { navController.navigate("app_permissions") },
                                    onOpenIconSettings = { isIconSettingsOpen = true },
                                    onOpenIconTheme = { navController.navigate("app_icon_theme") }
                                )
                            }

                            composable("add_games") {
                                val games by repository.games.collectAsState()
                                val otherApps by repository.otherApps.collectAsState()
                                val currentCtx = androidx.compose.ui.platform.LocalContext.current

                                AddGamesScreen(
                                    recommendedGames = games,
                                    otherApps = otherApps,
                                    onToggleRecommended = { id, enabled ->
                                        repository.toggleRecommendedGame(currentCtx, id, enabled)
                                    },
                                    onToggleOtherApp = { id, enabled ->
                                        repository.toggleOtherApp(currentCtx, id, enabled)
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("game_album") {
                                val games by repository.games.collectAsState()
                                val albumMedia by repository.albumMedia.collectAsState()
                                val currentCtx = androidx.compose.ui.platform.LocalContext.current
                                androidx.compose.runtime.LaunchedEffect(Unit) {
                                    repository.loadAlbumMedia(currentCtx)
                                }
                                GameAlbumScreen(
                                    games = games,
                                    albumMedia = albumMedia,
                                    onDeleteMedia = { item ->
                                        repository.deleteAlbumMedia(currentCtx, item)
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    repository = repository,
                                    onNavigateToDetail = { navController.navigate("settings_detail") },
                                    onNavigateToPermissions = { navController.navigate("app_permissions") },
                                    onNavigateToIconTheme = { navController.navigate("app_icon_theme") },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("settings_detail") {
                                GameAssistantDetailScreen(
                                    repository = repository,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("app_permissions") {
                                com.example.ui.settings.AppPermissionsScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("app_icon_theme") {
                                com.example.ui.settings.AppIconThemeScreen(
                                    repository = repository,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Game Icon Settings Bottom Sheet
                        if (isIconSettingsOpen) {
                            val iconMode by repository.iconMode.collectAsState()
                            GameIconSettingsBottomSheet(
                                currentMode = iconMode,
                                onModeSelected = { newMode ->
                                    repository.iconMode.value = newMode
                                },
                                onDismiss = { isIconSettingsOpen = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private val overlayBroadcastReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            // No-op for overlay receiver
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val filter = android.content.IntentFilter().apply {
                addAction("android.intent.action.OVERLAY_CHANGED")
                addAction("com.android.game.THEME_CHANGED")
                addAction("android.intent.action.CONFIGURATION_CHANGED")
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(overlayBroadcastReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(overlayBroadcastReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(overlayBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
