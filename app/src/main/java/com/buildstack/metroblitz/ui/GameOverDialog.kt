package com.buildstack.metroblitz.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildstack.metroblitz.R
import com.buildstack.metroblitz.core.GameConfig

/**
 * Game over popup showing run score, best score (with NEW BEST! indicator), and Play Again / Home buttons.
 * Renders immediately over the gameplay scene with a touch-consuming scrim (UI-05, UI-06, UI-08, UI-09).
 */
@Composable
fun GameOverDialog(
    score: Int,
    bestScore: Int,
    coins: Int,
    isNewBest: Boolean = false,
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
                .background(Color.White, shape = RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.game_icon),
                contentDescription = "Game Icon",
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(GameConfig.COLOR_CORAL_RED), CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "OOPS!",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = Color(GameConfig.COLOR_CORAL_RED)
            )

            if (isNewBest || (score >= bestScore && score > 0)) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(GameConfig.COLOR_MINT_GREEN),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW BEST!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(GameConfig.COLOR_DEEP_NAVY)
                    )
                }
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🪙 ",
                    fontSize = 16.sp
                )
                Text(
                    text = "Coins: $coins",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(GameConfig.COLOR_ORANGE)
                )
            }

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
