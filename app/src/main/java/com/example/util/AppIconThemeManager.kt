package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.example.R
import com.example.data.GameRepository

data class AppIconVariant(
    val key: String,
    val title: String,
    val subtitle: String,
    val aliasName: String,
    val accentHex: String,
    val primaryColorHex: String,
    val previewDrawableRes: Int
)

object AppIconThemeManager {

    val VARIANTS = listOf(
        AppIconVariant(
            key = "yellow",
            title = "Default Yellow",
            subtitle = "Classic Amber Hexagon Logo",
            aliasName = "com.android.game.MainActivity",
            accentHex = "#EAB308",
            primaryColorHex = "#FFC800",
            previewDrawableRes = R.drawable.ic_launcher_foreground
        ),
        AppIconVariant(
            key = "green",
            title = "Neon Green Accent",
            subtitle = "Electric Gaming Hexagon Logo",
            aliasName = "com.android.game.MainActivityGreen",
            accentHex = "#22C55E",
            primaryColorHex = "#22C55E",
            previewDrawableRes = R.drawable.ic_launcher_foreground_green
        ),
        AppIconVariant(
            key = "cyan",
            title = "Cyberpunk Blue",
            subtitle = "Neon Sci-Fi Cyan Hexagon Logo",
            aliasName = "com.android.game.MainActivityCyan",
            accentHex = "#06B6D4",
            primaryColorHex = "#06B6D4",
            previewDrawableRes = R.drawable.ic_launcher_foreground_cyan
        ),
        AppIconVariant(
            key = "black",
            title = "Dark Stealth Black",
            subtitle = "Midnight Stealth Hexagon Logo",
            aliasName = "com.android.game.MainActivityBlack",
            accentHex = "#A855F7",
            primaryColorHex = "#262626",
            previewDrawableRes = R.drawable.ic_launcher_foreground_black
        ),
        AppIconVariant(
            key = "red",
            title = "Crimson Red",
            subtitle = "Championship Crimson Hexagon Logo",
            aliasName = "com.android.game.MainActivityRed",
            accentHex = "#EF4444",
            primaryColorHex = "#EF4444",
            previewDrawableRes = R.drawable.ic_launcher_foreground_red
        )
    )

    fun getCurrentVariantKey(context: Context): String {
        val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
        return prefs.getString("selected_app_icon_key", "yellow") ?: "yellow"
    }

    fun applyAppIconAndTheme(context: Context, variant: AppIconVariant, repository: GameRepository) {
        val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_app_icon_key", variant.key).apply()

        // Update Theme Accent Color in Repository
        repository.updateAccentColor(context, variant.accentHex)

        // Enable component setting for active alias and disable others
        val pm = context.packageManager
        val packageName = context.packageName

        VARIANTS.forEach { item ->
            val isSelected = item.key == variant.key
            val state = if (isSelected) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                val compName = ComponentName(packageName, item.aliasName)
                pm.setComponentEnabledSetting(
                    compName,
                    state,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Toast.makeText(
            context,
            "App icon changed successfully! (Home screen will update shortly)",
            Toast.LENGTH_LONG
        ).show()
    }
}
