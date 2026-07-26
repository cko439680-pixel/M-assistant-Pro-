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
            key = "DEFAULT",
            title = "Default Yellow",
            subtitle = "Classic Amber Hexagon Logo",
            aliasName = "MainActivityDefault",
            accentHex = "#EAB308",
            primaryColorHex = "#FFC800",
            previewDrawableRes = R.drawable.ic_launcher_foreground
        ),
        AppIconVariant(
            key = "NEON_GREEN",
            title = "Neon Green Accent",
            subtitle = "Electric Gaming Hexagon Logo",
            aliasName = "MainActivityNeonGreen",
            accentHex = "#22C55E",
            primaryColorHex = "#22C55E",
            previewDrawableRes = R.drawable.ic_launcher_foreground_green
        ),
        AppIconVariant(
            key = "CYBERPUNK_BLUE",
            title = "Cyberpunk Blue",
            subtitle = "Neon Sci-Fi Cyan Hexagon Logo",
            aliasName = "MainActivityCyberpunkBlue",
            accentHex = "#06B6D4",
            primaryColorHex = "#06B6D4",
            previewDrawableRes = R.drawable.ic_launcher_foreground_cyan
        ),
        AppIconVariant(
            key = "DARK_STEALTH",
            title = "Dark Stealth Black",
            subtitle = "Midnight Stealth Hexagon Logo",
            aliasName = "MainActivityDarkStealth",
            accentHex = "#A855F7",
            primaryColorHex = "#262626",
            previewDrawableRes = R.drawable.ic_launcher_foreground_black
        ),
        AppIconVariant(
            key = "CRIMSON_RED",
            title = "Crimson Red",
            subtitle = "Championship Crimson Hexagon Logo",
            aliasName = "MainActivityCrimsonRed",
            accentHex = "#EF4444",
            primaryColorHex = "#EF4444",
            previewDrawableRes = R.drawable.ic_launcher_foreground_red
        )
    )

    fun getCurrentVariantKey(context: Context): String {
        val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("selected_app_icon_key", "DEFAULT") ?: "DEFAULT"
        return when (saved.uppercase()) {
            "YELLOW", "DEFAULT" -> "DEFAULT"
            "GREEN", "NEON_GREEN" -> "NEON_GREEN"
            "CYAN", "CYBERPUNK_BLUE" -> "CYBERPUNK_BLUE"
            "BLACK", "DARK_STEALTH" -> "DARK_STEALTH"
            "RED", "CRIMSON_RED" -> "CRIMSON_RED"
            else -> "DEFAULT"
        }
    }

    fun changeAppIcon(context: Context, targetAlias: String) {
        val pm = context.packageManager
        val packageName = context.packageName
        val aliases = listOf(
            "$packageName.MainActivityDefault",
            "$packageName.MainActivityNeonGreen",
            "$packageName.MainActivityCyberpunkBlue",
            "$packageName.MainActivityDarkStealth",
            "$packageName.MainActivityCrimsonRed"
        )

        for (alias in aliases) {
            val state = if (alias.endsWith(targetAlias)) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    ComponentName(context, alias),
                    state,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun applyAppIconAndTheme(context: Context, variant: AppIconVariant, repository: GameRepository) {
        val prefs = context.getSharedPreferences("game_assistant_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_app_icon_key", variant.key).apply()

        // Update Theme Accent Color in Repository
        repository.updateAccentColor(context, variant.accentHex)

        // Switch activity alias for icon
        changeAppIcon(context, variant.aliasName)

        Toast.makeText(
            context,
            "App icon updated successfully",
            Toast.LENGTH_SHORT
        ).show()
    }
}
