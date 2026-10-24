package com.buildstack.metroblitz.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildstack.metroblitz.core.GameConfig

/**
 * Menu overlay showing game title and pulsing "TAP TO PLAY".
 */
@Composable
fun MenuOverlay(
    bestScore: Int,
    totalCoins: Int,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onTap() }
            .padding(24.dp)
    ) {
        Text(
            text = "METRO BLITZ",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
        )

        Text(
            text = "TAP TO PLAY",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(GameConfig.COLOR_SUNNY_YELLOW),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp).scale(scale)
        )

        Text(
            text = "BEST $bestScore",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 24.dp)
        )

        Text(
            text = "$totalCoins COINS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(GameConfig.COLOR_SUNNY_YELLOW),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp)
        )
    }
}
