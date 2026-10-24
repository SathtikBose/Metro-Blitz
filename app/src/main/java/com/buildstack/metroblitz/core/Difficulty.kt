package com.buildstack.metroblitz.core

/**
 * Calculates difficulty tier based on distance travelled.
 * Pure Kotlin — zero platform imports.
 */
object Difficulty {
    fun calculateSpeed(timeElapsedSeconds: Float): Float {
        return (GameConfig.START_SPEED + timeElapsedSeconds * GameConfig.SPEED_GAIN_PER_SECOND)
            .coerceAtMost(GameConfig.MAX_SPEED)
    }

    fun tierForDistance(distanceMetres: Float): Int {
        return when {
            distanceMetres < GameConfig.TIER_1_DISTANCE -> 0
            distanceMetres < GameConfig.TIER_2_DISTANCE -> 1
            distanceMetres < GameConfig.TIER_3_DISTANCE -> 2
            else -> 3
        }
    }
}
