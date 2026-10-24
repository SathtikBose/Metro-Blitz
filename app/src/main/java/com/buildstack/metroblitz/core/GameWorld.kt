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

    fun update(rawDt: Float) {
        val dt = rawDt.coerceIn(0.0f, GameConfig.MAX_DELTA_TIME)
        if (state == GameState.PLAYING) {
            timeElapsedSeconds += dt
            speed = Difficulty.calculateSpeed(timeElapsedSeconds)
            distanceMetres += speed * dt
            score = ScoreSystem.calculateScore(distanceMetres, coinsCollected)
            trackSystem.update(speed, dt)
        }
    }

    fun idle(rawDt: Float) {
        // In MENU, PAUSED, COUNTDOWN: world does not advance
    }

    fun startPlaying() {
        stateMachine.transitionTo(GameState.PLAYING)
    }

    fun pause() {
        stateMachine.transitionTo(GameState.PAUSED)
    }

    fun resumeCountdown() {
        stateMachine.transitionTo(GameState.RESUME_COUNTDOWN)
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
        trackSystem.reset(seed)
        stateMachine.reset(GameState.PLAYING)
    }

    fun toMenu() {
        speed = GameConfig.START_SPEED
        distanceMetres = 0.0f
        score = 0
        coinsCollected = 0
        timeElapsedSeconds = 0.0f
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
            playerLane = 1,
            playerX = 0.0f,
            playerY = 0.0f,
            playerZ = 0.0f
        )
    }
}
