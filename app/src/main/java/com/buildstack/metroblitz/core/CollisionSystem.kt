package com.buildstack.metroblitz.core

/**
 * Pure Kotlin Axis-Aligned Bounding Box (AABB) collision detection system.
 * Performs strict geometric overlap checks between player and obstacles.
 * Pure Kotlin — zero platform imports.
 */
object CollisionSystem {

    data class Box(
        val minX: Float,
        val maxX: Float,
        val minY: Float,
        val maxY: Float,
        val minZ: Float,
        val maxZ: Float
    ) {
        /**
         * Strict overlap check: touching edges (e.g. minX == other.maxX) does NOT overlap (COL-10).
         */
        fun intersects(other: Box): Boolean {
            return minX < other.maxX && maxX > other.minX &&
                   minY < other.maxY && maxY > other.minY &&
                   minZ < other.maxZ && maxZ > other.minZ
        }
    }

    /**
     * Builds the AABB hitbox for the player based on position, lane-change motion, and action state.
     */
    fun getPlayerBox(
        x: Float,
        y: Float,
        z: Float,
        actionState: PlayerAction
    ): Box {
        val halfW = GameConfig.PLAYER_WIDTH / 2.0f // 0.4m
        val halfD = GameConfig.PLAYER_DEPTH / 2.0f // 0.3m
        val height = if (actionState == PlayerAction.SLIDING) {
            GameConfig.PLAYER_HEIGHT_SLIDING // 0.8m
        } else {
            GameConfig.PLAYER_HEIGHT_STANDING // 1.7m
        }

        return Box(
            minX = x - halfW,
            maxX = x + halfW,
            minY = y,
            maxY = y + height,
            minZ = z - halfD,
            maxZ = z + halfD
        )
    }

    fun getPlayerBox(player: PlayerController): Box =
        getPlayerBox(player.x, player.y, player.z, player.actionState)

    /**
     * Builds the AABB hitbox for an obstacle based on its type and coordinates.
     */
    fun getObstacleBox(obstacle: Obstacle): Box {
        val halfW = obstacle.type.width / 2.0f
        val halfD = obstacle.type.length / 2.0f

        val (minY, maxY) = when (obstacle.type) {
            ObstacleType.OVERHEAD_BEAM -> Pair(GameConfig.BEAM_BOTTOM_Y, GameConfig.BEAM_TOP_Y)
            ObstacleType.BARRICADE_LOW -> Pair(0.0f, GameConfig.BARRICADE_HEIGHT)
            ObstacleType.TRAIN_BLUE, ObstacleType.TRAIN_ORANGE -> Pair(0.0f, GameConfig.TRAIN_HEIGHT)
        }

        return Box(
            minX = obstacle.x - halfW,
            maxX = obstacle.x + halfW,
            minY = minY,
            maxY = maxY,
            minZ = obstacle.z - halfD,
            maxZ = obstacle.z + halfD
        )
    }

    /**
     * Checks if coordinates and action collide with any active obstacle.
     */
    fun checkCollision(
        x: Float,
        y: Float,
        z: Float,
        actionState: PlayerAction,
        obstacles: List<Obstacle>,
        isInvincible: Boolean = false
    ): Obstacle? {
        if (isInvincible) return null

        val playerBox = getPlayerBox(x, y, z, actionState)
        for (i in obstacles.indices) {
            val obs = obstacles[i]
            // Fast reject obstacles that are far ahead (> 15m) or far behind (< -5m)
            if (obs.z < -20.0f || obs.z > 10.0f) continue

            val obsBox = getObstacleBox(obs)
            if (playerBox.intersects(obsBox)) {
                return obs
            }
        }
        return null
    }

    /**
     * Checks if the player collides with any active obstacle.
     * Returns the first colliding obstacle, or null if clear.
     */
    fun checkCollision(
        player: PlayerController,
        obstacles: List<Obstacle>,
        isInvincible: Boolean = false
    ): Obstacle? = checkCollision(player.x, player.y, player.z, player.actionState, obstacles, isInvincible)
}
