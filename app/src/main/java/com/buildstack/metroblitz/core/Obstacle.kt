package com.buildstack.metroblitz.core

/**
 * Active obstacle instance in the game world.
 * Pure Kotlin — zero platform imports.
 */
data class Obstacle(
    val id: Int,
    val type: ObstacleType,
    val lane: Int,
    var z: Float
) {
    val x: Float
        get() = GameConfig.laneX(lane)

    val length: Float
        get() = type.length

    val minZ: Float
        get() = z - length / 2.0f

    val maxZ: Float
        get() = z + length / 2.0f

    val minX: Float
        get() = x - type.width / 2.0f

    val maxX: Float
        get() = x + type.width / 2.0f

    val minY: Float
        get() = if (type == ObstacleType.OVERHEAD_BEAM) GameConfig.BEAM_BOTTOM_Y else 0.0f

    val maxY: Float
        get() = type.height
}
