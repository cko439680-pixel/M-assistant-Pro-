package com.example.data

data class GameItem(
    val id: String,
    val title: String,
    val packageName: String,
    val timePlayedText: String,
    val playTimeMinutes: Int,
    val lastPlayedAgo: String,
    val isEnabled: Boolean = true,
    val isRecommended: Boolean = true
)

data class GameAlbumEntry(
    val gameId: String,
    val gameTitle: String,
    val dateText: String,
    val mediaCount: Int = 0
)

data class GameMediaItem(
    val id: String,
    val filePath: String,
    val fileName: String,
    val isVideo: Boolean,
    val gameTitle: String,
    val dateText: String,
    val timestamp: Long,
    val durationSeconds: Int = 0
)

enum class GameIconMode {
    FLAT_MODE,
    ORGANIZE_MODE
}

enum class PerformanceMode {
    POWER_SAVING,
    BALANCED,
    PRO_GAMER
}
