package com.buildstack.metroblitz.core

/**
 * Representation of a collectible coin in the endless runner world.
 * Pure Kotlin — zero platform imports.
 */
data class Coin(
    val id: Int,
    var lane: Int,
    var z: Float,
    var y: Float = GameConfig.COIN_GROUND_Y,
    var isCollected: Boolean = false,
    var popTimer: Float = 0.0f
) {
    val x: Float get() = GameConfig.laneX(lane)

    val minX: Float get() = x - GameConfig.COIN_RADIUS
    val maxX: Float get() = x + GameConfig.COIN_RADIUS
    val minY: Float get() = y - GameConfig.COIN_RADIUS
    val maxY: Float get() = y + GameConfig.COIN_RADIUS
    val minZ: Float get() = z - GameConfig.COIN_RADIUS
    val maxZ: Float get() = z + GameConfig.COIN_RADIUS

    /**
     * Checks if this coin bounding box intersects with an obstacle's bounding box.
     */
    fun intersects(obstacle: Obstacle): Boolean {
        if (lane != obstacle.lane) return false
        val obsBox = CollisionSystem.getObstacleBox(obstacle)
        val xOverlap = minX < obsBox.maxX && maxX > obsBox.minX
        val yOverlap = minY < obsBox.maxY && maxY > obsBox.minY
        val zOverlap = minZ < obsBox.maxZ && maxZ > obsBox.minZ
        return xOverlap && yOverlap && zOverlap
    }
}

/**
 * Pattern types for coin clusters.
 */
enum class CoinPatternType {
    LINE,
    ARC,
    ZIG_ZAG
}
