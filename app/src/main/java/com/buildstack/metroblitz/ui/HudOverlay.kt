package com.buildstack.metroblitz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
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
 * In-game HUD overlay displaying live score, coins, and pause button (UI-01, UI-07).
 * Uses semi-transparent navy backing pills for high readability across varying background scenery.
 */
@Composable
fun HudOverlay(
    score: Int,
    coins: Int,
    fps: Int,
    speed: Float = GameConfig.START_SPEED,
    distanceMetres: Float = 0.0f,
    activeSegments: Int = GameConfig.SEGMENT_POOL_SIZE,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Top-left: Live Score and Coins with semi-transparent navy pill backing
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score pill
            Box(
                modifier = Modifier
                    .background(
                        color = Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.65f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = "SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$score",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Coins pill
            Box(
                modifier = Modifier
                    .background(
                        color = Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.65f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙 ",
                        fontSize = 20.sp
                    )
                    Column {
                        Text(
                            text = "COINS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(GameConfig.COLOR_SUNNY_YELLOW).copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$coins",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(GameConfig.COLOR_SUNNY_YELLOW)
                        )
                    }
                }
            }
        }

        // Top-right: Pause button (48 dp circle)
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
                .background(Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.65f), shape = CircleShape)
        ) {
            Text(
                text = "||",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
