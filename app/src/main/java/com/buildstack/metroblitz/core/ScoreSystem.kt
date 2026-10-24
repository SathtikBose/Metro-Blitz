package com.buildstack.metroblitz.core

/**
 * Score calculation and tracking logic.
 * Pure Kotlin — zero platform imports.
 */
object ScoreSystem {
    fun calculateScore(distanceMetres: Float, coinsCollected: Int): Int {
        return distanceMetres.toInt() + coinsCollected * 10
    }
}
