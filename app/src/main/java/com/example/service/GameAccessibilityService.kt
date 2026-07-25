package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.data.GameRepository

class GameAccessibilityService : AccessibilityService() {

    private var lastBackPressTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Required callback for AccessibilityService
    }

    override fun onInterrupt() {
        isServiceConnected = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceConnected = false
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        val isMistouchActive = GameRepository.isMistouchActive.value
        if (isMistouchActive && event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 1200) {
                    lastBackPressTime = 0
                    Toast.makeText(applicationContext, "Gesture allowed", Toast.LENGTH_SHORT).show()
                    return false
                } else {
                    lastBackPressTime = now
                    Toast.makeText(
                        applicationContext,
                        "🔒 Mistouch Protection Active: Press or swipe twice to exit game",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    companion object {
        var isServiceConnected: Boolean = false
    }
}
