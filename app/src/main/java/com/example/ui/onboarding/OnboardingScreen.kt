package com.example.ui.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val pageType: Int
)

@Composable
fun OnboardingScreen(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val features = remember {
        listOf(
            OnboardingFeature(
                title = "Game Assistant",
                subtitle = "Use gaming tools during gameplay.",
                icon = Icons.Default.GridOn,
                pageType = 0
            ),
            OnboardingFeature(
                title = "Game Assistant on the Home screen",
                subtitle = "Claim game privileges and organise game icons.",
                icon = Icons.Default.Home,
                pageType = 1
            ),
            OnboardingFeature(
                title = "Floating Window",
                subtitle = "Chat while playing",
                icon = Icons.Default.ChatBubble,
                pageType = 2
            ),
            OnboardingFeature(
                title = "Mistouch prevention",
                subtitle = "Stay immersed in your game",
                icon = Icons.Default.Security,
                pageType = 3
            ),
            OnboardingFeature(
                title = "Network acceleration",
                subtitle = "Say goodbye to game lag",
                icon = Icons.Default.Language,
                pageType = 4
            ),
            OnboardingFeature(
                title = "Bullet notifications",
                subtitle = "Never miss a message while you are playing",
                icon = Icons.Default.Notifications,
                pageType = 5
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { features.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Welcome to Game Assistant.",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Feature Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF202020))
                        .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) { page ->
                            val item = features[page]
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Header row inside card
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E5A1E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = item.subtitle,
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Feature Graphics Canvas Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF161616))
                                        .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FeatureGraphicMockup(pageType = item.pageType)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dot Indicators
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            features.indices.forEach { index ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isSelected) 7.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White else Color(0xFF666666)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Section: Privacy Text & Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Privacy Notice Text with Hyperlinks
                val annotatedString = buildAnnotatedString {
                    append("\"Game Assistant\" is an app that provides game-related content and improves your gaming experience.\n\nFor details, refer to the ")

                    pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                    withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.Medium)) {
                        append("Privacy Notice for Game Assistant")
                    }
                    pop()

                    append(" and ")

                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                    withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.Medium)) {
                        append("User Agreement")
                    }
                    pop()

                    append(".")
                }

                ClickableText(
                    text = annotatedString,
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                            .firstOrNull()?.let {
                                showPrivacyDialog = true
                            }
                        annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                            .firstOrNull()?.let {
                                showTermsDialog = true
                            }
                    }
                )

                // Agree and Continue Button
                Button(
                    onClick = { onAgree() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF66BB6A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Agree and continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Disagree Button
                Button(
                    onClick = { onDisagree() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Disagree",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs for Privacy and Terms
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Privacy Notice", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "M Assistant Pro values your privacy. Preferences and game profiles are strictly stored locally on your device. We do not collect or sell personal telemetry data.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = Color(0xFF66BB6A))
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("User Agreement", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "By using Game Assistant, you agree to the software usage guidelines. System overlay tools are designed to enhance gameplay metrics without modifying game code.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close", color = Color(0xFF66BB6A))
                }
            }
        )
    }
}

@Composable
private fun FeatureGraphicMockup(pageType: Int) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val w = size.width
        val h = size.height

        when (pageType) {
            0 -> {
                // Game Assistant grid layout mockup
                val cardW = w * 0.45f
                val cardH = h * 0.85f
                val left = 12.dp.toPx()
                val top = (h - cardH) / 2

                drawRoundRect(
                    color = Color(0xFF282828),
                    topLeft = Offset(left, top),
                    size = Size(cardW, cardH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                )

                // Grid placeholders inside card
                val gap = 8.dp.toPx()
                val boxSize = (cardW - gap * 4) / 3
                for (row in 0..2) {
                    for (col in 0..2) {
                        val bx = left + gap + col * (boxSize + gap)
                        val by = top + gap + row * (boxSize + gap)
                        drawRoundRect(
                            color = Color(0xFF383838),
                            topLeft = Offset(bx, by),
                            size = Size(boxSize, boxSize),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }
            1 -> {
                // Home screen widget & apps layout mockup
                val phoneW = w * 0.5f
                val phoneH = h * 0.9f
                val phoneLeft = (w - phoneW) / 2
                val phoneTop = (h - phoneH) / 2

                drawRoundRect(
                    color = Color(0xFF252525),
                    topLeft = Offset(phoneLeft, phoneTop),
                    size = Size(phoneW, phoneH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Top widget
                drawRoundRect(
                    color = Color(0xFF353535),
                    topLeft = Offset(phoneLeft + 12.dp.toPx(), phoneTop + 12.dp.toPx()),
                    size = Size(phoneW - 24.dp.toPx(), 36.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
            }
            2 -> {
                // Floating chat window layout mockup
                val floatW = w * 0.42f
                val floatH = h * 0.85f
                val floatLeft = 12.dp.toPx()
                val floatTop = (h - floatH) / 2

                drawRoundRect(
                    color = Color(0xFF282828),
                    topLeft = Offset(floatLeft, floatTop),
                    size = Size(floatW, floatH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )

                // Chat bubble lines
                drawRoundRect(
                    color = Color(0xFF3A3A3A),
                    topLeft = Offset(floatLeft + 8.dp.toPx(), floatTop + 12.dp.toPx()),
                    size = Size(floatW - 24.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF2E5A1E),
                    topLeft = Offset(floatLeft + 16.dp.toPx(), floatTop + 36.dp.toPx()),
                    size = Size(floatW - 24.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
            }
            3 -> {
                // Mistouch prevention green edge glow
                val glowBrush = Brush.radialGradient(
                    colors = listOf(Color(0x8866BB6A), Color.Transparent),
                    center = Offset(w / 2, h / 2),
                    radius = w * 0.7f
                )
                drawRect(brush = glowBrush)
                drawRoundRect(
                    color = Color(0xFF66BB6A),
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            4 -> {
                // Network acceleration speed gauge ring
                val center = Offset(w / 2, h / 2)
                val radius = Math.min(w, h) * 0.35f

                drawCircle(
                    color = Color(0xFF2C2C2C),
                    center = center,
                    radius = radius,
                    style = Stroke(width = 8.dp.toPx())
                )
                drawArc(
                    color = Color(0xFF66BB6A),
                    startAngle = 135f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            5 -> {
                // Bullet notifications floating stream banner mockup
                val barH = 22.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF282828),
                    topLeft = Offset(10.dp.toPx(), h * 0.25f),
                    size = Size(w * 0.6f, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(11.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF66BB6A),
                    center = Offset(21.dp.toPx(), h * 0.25f + barH / 2),
                    radius = 6.dp.toPx()
                )

                drawRoundRect(
                    color = Color(0xFF282828),
                    topLeft = Offset(w * 0.3f, h * 0.55f),
                    size = Size(w * 0.65f, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(11.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF66BB6A),
                    center = Offset(w * 0.3f + 11.dp.toPx(), h * 0.55f + barH / 2),
                    radius = 6.dp.toPx()
                )
            }
        }
    }
}
