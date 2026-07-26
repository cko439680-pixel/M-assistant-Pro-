package com.example.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.ui.components.LiquidGlassCard
import com.example.util.AppIconThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppIconThemeScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedKey by remember { mutableStateOf(AppIconThemeManager.getCurrentVariantKey(context)) }

    val blurIntensity by repository.blurIntensity.collectAsState()
    val liquidGlassOpacity by repository.liquidGlassOpacity.collectAsState()
    val liquidGlassSpecular by repository.liquidGlassSpecular.collectAsState()
    val activeAccentHex by repository.accentColorHex.collectAsState()

    val currentAccentColor = remember(activeAccentHex) {
        try { Color(android.graphics.Color.parseColor(activeAccentHex)) }
        catch (e: Exception) { Color(0xFF22C55E) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Icon & Theme Changer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Header Card
            LiquidGlassCard(
                blurIntensity = blurIntensity,
                liquidGlassOpacity = liquidGlassOpacity,
                liquidGlassSpecular = liquidGlassSpecular,
                glowingBorder = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        currentAccentColor.copy(alpha = 0.3f),
                                        Color(0xFF1E1E24)
                                    )
                                )
                            )
                            .border(1.dp, currentAccentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentAccentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CUSTOMIZE APPEARANCE",
                            color = currentAccentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home Screen Launcher & Theme",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Select your preferred app icon variant below. The selected icon and matching accent theme will be saved and applied immediately.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Text(
                text = "AVAILABLE ICON VARIANTS",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Icon Variant List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppIconThemeManager.VARIANTS.forEach { variant ->
                    val isSelected = variant.key == selectedKey

                    val animatedBorderColor by animateColorAsState(
                        targetValue = if (isSelected) Color(android.graphics.Color.parseColor(variant.accentHex)) else Color(0xFF2C2C2C),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "borderColor"
                    )

                    val animatedBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(android.graphics.Color.parseColor(variant.accentHex)).copy(alpha = 0.15f) else Color(0xFF18181C),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "bgColor"
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = animatedBorderColor,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                selectedKey = variant.key
                                AppIconThemeManager.applyAppIconAndTheme(context, variant, repository)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Preview Box
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF101012))
                                    .border(
                                        1.dp,
                                        Color(android.graphics.Color.parseColor(variant.accentHex)).copy(alpha = 0.5f),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = variant.previewDrawableRes),
                                    contentDescription = variant.title,
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = variant.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = variant.subtitle,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(variant.accentHex))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


