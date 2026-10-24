package com.buildstack.metroblitz.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildstack.metroblitz.core.GameConfig

/**
 * Game over popup showing run score, best score, and Play Again / Home buttons.
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
                text = "OOPS!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color(GameConfig.COLOR_CORAL_RED)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Score: $score",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(GameConfig.COLOR_DEEP_NAVY)
            )

            Text(
                text = "Best: $bestScore",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(GameConfig.COLOR_SLATE)
            )

            Text(
                text = "Coins: $coins",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(GameConfig.COLOR_SUNNY_YELLOW)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_SUNNY_YELLOW)),
                modifier = Modifier.width(220.dp)
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
                modifier = Modifier.width(220.dp)
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
