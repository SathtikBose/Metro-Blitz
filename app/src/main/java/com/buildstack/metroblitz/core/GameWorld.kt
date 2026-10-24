package com.buildstack.metroblitz.core

/**
 * Owns the gameplay simulation.
 * Pure Kotlin — zero platform imports.
 */
class GameWorld(
    val seed: Long = 1337L
) {
    val stateMachine = GameStateMachine(GameState.MENU)
    val state: GameState
        get() = stateMachine.currentState

    val player = PlayerController()

    val trackSystem = TrackSystem(
        poolSize = GameConfig.SEGMENT_POOL_SIZE,
        segmentLength = GameConfig.SEGMENT_LENGTH_METRES,
        rng = Rng(seed)
    )

    var speed: Float = GameConfig.START_SPEED
    var distanceMetres: Float = 0.0f
    var score: Int = 0
    var coinsCollected: Int = 0
    var timeElapsedSeconds: Float = 0.0f
    var countdownRemaining: Float = 0.0f

    fun update(rawDt: Float) {
        val dt = rawDt.coerceIn(0.0f, GameConfig.MAX_DELTA_TIME)
        when (state) {
            GameState.PLAYING -> {
                timeElapsedSeconds += dt
                speed = Difficulty.calculateSpeed(timeElapsedSeconds)
                distanceMetres += speed * dt
                score = ScoreSystem.calculateScore(distanceMetres, coinsCollected)
                player.update(dt)
                trackSystem.update(speed, dt)
            }
            GameState.RESUME_COUNTDOWN -> {
                countdownRemaining -= dt
                if (countdownRemaining <= 0.0f) {
                    countdownRemaining = 0.0f
                    stateMachine.transitionTo(GameState.PLAYING)
                }
            }
            GameState.MENU, GameState.PAUSED, GameState.HIT, GameState.GAME_OVER, GameState.LOADING -> {
                // World frozen
            }
        }
    }

    fun idle(rawDt: Float) {
        // World frozen
    }

    fun onLeft() {
        if (state == GameState.PLAYING) player.onLeft()
    }

    fun onRight() {
        if (state == GameState.PLAYING) player.onRight()
    }

    fun onJump() {
        if (state == GameState.PLAYING) player.onJump()
    }

    fun onSlide() {
        if (state == GameState.PLAYING) player.onSlide()
    }

    fun startPlaying() {
        stateMachine.transitionTo(GameState.PLAYING)
    }

    fun pause() {
        stateMachine.transitionTo(GameState.PAUSED)
    }

    fun resumeCountdown() {
        if (stateMachine.transitionTo(GameState.RESUME_COUNTDOWN)) {
            countdownRemaining = GameConfig.RESUME_COUNTDOWN_SECONDS
        }
    }

    fun resumePlaying() {
        stateMachine.transitionTo(GameState.PLAYING)
    }

    fun hit() {
        stateMachine.transitionTo(GameState.HIT)
    }

    fun gameOver() {
        stateMachine.transitionTo(GameState.GAME_OVER)
    }

    fun reset() {
        speed = GameConfig.START_SPEED
        distanceMetres = 0.0f
        score = 0
        coinsCollected = 0
        timeElapsedSeconds = 0.0f
        countdownRemaining = 0.0f
        player.reset()
        trackSystem.reset(seed)
        stateMachine.reset(GameState.PLAYING)
    }

    fun toMenu() {
        speed = GameConfig.START_SPEED
        distanceMetres = 0.0f
        score = 0
        coinsCollected = 0
        timeElapsedSeconds = 0.0f
        countdownRemaining = 0.0f
        player.reset()
        trackSystem.reset(seed)
        stateMachine.reset(GameState.MENU)
    }

    fun snapshot(): GameSnapshot {
        return GameSnapshot(
            state = state,
            speed = speed,
            distanceMetres = distanceMetres,
            score = score,
            coins = coinsCollected,
            playerLane = player.currentLane,
            playerX = player.x,
            playerY = player.y,
            playerZ = player.z,
            playerAction = player.actionState,
            isChangingLane = player.isChangingLane,
            laneChangeDirection = player.laneChangeDirection,
            countdownRemaining = countdownRemaining
        )
    }
}
