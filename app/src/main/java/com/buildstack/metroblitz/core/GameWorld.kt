package com.buildstack.metroblitz.core

/**
 * Owns the gameplay simulation.
 * Pure Kotlin — zero platform imports.
 */
class GameWorld(
    var state: GameState = GameState.MENU
) {
    var speed: Float = GameConfig.START_SPEED
    var distanceMetres: Float = 0.0f
    var score: Int = 0
    var coinsCollected: Int = 0
    var timeElapsedSeconds: Float = 0.0f

    fun update(rawDt: Float) {
        val dt = rawDt.coerceIn(0.0f, GameConfig.MAX_DELTA_TIME)
        if (state == GameState.PLAYING) {
            timeElapsedSeconds += dt
            speed = (GameConfig.START_SPEED + timeElapsedSeconds * GameConfig.SPEED_GAIN_PER_SECOND)
                .coerceAtMost(GameConfig.MAX_SPEED)
            distanceMetres += speed * dt
            score = distanceMetres.toInt() + coinsCollected * 10
        }
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

    fun reset() {
        speed = GameConfig.START_SPEED
        distanceMetres = 0.0f
        score = 0
        coinsCollected = 0
        timeElapsedSeconds = 0.0f
        state = GameState.PLAYING
    }
}
