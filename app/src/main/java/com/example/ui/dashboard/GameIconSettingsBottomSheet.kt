package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameIconMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameIconSettingsBottomSheet(
    currentMode: GameIconMode,
    onModeSelected: (GameIconMode) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Flat Mode Card
                val isFlat = currentMode == GameIconMode.FLAT_MODE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2B2B2B))
                        .border(
                            width = 2.dp,
                            color = if (isFlat) Color(0xFF22C55E) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onModeSelected(GameIconMode.FLAT_MODE) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Flat mode",
                        color = if (isFlat) Color(0xFF22C55E) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Shows game icons directly on the Home screen",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                // Organise Mode Card
                val isOrganise = currentMode == GameIconMode.ORGANIZE_MODE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2B2B2B))
                        .border(
                            width = 2.dp,
                            color = if (isOrganise) Color(0xFF22C55E) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onModeSelected(GameIconMode.ORGANIZE_MODE) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Organise game...",
                        color = if (isOrganise) Color(0xFF22C55E) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hides game icons from the Home screen",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "The Game Assistant icon will be added to the Home screen.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
