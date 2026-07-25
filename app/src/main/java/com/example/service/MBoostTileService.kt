package com.example.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.example.data.GameRepository
import com.example.data.PerformanceMode

class MBoostTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val currentMode = GameRepository.currentPerformanceMode.value

        val newMode = if (currentMode == PerformanceMode.PRO_GAMER) {
            PerformanceMode.BALANCED
        } else {
            PerformanceMode.PRO_GAMER
        }

        GameRepository.currentPerformanceMode.value = newMode

        // Trigger vibration feedback
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateTileState()

        val toastMsg = if (newMode == PerformanceMode.PRO_GAMER) {
            "⚡ M Assistant Pro Boosted! Championship Mode Engaged!"
        } else {
            "⚡ M Assistant Pro: Balanced Mode Active"
        }
        Toast.makeText(applicationContext, toastMsg, Toast.LENGTH_SHORT).show()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val currentMode = GameRepository.currentPerformanceMode.value
        val isActive = currentMode == PerformanceMode.PRO_GAMER

        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "M Assistant Pro"
        tile.subtitle = if (isActive) "Championship Boost" else "Tap to Boost"
        tile.updateTile()
    }
}
