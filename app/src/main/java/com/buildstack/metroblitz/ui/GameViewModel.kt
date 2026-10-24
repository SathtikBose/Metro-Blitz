package com.buildstack.metroblitz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.metroblitz.GameApp
import com.buildstack.metroblitz.audio.SoundManager
import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.GameSnapshot
import com.buildstack.metroblitz.core.GameState
import com.buildstack.metroblitz.core.GameWorld
import com.buildstack.metroblitz.data.FakeScoreRepository
import com.buildstack.metroblitz.data.ScoreRepository
import com.buildstack.metroblitz.data.SettingsRepository
import com.buildstack.metroblitz.input.Command
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val isInvincible: Boolean = false,
    val isNewBest: Boolean = false,
    val soundOn: Boolean = true,
    val musicOn: Boolean = true,
    val vibrationOn: Boolean = true
)

/**
 * Main game ViewModel managing state transitions, input commands, and UI state publication.
 */
class GameViewModel(
    val scoreRepository: ScoreRepository = try {
        GameApp.instance.scoreRepository
    } catch (_: Throwable) {
        FakeScoreRepository()
    },
    val settingsRepository: SettingsRepository? = try {
        GameApp.instance.settingsRepository
    } catch (_: Throwable) {
        null
    },
    val soundManager: SoundManager? = try {
        GameApp.instance.soundManager
    } catch (_: Throwable) {
        null
    },
    val hapticHelper: com.buildstack.metroblitz.util.HapticHelper? = try {
        GameApp.instance.hapticHelper
    } catch (_: Throwable) {
        null
    }
) : ViewModel() {
    val world = GameWorld()

    private val _uiState = MutableStateFlow(UiState(gameState = GameState.MENU))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var previousState: GameState = GameState.MENU
    private var previousCoins: Int = 0
    private var previousCountdownSecs: Int = 3

    init {
        viewModelScope.launch {
            scoreRepository.bestScore.collect { best ->
                _uiState.value = _uiState.value.copy(bestScore = best)
            }
        }
        viewModelScope.launch {
            scoreRepository.totalCoins.collect { total ->
                _uiState.value = _uiState.value.copy(totalCoins = total)
            }
        }

        settingsRepository?.let { repo ->
            viewModelScope.launch {
                repo.soundOn.collect { on ->
                    _uiState.value = _uiState.value.copy(soundOn = on)
                    soundManager?.soundEnabled = on
                }
            }
            viewModelScope.launch {
                repo.musicOn.collect { on ->
                    _uiState.value = _uiState.value.copy(musicOn = on)
                    soundManager?.musicEnabled = on
                }
            }
            viewModelScope.launch {
                repo.vibrationOn.collect { on ->
                    _uiState.value = _uiState.value.copy(vibrationOn = on)
                    hapticHelper?.vibrationEnabled = on
                }
            }
        }
    }

    fun onToggleSound() {
        val next = !_uiState.value.soundOn
        _uiState.value = _uiState.value.copy(soundOn = next)
        soundManager?.soundEnabled = next
        viewModelScope.launch { settingsRepository?.setSoundOn(next) }
    }

    fun onToggleMusic() {
        val next = !_uiState.value.musicOn
        _uiState.value = _uiState.value.copy(musicOn = next)
        soundManager?.musicEnabled = next
        viewModelScope.launch { settingsRepository?.setMusicOn(next) }
    }

    fun onToggleVibration() {
        val next = !_uiState.value.vibrationOn
        _uiState.value = _uiState.value.copy(vibrationOn = next)
        viewModelScope.launch { settingsRepository?.setVibrationOn(next) }
    }

    fun onCommand(command: Command) {
        when (command) {
            Command.TAP -> {
                soundManager?.playTap()
                if (_uiState.value.gameState == GameState.MENU) {
                    world.startPlaying()
                    soundManager?.startMusic()
                    _uiState.value = _uiState.value.copy(gameState = GameState.PLAYING)
                }
            }
            Command.LEFT -> world.onLeft()
            Command.RIGHT -> world.onRight()
            Command.JUMP -> {
                soundManager?.playJump()
                world.onJump()
            }
            Command.SLIDE -> {
                soundManager?.playSlide()
                world.onSlide()
            }
        }
    }

    fun onPauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING || _uiState.value.gameState == GameState.RESUME_COUNTDOWN) {
            world.pause()
            soundManager?.pauseMusic()
            _uiState.value = _uiState.value.copy(gameState = GameState.PAUSED)
        }
    }

    fun onResumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            world.resumeCountdown()
            _uiState.value = _uiState.value.copy(
                gameState = GameState.RESUME_COUNTDOWN,
                countdownSeconds = 3
            )
        }
    }

    fun onRestartGame() {
        world.reset()
        soundManager?.startMusic()
        previousCoins = 0
        _uiState.value = _uiState.value.copy(
            gameState = GameState.PLAYING,
            score = 0,
            coins = 0,
            isNewBest = false,
            speed = GameConfig.START_SPEED
        )
    }

    fun onHome() {
        world.toMenu()
        soundManager?.pauseMusic()
        previousCoins = 0
        _uiState.value = _uiState.value.copy(
            gameState = GameState.MENU,
            score = 0,
            coins = 0,
            isNewBest = false
        )
    }

    fun onFrame(rawDt: Float, currentFps: Int = 60) {
        world.update(rawDt)
        val snapshot = world.snapshot()
        val countdownSecs = if (snapshot.state == GameState.RESUME_COUNTDOWN) {
            kotlin.math.ceil(snapshot.countdownRemaining).toInt().coerceIn(1, 3)
        } else {
            3
        }

        // SFX on coin collection
        if (snapshot.coins > previousCoins) {
            soundManager?.playCoin()
            previousCoins = snapshot.coins
        }

        // SFX on countdown tick
        if (snapshot.state == GameState.RESUME_COUNTDOWN && countdownSecs != previousCountdownSecs) {
            soundManager?.playCountdown()
            previousCountdownSecs = countdownSecs
        }

        // SFX and haptics on hit
        if (snapshot.state == GameState.HIT && previousState != GameState.HIT) {
            soundManager?.playHit()
            soundManager?.pauseMusic()
            hapticHelper?.vibrateHit()
        }

        // Music resume when transitioning into PLAYING
        if (snapshot.state == GameState.PLAYING && previousState == GameState.RESUME_COUNTDOWN) {
            soundManager?.startMusic()
        }

        // On transition to GAME_OVER: persist run results and compute isNewBest
        if (snapshot.state == GameState.GAME_OVER && previousState != GameState.GAME_OVER) {
            val isNewBestScore = snapshot.score > _uiState.value.bestScore && snapshot.score > 0
            _uiState.value = _uiState.value.copy(isNewBest = isNewBestScore)
            viewModelScope.launch {
                scoreRepository.saveRunResult(snapshot.score, snapshot.coins)
            }
        }
        previousState = snapshot.state

        if (snapshot.score != _uiState.value.score ||
            snapshot.coins != _uiState.value.coins ||
            snapshot.state != _uiState.value.gameState ||
            currentFps != _uiState.value.fps ||
            countdownSecs != _uiState.value.countdownSeconds ||
            snapshot.distanceMetres.toInt() != _uiState.value.distanceMetres.toInt()
        ) {
            _uiState.value = _uiState.value.copy(
                score = snapshot.score,
                coins = snapshot.coins,
                speed = snapshot.speed,
                distanceMetres = snapshot.distanceMetres,
                activeSegments = world.trackSystem.segments.size,
                gameState = snapshot.state,
                countdownSeconds = countdownSecs,
                fps = currentFps
            )
        }
    }
}
