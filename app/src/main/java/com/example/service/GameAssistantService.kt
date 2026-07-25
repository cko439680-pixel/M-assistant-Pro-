package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.GameRepository
import com.example.ui.ingame.InGameOverlay
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TouchPassThroughFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var isPanelOpen: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }
    var isDialogOpen: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }
    var interactiveBounds: List<androidx.compose.ui.geometry.Rect> = emptyList()
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isPanelOpen || isDialogOpen) {
            return super.dispatchTouchEvent(ev)
        }

        val x = ev.x
        val y = ev.y

        val margin = 30f
        val hitsWidget = interactiveBounds.any { rect ->
            !rect.isEmpty &&
            x >= (rect.left - margin) && x <= (rect.right + margin) &&
            y >= (rect.top - margin) && y <= (rect.bottom + margin)
        }

        if (hitsWidget) {
            return super.dispatchTouchEvent(ev)
        }

        return false
    }
}

class GameAssistantService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private var windowManager: WindowManager? = null
    private var composeContainer: TouchPassThroughFrameLayout? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private var crosshairContainer: ComposeView? = null
    private var fpsHudContainer: ComposeView? = null
    private var systemStatusContainer: ComposeView? = null
    private var filterOverlayContainer: ComposeView? = null

    private val gameRepository = GameRepository()

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    private var currentYOffset = 0
    private var isPanelCurrentlyOpen = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        } catch (e: Exception) {
            e.printStackTrace()
        }

        gameRepository.initPreferences(this)

        createNotificationChannel()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(
                        NOTIFICATION_ID,
                        createNotification(),
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, createNotification())
                }
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        lifecycleScope.launch {
            gameRepository.isAimAssistOn.collectLatest { active ->
                updateCrosshairOverlay(active)
            }
        }

        lifecycleScope.launch {
            gameRepository.isFpsWidgetOn.collectLatest { active ->
                updateFpsHudOverlay(active)
            }
        }

        lifecycleScope.launch {
            gameRepository.isSystemStatusOn.collectLatest { active ->
                updateSystemStatusOverlay(active)
            }
        }

        lifecycleScope.launch {
            combine(
                GameRepository.isGamerVisionOn,
                GameRepository.selectedFilterType,
                GameRepository.filterIntensity
            ) { active, filterType, intensity ->
                Triple(active, filterType, intensity)
            }.collectLatest { (active, filterType, intensity) ->
                updateGameFilterOverlay(active, filterType, intensity)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Settings.canDrawOverlays(this)) {
            showFloatingOverlay()
        } else {
            removeFloatingOverlay()
        }
        return START_STICKY
    }

    private fun removeFloatingOverlay() {
        composeContainer?.let { view ->
            try {
                windowManager?.removeViewImmediate(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeContainer = null
        }
    }

    private fun showFloatingOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            removeFloatingOverlay()
            return
        }

        if (composeContainer != null) {
            removeFloatingOverlay()
        }

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val density = resources.displayMetrics.density
            val handleWidth = (36 * density).toInt()
            val handleHeight = (120 * density).toInt()
            val screenHeight = resources.displayMetrics.heightPixels

            val params = WindowManager.LayoutParams(
                if (isPanelCurrentlyOpen) WindowManager.LayoutParams.MATCH_PARENT else handleWidth,
                if (isPanelCurrentlyOpen) WindowManager.LayoutParams.MATCH_PARENT else handleHeight,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = if (isPanelCurrentlyOpen) 0 else ((screenHeight / 2) - (handleHeight / 2) + currentYOffset).coerceAtLeast(0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
            windowParams = params

            val composeViewInstance = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@GameAssistantService)
                setViewTreeViewModelStoreOwner(this@GameAssistantService)
                setViewTreeSavedStateRegistryOwner(this@GameAssistantService)

                setContent {
                    MyApplicationTheme {
                        InGameOverlay(
                            repository = gameRepository,
                            onPanelStateChanged = { isOpen ->
                                isPanelCurrentlyOpen = isOpen
                                updateWindowBounds(isOpen, currentYOffset)
                            },
                            onHandleYChanged = { yOffset ->
                                currentYOffset = yOffset
                                if (!isPanelCurrentlyOpen) {
                                    updateWindowBounds(false, yOffset)
                                }
                            },
                            onInteractiveBoundsChanged = { bounds, panelOpen, dialogOpen ->
                                composeContainer?.isPanelOpen = panelOpen
                                composeContainer?.isDialogOpen = dialogOpen
                                composeContainer?.interactiveBounds = bounds
                            }
                        )
                    }
                }
            }

            val container = TouchPassThroughFrameLayout(this).apply {
                setViewTreeLifecycleOwner(this@GameAssistantService)
                setViewTreeViewModelStoreOwner(this@GameAssistantService)
                setViewTreeSavedStateRegistryOwner(this@GameAssistantService)
                addView(composeViewInstance, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            }
            composeContainer = container

            windowManager?.addView(container, params)
        } catch (e: SecurityException) {
            e.printStackTrace()
            composeContainer = null
        } catch (e: Exception) {
            e.printStackTrace()
            composeContainer = null
        }
    }

    private fun updateWindowBounds(isOpen: Boolean, yOffset: Int = 0) {
        if (!Settings.canDrawOverlays(this)) return
        val params = windowParams ?: return
        val view = composeContainer ?: return

        if (isOpen) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 0
        } else {
            val density = resources.displayMetrics.density
            val screenHeight = resources.displayMetrics.heightPixels
            val handleWidth = (36 * density).toInt()
            val handleHeight = (120 * density).toInt()

            params.width = handleWidth
            params.height = handleHeight
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = ((screenHeight / 2) - (handleHeight / 2) + yOffset).coerceAtLeast(0)
        }

        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        try {
            windowManager?.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCrosshairOverlay(enabled: Boolean) {
        if (!enabled || !Settings.canDrawOverlays(this)) {
            crosshairContainer?.let { view ->
                try {
                    windowManager?.removeViewImmediate(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                crosshairContainer = null
            }
            return
        }

        if (crosshairContainer != null) return
        try {
            if (windowManager == null) {
                windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                x = 0
                y = 0
            }

            val view = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@GameAssistantService)
                setViewTreeViewModelStoreOwner(this@GameAssistantService)
                setViewTreeSavedStateRegistryOwner(this@GameAssistantService)

                setContent {
                    MyApplicationTheme {
                        val accentHex by gameRepository.accentColorHex.collectAsState()
                        val themeAccentColor = remember(accentHex) {
                            try { Color(android.graphics.Color.parseColor(accentHex)) } catch (e: Exception) { Color(0xFF22C55E) }
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(60.dp)) {
                                val c = size / 2f
                                drawCircle(color = themeAccentColor, radius = 20.dp.toPx(), style = Stroke(width = 3f))
                                drawCircle(color = Color.Red, radius = 4.dp.toPx())
                                drawLine(color = themeAccentColor, start = Offset(c.width - 25.dp.toPx(), c.height), end = Offset(c.width + 25.dp.toPx(), c.height), strokeWidth = 2f)
                                drawLine(color = themeAccentColor, start = Offset(c.width, c.height - 25.dp.toPx()), end = Offset(c.width, c.height + 25.dp.toPx()), strokeWidth = 2f)
                            }
                        }
                    }
                }
            }
            crosshairContainer = view
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
            crosshairContainer = null
        }
    }

    private fun updateFpsHudOverlay(enabled: Boolean) {
        if (!enabled || !Settings.canDrawOverlays(this)) {
            fpsHudContainer?.let { view ->
                try {
                    windowManager?.removeViewImmediate(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                fpsHudContainer = null
            }
            return
        }

        if (fpsHudContainer != null) return
        try {
            if (windowManager == null) {
                windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = (100 * resources.displayMetrics.density).toInt()
                y = (180 * resources.displayMetrics.density).toInt()
            }

            val view = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@GameAssistantService)
                setViewTreeViewModelStoreOwner(this@GameAssistantService)
                setViewTreeSavedStateRegistryOwner(this@GameAssistantService)

                setContent {
                    MyApplicationTheme {
                        val fps by gameRepository.currentFpsState.collectAsState()
                        val ping by gameRepository.pingMsState.collectAsState()
                        val accentHex by gameRepository.accentColorHex.collectAsState()
                        val themeAccentColor = remember(accentHex) {
                            try { Color(android.graphics.Color.parseColor(accentHex)) } catch (e: Exception) { Color(0xFF22C55E) }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xEE121212))
                                .border(1.dp, themeAccentColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(themeAccentColor)
                                )
                                Text(
                                    "$fps FPS",
                                    color = themeAccentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("|", color = Color.Gray, fontSize = 11.sp)
                                Text("$ping ms", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            fpsHudContainer = view
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
            fpsHudContainer = null
        }
    }

    private fun updateSystemStatusOverlay(enabled: Boolean) {
        if (!enabled || !Settings.canDrawOverlays(this)) {
            systemStatusContainer?.let { view ->
                try {
                    windowManager?.removeViewImmediate(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                systemStatusContainer = null
            }
            return
        }

        if (systemStatusContainer != null) return
        try {
            if (windowManager == null) {
                windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = (60 * resources.displayMetrics.density).toInt()
                y = (60 * resources.displayMetrics.density).toInt()
            }

            val view = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@GameAssistantService)
                setViewTreeViewModelStoreOwner(this@GameAssistantService)
                setViewTreeSavedStateRegistryOwner(this@GameAssistantService)

                setContent {
                    MyApplicationTheme {
                        val fps by gameRepository.currentFpsState.collectAsState()
                        val temp by gameRepository.batteryTempState.collectAsState()
                        val ram by gameRepository.ramUsedPctState.collectAsState()
                        val accentHex by gameRepository.accentColorHex.collectAsState()
                        val themeAccentColor = remember(accentHex) {
                            try { Color(android.graphics.Color.parseColor(accentHex)) } catch (e: Exception) { Color(0xFF22C55E) }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xEE121212))
                                .border(1.5.dp, themeAccentColor, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(themeAccentColor)
                                )
                                Text("$fps FPS", color = themeAccentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("|", color = Color.Gray, fontSize = 12.sp)
                                Text("${temp.toInt()}°C", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("|", color = Color.Gray, fontSize = 12.sp)
                                Text("$ram% RAM", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            systemStatusContainer = view
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
            systemStatusContainer = null
        }
    }

    private fun updateGameFilterOverlay(enabled: Boolean, filterType: String, intensity: Float) {
        if (!enabled || !Settings.canDrawOverlays(this)) {
            filterOverlayContainer?.let { view ->
                try {
                    windowManager?.removeViewImmediate(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                filterOverlayContainer = null
            }
            return
        }

        val filterColor = when (filterType) {
            "Hyper HDR" -> Color(0xFFFFF7ED).copy(alpha = (intensity * 0.12f).coerceIn(0.02f, 0.25f))
            "Ultra HD Clarity", "Ultra HD" -> Color(0xFFFFFBEB).copy(alpha = (intensity * 0.15f).coerceIn(0.02f, 0.28f))
            "Shadow Booster", "Shadows" -> Color(0xFFFEF3C7).copy(alpha = (intensity * 0.18f).coerceIn(0.02f, 0.30f))
            "Night Vision", "Night" -> Color(0xFFFFB000).copy(alpha = (intensity * 0.35f).coerceIn(0.05f, 0.45f))
            "Vivid Cyberpunk", "Cyberpunk", "Cyber" -> Color(0xFFF43F5E).copy(alpha = (intensity * 0.15f).coerceIn(0.02f, 0.25f))
            else -> Color(0xFFFFF7ED).copy(alpha = 0.08f)
        }

        if (filterOverlayContainer != null) {
            filterOverlayContainer?.setContent {
                MyApplicationTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(filterColor)
                    )
                }
            }
            return
        }

        try {
            if (windowManager == null) {
                windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }

            val view = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@GameAssistantService)
                setViewTreeViewModelStoreOwner(this@GameAssistantService)
                setViewTreeSavedStateRegistryOwner(this@GameAssistantService)

                setContent {
                    MyApplicationTheme {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(filterColor)
                        )
                    }
                }
            }
            filterOverlayContainer = view
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
            filterOverlayContainer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        updateCrosshairOverlay(false)
        updateFpsHudOverlay(false)
        updateSystemStatusOverlay(false)
        updateGameFilterOverlay(false, "", 0f)

        if (composeContainer != null) {
            try {
                windowManager?.removeView(composeContainer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeContainer = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "M Assistant Game Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Game Floating Assistant Overlay Service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("M Assistant Active")
            .setContentText("Game Floating Panel Service Running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "m_assistant_overlay_channel"
        private const val NOTIFICATION_ID = 7772
    }
}
