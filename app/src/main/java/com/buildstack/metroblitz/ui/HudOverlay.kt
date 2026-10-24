package com.buildstack.metroblitz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
 * In-game HUD overlay displaying live score, coins, and pause button.
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
        Column(modifier = Modifier.align(Alignment.TopStart).padding(top = 16.dp)) {
            Text(
                text = "SCORE: $score",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "COINS: $coins",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(GameConfig.COLOR_SUNNY_YELLOW)
            )
        }

        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
                .background(Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.6f), shape = CircleShape)
        ) {
            Text(
                text = "||",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "FPS: $fps | SPD: ${"%.1f".format(speed)} m/s",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "DIST: ${distanceMetres.toInt()}m | SEGS: $activeSegments",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
