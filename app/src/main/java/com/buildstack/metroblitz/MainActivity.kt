package com.buildstack.metroblitz

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.buildstack.metroblitz.core.GameState
import com.buildstack.metroblitz.render.GameScene
import com.buildstack.metroblitz.theme.MetroBlitzTheme
import com.buildstack.metroblitz.ui.CountdownOverlay
import com.buildstack.metroblitz.ui.GameOverDialog
import com.buildstack.metroblitz.ui.GameViewModel
import com.buildstack.metroblitz.ui.HudOverlay
import com.buildstack.metroblitz.ui.MenuOverlay
import com.buildstack.metroblitz.ui.PauseOverlay

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while playing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Immersive full-screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MetroBlitzTheme {
                GameScreen(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseGame()
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GameScene(
            onFrame = { dt, fps ->
                viewModel.onFrame(dt, fps)
            }
        )

        when (uiState.gameState) {
            GameState.LOADING, GameState.MENU -> {
                MenuOverlay(
                    bestScore = uiState.bestScore,
                    totalCoins = uiState.totalCoins
                )
            }
            GameState.PLAYING -> {
                HudOverlay(
                    score = uiState.score,
                    coins = uiState.coins,
                    fps = uiState.fps,
                    onPauseClick = { viewModel.onPauseGame() }
                )
            }
            GameState.PAUSED -> {
                PauseOverlay(
                    onResume = { viewModel.onResumeGame() },
                    onRestart = { viewModel.onRestartGame() },
                    onHome = { viewModel.onHome() }
                )
            }
            GameState.RESUME_COUNTDOWN -> {
                CountdownOverlay(secondsRemaining = uiState.countdownSeconds)
            }
            GameState.GAME_OVER -> {
                GameOverDialog(
                    score = uiState.score,
                    bestScore = uiState.bestScore,
                    coins = uiState.coins,
                    onPlayAgain = { viewModel.onRestartGame() },
                    onHome = { viewModel.onHome() }
                )
            }
            GameState.HIT -> {
                // Transient state handled in Phase 6
            }
        }
    }
}
