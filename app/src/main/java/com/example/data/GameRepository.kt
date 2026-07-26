package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository {

    private val _games = MutableStateFlow<List<GameItem>>(emptyList())
    val games: StateFlow<List<GameItem>> = _games.asStateFlow()

    private val _otherApps = MutableStateFlow<List<GameItem>>(emptyList())
    val otherApps: StateFlow<List<GameItem>> = _otherApps.asStateFlow()

    var isAppsLoaded = false

    fun initPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            val savedStyle = prefs.getString("selected_panel_style", null)
            if (!savedStyle.isNullOrEmpty()) {
                selectedPanelStyle.value = savedStyle
            }
            val savedWallpaper = prefs.getString("custom_wallpaper", null)
            if (!savedWallpaper.isNullOrEmpty()) {
                customWallpaper.value = savedWallpaper
            }
            val savedAccent = prefs.getString("accent_color_hex", null)
            if (!savedAccent.isNullOrEmpty()) {
                accentColorHex.value = savedAccent
            }
            if (prefs.contains("is_liquid_glass")) {
                isLiquidGlassMode.value = prefs.getBoolean("is_liquid_glass", false)
            }
            if (prefs.contains("blur_intensity")) {
                blurIntensity.value = prefs.getFloat("blur_intensity", 0.6f)
            }
            if (prefs.contains("ui_brightness")) {
                uiBrightness.value = prefs.getFloat("ui_brightness", 0.8f)
            }
            if (prefs.contains("liquid_glass_opacity")) {
                liquidGlassOpacity.value = prefs.getFloat("liquid_glass_opacity", 0.7f)
            }
            if (prefs.contains("liquid_glass_specular")) {
                liquidGlassSpecular.value = prefs.getFloat("liquid_glass_specular", 0.8f)
            }
            if (prefs.contains("game_assistant_master_enabled")) {
                gameAssistantMasterEnabled.value = prefs.getBoolean("game_assistant_master_enabled", true)
            }
            if (prefs.contains("is_mistouch_active")) {
                isMistouchActive.value = prefs.getBoolean("is_mistouch_active", false)
            }
            val savedPerfMode = prefs.getString("current_performance_mode", null)
            if (!savedPerfMode.isNullOrEmpty()) {
                try {
                    currentPerformanceMode.value = PerformanceMode.valueOf(savedPerfMode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val toolsPrefs = context.getSharedPreferences("m_assistant_tools_prefs", Context.MODE_PRIVATE)
            if (toolsPrefs.contains("fps_widget_active")) {
                isFpsWidgetOn.value = toolsPrefs.getBoolean("fps_widget_active", false)
            }
            if (toolsPrefs.contains("system_status_active")) {
                isSystemStatusOn.value = toolsPrefs.getBoolean("system_status_active", false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePanelStyle(context: Context, style: String) {
        selectedPanelStyle.value = style
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("selected_panel_style", style).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLiquidGlassMode(context: Context, enabled: Boolean) {
        isLiquidGlassMode.value = enabled
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_liquid_glass", enabled).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateBlurIntensity(context: Context, value: Float) {
        blurIntensity.value = value
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putFloat("blur_intensity", value).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateUiBrightness(context: Context, value: Float) {
        uiBrightness.value = value
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putFloat("ui_brightness", value).apply()
            com.example.ui.ingame.applyScreenBrightness(context, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLiquidGlassOpacity(context: Context, value: Float) {
        liquidGlassOpacity.value = value
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putFloat("liquid_glass_opacity", value).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLiquidGlassSpecular(context: Context, value: Float) {
        liquidGlassSpecular.value = value
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putFloat("liquid_glass_specular", value).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateWallpaper(context: Context, wallpaper: String) {
        customWallpaper.value = wallpaper
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("custom_wallpaper", wallpaper).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAccentColor(context: Context, hexColor: String) {
        accentColorHex.value = hexColor
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("accent_color_hex", hexColor).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveEnabledApps(context: Context) {
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            val enabledPkgNames = _games.value.filter { it.isEnabled }.map { it.packageName }.toSet()
            prefs.edit().putStringSet("enabled_game_pkg_names", enabledPkgNames).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadInstalledApps(context: Context) {
        initPreferences(context)
        if (isAppsLoaded) return
        try {
            val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
            val savedEnabledIds = prefs.getStringSet("enabled_game_pkg_names", null) ?: emptySet()

            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }

            val installedGamesList = mutableListOf<GameItem>()
            val installedOtherAppsList = mutableListOf<GameItem>()

            for (info in resolveInfos) {
                val appName = info.loadLabel(pm).toString()
                val pkgName = info.activityInfo.packageName
                if (pkgName == context.packageName) continue

                val isGame = try {
                    val appInfo = pm.getApplicationInfo(pkgName, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appInfo.category == ApplicationInfo.CATEGORY_GAME
                    } else false
                } catch (e: Exception) {
                    false
                } || appName.lowercase().run {
                    contains("game") || contains("bgmi") || contains("pubg") || contains("fire") || contains("craft") || contains("racing") || contains("clash") || contains("play") || contains("asphalt") || contains("roblox")
                }

                val isEnabledInPrefs = savedEnabledIds.contains(pkgName)

                val item = GameItem(
                    id = pkgName,
                    title = appName,
                    packageName = pkgName,
                    timePlayedText = "No time played",
                    playTimeMinutes = 0,
                    lastPlayedAgo = "Installed on device",
                    isEnabled = isEnabledInPrefs,
                    isRecommended = isGame
                )

                if (isGame) {
                    installedGamesList.add(item)
                } else {
                    installedOtherAppsList.add(item)
                }
            }

            val allInstalled = installedGamesList + installedOtherAppsList
            _games.value = allInstalled.filter { it.isEnabled }
            _otherApps.value = allInstalled
            isAppsLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val iconMode get() = Companion.iconMode
    val launchAnimationEnabled get() = Companion.launchAnimationEnabled
    val gameAlbumEnabled get() = Companion.gameAlbumEnabled
    val gameAssistantMasterEnabled get() = Companion.gameAssistantMasterEnabled
    val autoAdjustResolution get() = Companion.autoAdjustResolution

    val currentPerformanceMode get() = Companion.currentPerformanceMode
    val isHighRefreshRate get() = Companion.isHighRefreshRate
    val isUltraTouch get() = Companion.isUltraTouch
    val touchPrecision get() = Companion.touchPrecision
    val swipeSensitivity get() = Companion.swipeSensitivity

    val smartDualSim get() = Companion.smartDualSim
    val smartDualChannel get() = Companion.smartDualChannel
    val callBlocker get() = Companion.callBlocker
    val blockNotifications get() = Companion.blockNotifications

    // Theme & Aesthetic Customizations
    val selectedPanelStyle get() = Companion.selectedPanelStyle
    val isLiquidGlassMode get() = Companion.isLiquidGlassMode
    val blurIntensity get() = Companion.blurIntensity
    val uiBrightness get() = Companion.uiBrightness
    val liquidGlassOpacity get() = Companion.liquidGlassOpacity
    val liquidGlassSpecular get() = Companion.liquidGlassSpecular
    val accentColorHex get() = Companion.accentColorHex
    val customWallpaper get() = Companion.customWallpaper
    val isMistouchActive get() = Companion.isMistouchActive
    val albumMedia get() = Companion.albumMedia

    val isAimAssistOn get() = Companion.isAimAssistOn
    val isFpsWidgetOn get() = Companion.isFpsWidgetOn
    val isSystemStatusOn get() = Companion.isSystemStatusOn
    val isSystemStatusCompact get() = Companion.isSystemStatusCompact

    val currentFpsState get() = Companion.currentFpsState
    val pingMsState get() = Companion.pingMsState
    val batteryTempState get() = Companion.batteryTempState
    val batteryPctState get() = Companion.batteryPctState
    val cpuLoadState get() = Companion.cpuLoadState
    val ramUsedPctState get() = Companion.ramUsedPctState

    companion object {
        val iconMode = MutableStateFlow(GameIconMode.FLAT_MODE)
        val launchAnimationEnabled = MutableStateFlow(true)
        val gameAlbumEnabled = MutableStateFlow(true)
        val gameAssistantMasterEnabled = MutableStateFlow(true)
        val autoAdjustResolution = MutableStateFlow(true)

        val currentPerformanceMode = MutableStateFlow(PerformanceMode.BALANCED)
        val isHighRefreshRate = MutableStateFlow(false)
        val isUltraTouch = MutableStateFlow(false)
        val touchPrecision = MutableStateFlow(0)
        val swipeSensitivity = MutableStateFlow(0)

        val smartDualSim = MutableStateFlow(false)
        val smartDualChannel = MutableStateFlow(false)
        val callBlocker = MutableStateFlow(false)
        val blockNotifications = MutableStateFlow(false)

        // Theme & Aesthetic Customizations
        val selectedPanelStyle = MutableStateFlow("Classic / Legacy Panel")
        val isLiquidGlassMode = MutableStateFlow(false)
        val blurIntensity = MutableStateFlow(0.6f)
        val uiBrightness = MutableStateFlow(0.8f)
        val liquidGlassOpacity = MutableStateFlow(0.7f)
        val liquidGlassSpecular = MutableStateFlow(0.8f)
        val accentColorHex = MutableStateFlow("#22C55E") // Default Electric Green
        val customWallpaper = MutableStateFlow("Cyberpunk Neon")
        val isMistouchActive = MutableStateFlow(false)
        val albumMedia = MutableStateFlow<List<GameMediaItem>>(emptyList())

        // Independent System Overlays State
        val isAimAssistOn = MutableStateFlow(false)
        val isFpsWidgetOn = MutableStateFlow(false)
        val isSystemStatusOn = MutableStateFlow(false)
        val isSystemStatusCompact = MutableStateFlow(false)
        val isGamerVisionOn = MutableStateFlow(false)
        val selectedFilterType = MutableStateFlow("Hyper HDR")
        val filterIntensity = MutableStateFlow(0.22f)
        val isPerformanceEngineActive = MutableStateFlow(false)
        val isChampionshipModeOn = MutableStateFlow(false)

        // Real-Time System Metrics State
        val currentFpsState = MutableStateFlow(60)
        val pingMsState = MutableStateFlow(28)
        val batteryTempState = MutableStateFlow(37f)
        val batteryPctState = MutableStateFlow(87)
        val cpuLoadState = MutableStateFlow(34f)
        val ramUsedPctState = MutableStateFlow(61)
    }

    fun loadAlbumMedia(context: Context) {
        try {
            val list = mutableListOf<GameMediaItem>()
            val dir = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "M_Assistant_Pro")
            if (dir.exists()) {
                dir.listFiles()?.sortedByDescending { it.lastModified() }?.forEach { file ->
                    if (file.isFile) {
                        val isVideo = file.name.endsWith(".mp4", ignoreCase = true)
                        val isImg = file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".png", ignoreCase = true)
                        if (isVideo || isImg) {
                            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
                            val dateText = sdf.format(java.util.Date(file.lastModified()))
                            list.add(
                                GameMediaItem(
                                    id = file.absolutePath,
                                    filePath = file.absolutePath,
                                    fileName = file.name,
                                    isVideo = isVideo,
                                    gameTitle = "M Assistant Pro",
                                    dateText = dateText,
                                    timestamp = file.lastModified(),
                                    durationSeconds = if (isVideo) 15 else 0
                                )
                            )
                        }
                    }
                }
            }
            albumMedia.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addAlbumMedia(context: Context, item: GameMediaItem) {
        val current = albumMedia.value.toMutableList()
        current.add(0, item)
        albumMedia.value = current
    }

    fun deleteAlbumMedia(context: Context, item: GameMediaItem) {
        try {
            val file = java.io.File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        albumMedia.value = albumMedia.value.filter { it.id != item.id }
    }

    fun applyAppIconForContext(context: android.content.Context) {
        try {
            val key = com.example.util.AppIconThemeManager.getCurrentVariantKey(context)
            val variant = com.example.util.AppIconThemeManager.VARIANTS.find { it.key == key }
                ?: com.example.util.AppIconThemeManager.VARIANTS.first()
            com.example.util.AppIconThemeManager.changeAppIcon(context, variant.aliasName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleRecommendedGame(context: Context?, id: String, enabled: Boolean) {
        _games.value = _games.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        context?.let { saveEnabledApps(it) }
    }

    fun toggleRecommendedGame(id: String, enabled: Boolean) {
        toggleRecommendedGame(null, id, enabled)
    }

    fun toggleOtherApp(context: Context?, id: String, enabled: Boolean) {
        _otherApps.value = _otherApps.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        val app = _otherApps.value.find { it.id == id } ?: return
        val currentGames = _games.value.toMutableList()
        val existingIndex = currentGames.indexOfFirst { it.id == id || it.packageName == app.packageName }
        if (enabled) {
            if (existingIndex >= 0) {
                currentGames[existingIndex] = currentGames[existingIndex].copy(isEnabled = true)
            } else {
                currentGames.add(app.copy(isEnabled = true, isRecommended = false))
            }
        } else {
            if (existingIndex >= 0) {
                currentGames[existingIndex] = currentGames[existingIndex].copy(isEnabled = false)
            }
        }
        _games.value = currentGames
        context?.let { saveEnabledApps(it) }
    }

    fun toggleOtherApp(id: String, enabled: Boolean) {
        toggleOtherApp(null, id, enabled)
    }

    fun removeGame(context: Context?, id: String) {
        _games.value = _games.value.filter { it.id != id }
        context?.let { saveEnabledApps(it) }
    }

    fun removeGame(id: String) {
        removeGame(null, id)
    }
}
