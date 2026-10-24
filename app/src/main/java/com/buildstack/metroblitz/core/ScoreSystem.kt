package com.buildstack.metroblitz.core

import kotlin.math.floor

/**
 * Single source of truth for score calculation and high score tracking logic.
 * GDD formula: score = floor(distanceMetres) + coinsCollectedThisRun * 10
 * Pure Kotlin — zero platform imports.
 */
object ScoreSystem {

    /**
     * Calculates the player's total score for the run based on distance travelled and coins collected.
     */
    fun calculateScore(distanceMetres: Float, coinsCollected: Int): Int {
        val distanceScore = floor(distanceMetres.coerceAtLeast(0.0f)).toInt()
        val coinScore = coinsCollected.coerceAtLeast(0) * GameConfig.COIN_SCORE_VALUE
        return distanceScore + coinScore
    }

    /**
     * Determines whether the current run's score qualifies as a new high score.
     */
    fun isNewBest(currentScore: Int, previousBest: Int): Boolean {
        return currentScore > previousBest && currentScore > 0
    }

    /**
     * Computes the updated best score given a completed run.
     */
    fun updateBestScore(currentScore: Int, previousBest: Int): Int {
        return maxOf(currentScore, previousBest)
    }
}
