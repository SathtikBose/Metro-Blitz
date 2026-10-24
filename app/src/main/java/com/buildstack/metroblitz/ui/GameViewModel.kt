package com.buildstack.metroblitz.ui

import androidx.lifecycle.ViewModel
import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.GameSnapshot
import com.buildstack.metroblitz.core.GameState
import com.buildstack.metroblitz.core.GameWorld
import com.buildstack.metroblitz.input.Command
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI State exposed to Jetpack Compose overlays.
 */
data class UiState(
    val gameState: GameState = GameState.MENU,
    val score: Int = 0,
    val bestScore: Int = 0,
    val coins: Int = 0,
    val totalCoins: Int = 0,
    val speed: Float = GameConfig.START_SPEED,
    val distanceMetres: Float = 0.0f,
    val activeSegments: Int = GameConfig.SEGMENT_POOL_SIZE,
    val countdownSeconds: Int = 3,
    val fps: Int = 60,
    val isInvincible: Boolean = false
)

/**
 * Main game ViewModel managing state transitions, input commands, and UI state publication.
 */
class GameViewModel : ViewModel() {
    val world = GameWorld()

    private val _uiState = MutableStateFlow(UiState(gameState = GameState.MENU))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onCommand(command: Command) {
        when (command) {
            Command.TAP -> {
                if (_uiState.value.gameState == GameState.MENU) {
                    world.startPlaying()
                    _uiState.value = _uiState.value.copy(gameState = GameState.PLAYING)
                }
            }
            Command.LEFT -> world.onLeft()
            Command.RIGHT -> world.onRight()
            Command.JUMP -> world.onJump()
            Command.SLIDE -> world.onSlide()
        }
    }

    fun onPauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING || _uiState.value.gameState == GameState.RESUME_COUNTDOWN) {
            world.pause()
            _uiState.value = _uiState.value.copy(gameState = GameState.PAUSED)
        }
    }

    fun onResumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            world.resumePlaying()
            _uiState.value = _uiState.value.copy(gameState = GameState.PLAYING)
        }
    }

    fun onRestartGame() {
        world.reset()
        _uiState.value = _uiState.value.copy(
            gameState = GameState.PLAYING,
            score = 0,
            coins = 0,
            speed = GameConfig.START_SPEED
        )
    }

    fun onHome() {
        world.toMenu()
        _uiState.value = _uiState.value.copy(gameState = GameState.MENU)
    }

    fun onFrame(rawDt: Float, currentFps: Int = 60) {
        world.update(rawDt)
        val snapshot = world.snapshot()
        if (snapshot.score != _uiState.value.score ||
            snapshot.state != _uiState.value.gameState ||
            currentFps != _uiState.value.fps ||
            snapshot.distanceMetres.toInt() != _uiState.value.distanceMetres.toInt()
        ) {
            _uiState.value = _uiState.value.copy(
                score = snapshot.score,
                coins = snapshot.coins,
                speed = snapshot.speed,
                distanceMetres = snapshot.distanceMetres,
                activeSegments = world.trackSystem.segments.size,
                gameState = snapshot.state,
                fps = currentFps
            )
        }
    }
}
