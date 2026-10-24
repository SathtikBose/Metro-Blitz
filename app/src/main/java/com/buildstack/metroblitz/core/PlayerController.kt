package com.buildstack.metroblitz.core

/**
 * Player lane, jump, and slide state machine with input buffering.
 * Pure Kotlin — zero platform imports.
 */
class PlayerController {
    var currentLane: Int = 1
        private set

    var x: Float = 0.0f
        private set
    var y: Float = 0.0f
        private set
    var z: Float = 0.0f
        private set

    fun reset() {
        currentLane = 1
        x = 0.0f
        y = 0.0f
        z = 0.0f
    }
}
