package com.buildstack.metroblitz.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
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
 * Game over popup showing run score, best score, and Play Again / Home buttons.
 * Slides up smoothly (0.25s) when presented (UI-05, UI-06).
 */
@Composable
fun GameOverDialog(
    score: Int,
    bestScore: Int,
    coins: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = tween(durationMillis = 250)
            ) + fadeIn(animationSpec = tween(250))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .padding(32.dp)
            ) {
                Text(
                    text = "OOPS!",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(GameConfig.COLOR_CORAL_RED)
                )

                if (bestScore > 0 && score > bestScore) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NEW BEST!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(GameConfig.COLOR_MINT_GREEN)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Score: $score",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(GameConfig.COLOR_DEEP_NAVY)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Best: $bestScore",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(GameConfig.COLOR_SLATE)
                )

                Text(
                    text = "Coins: $coins",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(GameConfig.COLOR_SUNNY_YELLOW)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SUNNY_YELLOW)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(220.dp).height(54.dp)
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SLATE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(220.dp).height(48.dp)
                ) {
                    Text(
                        text = "Home",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
