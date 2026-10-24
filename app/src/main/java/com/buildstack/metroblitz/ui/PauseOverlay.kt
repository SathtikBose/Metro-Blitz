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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildstack.metroblitz.core.GameConfig

/**
 * Pause overlay with Resume, Restart, Home actions, and persisted Sound/Music/Vibration settings toggles.
 */
@Composable
fun PauseOverlay(
    soundOn: Boolean = true,
    musicOn: Boolean = true,
    vibrationOn: Boolean = true,
    onToggleSound: () -> Unit = {},
    onToggleMusic: () -> Unit = {},
    onToggleVibration: () -> Unit = {},
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                modifier = Modifier.width(220.dp).height(50.dp)
            ) {
                Text(
                    text = "Resume",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(GameConfig.COLOR_DEEP_NAVY)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SLATE)),
                modifier = Modifier.width(220.dp).height(46.dp)
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
                modifier = Modifier.width(220.dp).height(46.dp)
            ) {
                Text(
                    text = "Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound, Music, and Vibration settings toggles row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onToggleSound,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (soundOn) Color(GameConfig.COLOR_MINT_GREEN) else Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (soundOn) "SFX: ON" else "SFX: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }

                Button(
                    onClick = onToggleMusic,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (musicOn) Color(GameConfig.COLOR_MINT_GREEN) else Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (musicOn) "BGM: ON" else "BGM: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }

                Button(
                    onClick = onToggleVibration,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vibrationOn) Color(GameConfig.COLOR_MINT_GREEN) else Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (vibrationOn) "VIBE: ON" else "VIBE: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }
            }
        }
    }
}
