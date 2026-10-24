package com.buildstack.metroblitz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildstack.metroblitz.core.GameConfig

/**
 * Pause overlay with Resume, Restart, Home actions, and sound/vibration toggle stubs.
 */
@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(32.dp)
        ) {
            Text(
                text = "PAUSED",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(GameConfig.COLOR_DEEP_NAVY)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SUNNY_YELLOW)),
                modifier = Modifier.width(220.dp)
            ) {
                Text(
                    text = "Resume",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(GameConfig.COLOR_DEEP_NAVY)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SLATE)),
                modifier = Modifier.width(220.dp)
            ) {
                Text(
                    text = "Restart",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SLATE)),
                modifier = Modifier.width(220.dp)
            ) {
                Text(
                    text = "Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound and Vibration toggle stubs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { soundOn = !soundOn },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (soundOn) Color(GameConfig.COLOR_MINT_GREEN) else Color.LightGray
                    )
                ) {
                    Text(
                        text = if (soundOn) "Sound: ON" else "Sound: OFF",
                        fontSize = 12.sp,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }

                Button(
                    onClick = { vibrationOn = !vibrationOn },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vibrationOn) Color(GameConfig.COLOR_MINT_GREEN) else Color.LightGray
                    )
                ) {
                    Text(
                        text = if (vibrationOn) "Vibe: ON" else "Vibe: OFF",
                        fontSize = 12.sp,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }
            }
        }
    }
}
