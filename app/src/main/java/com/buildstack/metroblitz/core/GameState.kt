package com.buildstack.metroblitz.core

/**
 * Game state enumeration and snapshot models for UI publishing.
 * Pure Kotlin — zero platform imports.
 */
enum class GameState {
    LOADING,
    MENU,
    PLAYING,
    PAUSED,
    RESUME_COUNTDOWN,
    HIT,
    GAME_OVER
}

/**
 * Snapshot of immutable world state for rendering and UI overlays.
 */
data class GameSnapshot(
    val state: GameState,
    val speed: Float,
    val distanceMetres: Float,
    val score: Int,
    val coins: Int,
    val playerLane: Int,
    val playerX: Float,
    val playerY: Float,
    val playerZ: Float,
    val playerAction: PlayerAction = PlayerAction.RUNNING,
    val isChangingLane: Boolean = false,
    val laneChangeDirection: Int = 0,
    val countdownRemaining: Float = 0.0f,
    val activeObstacles: List<Obstacle> = emptyList(),
    val hitTimer: Float = 0.0f
)
