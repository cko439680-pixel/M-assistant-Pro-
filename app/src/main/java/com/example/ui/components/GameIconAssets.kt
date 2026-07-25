package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameIconBadge(
    gameId: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val context = LocalContext.current
    val cornerShape = RoundedCornerShape(12.dp)

    // Attempt to load real system package icon if gameId is a package name
    val realAppIcon = remember(gameId) {
        try {
            if (gameId.contains(".")) {
                context.packageManager.getApplicationIcon(gameId)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    if (realAppIcon != null) {
        val bitmap = remember(realAppIcon) {
            try {
                if (realAppIcon is BitmapDrawable) {
                    realAppIcon.bitmap?.asImageBitmap()
                } else {
                    val w = realAppIcon.intrinsicWidth.coerceAtLeast(1)
                    val h = realAppIcon.intrinsicHeight.coerceAtLeast(1)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    realAppIcon.setBounds(0, 0, canvas.width, canvas.height)
                    realAppIcon.draw(canvas)
                    bmp.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(cornerShape)
            )
            return
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(cornerShape),
        contentAlignment = Alignment.Center
    ) {
        when (gameId) {
            "bgmi" -> {
                // Battlegrounds India
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C3E50), Color(0xFF000000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .background(Color(0xFFE67E22), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BGMI",
                            color = Color.White,
                            fontSize = (size.value * 0.28).sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            "freefire" -> {
                // Free Fire MAX
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFF5722), Color(0xFF3E2723))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FREE\nFIRE",
                        color = Color(0xFFFFD54F),
                        fontSize = (size.value * 0.22).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (size.value * 0.24).sp
                    )
                }
            }
            "hillclimb" -> {
                // Hill Climb Racing 2
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF29B6F6), Color(0xFF0277BD))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HILL\nCLIMB 2",
                        color = Color.Yellow,
                        fontSize = (size.value * 0.20).sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = (size.value * 0.22).sp
                    )
                }
            }
            "minecraft" -> {
                // Minecraft
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF4CAF50), Color(0xFF3E2723))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(size * 0.5f)
                            .background(Color(0xFF795548), RoundedCornerShape(2.dp))
                    )
                }
            }
            "aistudio" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E88E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Studio",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            "mcpedl" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2E7D32)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DL",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.4).sp
                    )
                }
            }
            "cleaner" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Cleaner",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            "phonepe" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF5E35B1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "पे",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.45).sp
                    )
                }
            }
            "spotify" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1DB954)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Spotify",
                        tint = Color.Black,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            "styles" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFD81B60)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Styles",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            "yonosbi" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF00ACC1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "yono\nSBI",
                        color = Color.White,
                        fontSize = (size.value * 0.20).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            "zarchiver" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF33691E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Z",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.5).sp
                    )
                }
            }
            "game_assistant" -> {
                // Game Assistant emblem
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121814)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(size * 0.7f)
                            .background(Color(0xFF22C55E), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Games,
                            contentDescription = "Game Assistant",
                            tint = Color.Black,
                            modifier = Modifier.size(size * 0.4f)
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Games,
                        contentDescription = "Game",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.5f)
                    )
                }
            }
        }
    }
}
