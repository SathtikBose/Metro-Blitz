package com.buildstack.metroblitz

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.GameState
import com.buildstack.metroblitz.input.Command
import com.buildstack.metroblitz.input.DebugKeyInput
import com.buildstack.metroblitz.input.SwipeDetector
import com.buildstack.metroblitz.render.GameScene
import com.buildstack.metroblitz.theme.MetroBlitzTheme
import com.buildstack.metroblitz.ui.CountdownOverlay
import com.buildstack.metroblitz.ui.GameOverDialog
import com.buildstack.metroblitz.ui.GameViewModel
import com.buildstack.metroblitz.ui.HudOverlay
import com.buildstack.metroblitz.ui.MenuOverlay
import com.buildstack.metroblitz.ui.ModelViewerScreen
import com.buildstack.metroblitz.ui.PauseOverlay

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private lateinit var swipeDetector: SwipeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        swipeDetector = SwipeDetector(thresholdPx = 48f * density) { command ->
            viewModel.onCommand(command)
        }

        // Keep screen on while playing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Immersive full-screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MetroBlitzTheme {
                var showModelViewer by remember { mutableStateOf(false) }

                if (showModelViewer && BuildConfig.DEBUG) {
                    ModelViewerScreen(onBack = { showModelViewer = false })
                } else {
                    GameScreen(
                        viewModel = viewModel,
                        onOpenModelViewer = { showModelViewer = true }
                    )
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        swipeDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (BuildConfig.DEBUG) {
            val cmd = DebugKeyInput.onKeyDown(keyCode)
            if (cmd != null) {
                viewModel.onCommand(cmd)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            viewModel.onPauseGame()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseGame()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onPauseGame()
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onOpenModelViewer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GameScene(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )

        when (uiState.gameState) {
            GameState.LOADING, GameState.MENU -> {
                MenuOverlay(
                    bestScore = uiState.bestScore,
                    totalCoins = uiState.totalCoins,
                    onTap = { viewModel.onCommand(Command.TAP) }
                )
            }
            GameState.PLAYING -> {
                HudOverlay(
                    score = uiState.score,
                    coins = uiState.coins,
                    fps = uiState.fps,
                    speed = uiState.speed,
                    distanceMetres = uiState.distanceMetres,
                    activeSegments = uiState.activeSegments,
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
                    isNewBest = uiState.isNewBest,
                    onPlayAgain = { viewModel.onRestartGame() },
                    onHome = { viewModel.onHome() }
                )
            }
            GameState.HIT -> {
                // Transient state handled in Phase 6
            }
        }

        // Debug Model Viewer shortcut button
        if (BuildConfig.DEBUG && (uiState.gameState == GameState.MENU || uiState.gameState == GameState.PAUSED)) {
            Button(
                onClick = onOpenModelViewer,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_VIOLET)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Text(text = "DEBUG: 3D VIEWER", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}
