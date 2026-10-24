package com.buildstack.metroblitz.core

import kotlin.math.abs
import kotlin.math.sin

/**
 * Manages procedural coin generation, scrolling, collection, spin/bob animation, and pooling.
 * Pure Kotlin — zero platform imports.
 */
class CoinSystem(
    val seed: Long = 1337L
) {
    val rng = Rng(seed)
    val activeCoins = mutableListOf<Coin>()

    var nextCoinId: Int = 1
        private set

    var coinsCollectedThisRun: Int = 0
        private set

    var spinAngleDegrees: Float = 0.0f
        private set

    var totalTimeSeconds: Float = 0.0f
        private set

    val bobOffsetY: Float
        get() = sin(totalTimeSeconds * 4.0f) * 0.05f

    fun reset(newSeed: Long = seed) {
        activeCoins.clear()
        rng.reset(newSeed)
        nextCoinId = 1
        coinsCollectedThisRun = 0
        spinAngleDegrees = 0.0f
        totalTimeSeconds = 0.0f
    }

    /**
     * Spawns a straight line of coins in a single lane at ground level.
     */
    fun spawnLine(lane: Int, startZ: Float, count: Int = 6, spacingZ: Float = 2.0f, obstacles: List<Obstacle> = emptyList()) {
        for (i in 0 until count) {
            val coinZ = startZ - (i * spacingZ)
            val coin = Coin(
                id = nextCoinId++,
                lane = lane,
                z = coinZ,
                y = GameConfig.COIN_GROUND_Y
            )
            if (obstacles.none { coin.intersects(it) }) {
                activeCoins.add(coin)
            }
        }
    }

    /**
     * Spawns a parabolic arc of coins over a low barricade.
     * 7 coins: 3 on ascent, 1 at apex (y=2.2m), 3 on descent.
     * Checks against all active obstacles to guarantee zero overlap.
     */
    fun spawnArcOverBarricade(barricade: Obstacle, obstacles: List<Obstacle> = listOf(barricade)) {
        val lane = barricade.lane
        val centerZ = barricade.z

        // Offsets in Z relative to barricade: -6m (ahead of barricade), -4m, -2m, 0m (apex), +2m, +4m, +6m
        val offsets = floatArrayOf(-6.0f, -4.0f, -2.0f, 0.0f, 2.0f, 4.0f, 6.0f)
        val heights = floatArrayOf(
            GameConfig.COIN_GROUND_Y,
            1.5f,
            2.0f,
            GameConfig.COIN_ARC_PEAK_Y, // 2.2m
            2.0f,
            1.5f,
            GameConfig.COIN_GROUND_Y
        )

        val allObstacles = if (obstacles.contains(barricade)) obstacles else obstacles + barricade

        for (i in offsets.indices) {
            val coin = Coin(
                id = nextCoinId++,
                lane = lane,
                z = centerZ + offsets[i],
                y = heights[i]
            )
            // Safety check: ensure no overlap with ANY obstacle in the world
            if (allObstacles.none { coin.intersects(it) }) {
                activeCoins.add(coin)
            }
        }
    }

    /**
     * Spawns a zig-zag pattern of coins alternating across lanes at ground level.
     */
    fun spawnZigZag(startLane: Int, startZ: Float, count: Int = 5, spacingZ: Float = 3.0f, obstacles: List<Obstacle> = emptyList()) {
        var currentLane = startLane
        var direction = if (currentLane == 0) 1 else -1

        for (i in 0 until count) {
            val coinZ = startZ - (i * spacingZ)
            val coin = Coin(
                id = nextCoinId++,
                lane = currentLane,
                z = coinZ,
                y = GameConfig.COIN_GROUND_Y
            )
            if (obstacles.none { coin.intersects(it) }) {
                activeCoins.add(coin)
            }

            // Step lane
            currentLane += direction
            if (currentLane <= 0) {
                currentLane = 0
                direction = 1
            } else if (currentLane >= GameConfig.LANE_COUNT - 1) {
                currentLane = GameConfig.LANE_COUNT - 1
                direction = -1
            }
        }
    }

    /**
     * Checks pickup collisions with the player.
     * Returns the list of coins collected this frame.
     */
    fun checkPickups(player: PlayerController): List<Coin> {
        val collectedThisFrame = mutableListOf<Coin>()
        val playerHeight = if (player.actionState == PlayerAction.SLIDING) {
            GameConfig.PLAYER_HEIGHT_SLIDING
        } else {
            GameConfig.PLAYER_HEIGHT_STANDING
        }

        val playerMinY = player.y
        val playerMaxY = player.y + playerHeight

        for (i in activeCoins.indices) {
            val coin = activeCoins[i]
            if (coin.isCollected) continue

            // 1. X check (must be within pickup radius 0.6m in X)
            val xDist = abs(player.x - coin.x)
            if (xDist > 0.6f) continue

            // 2. Z check (must be within pickup radius 0.6m in Z)
            val zDist = abs(player.z - coin.z)
            if (zDist > 0.6f) continue

            // 3. Y check: coin must be within player Y interval expanded by vertical pickup radius (0.3m)
            if (coin.y < playerMinY - 0.3f || coin.y > playerMaxY + 0.3f) continue

            // Collected!
            coin.isCollected = true
            coinsCollectedThisRun++
            collectedThisFrame.add(coin)
        }

        return collectedThisFrame
    }

    /**
     * Updates coin scrolling, spin, bob, and despawning.
     */
    fun update(speed: Float, dt: Float, player: PlayerController): List<Coin> {
        totalTimeSeconds += dt
        spinAngleDegrees = (spinAngleDegrees + GameConfig.COIN_SPIN_SPEED_DEG_PER_SEC * dt) % 360.0f

        // Check pickups
        val newlyCollected = checkPickups(player)

        // Move coins and advance pop timer for collected coins
        val it = activeCoins.iterator()
        while (it.hasNext()) {
            val coin = it.next()
            coin.z += speed * dt

            if (coin.isCollected) {
                coin.popTimer += dt
                // Remove when pop animation finishes
                if (coin.popTimer >= GameConfig.COIN_POP_DURATION_SECONDS) {
                    it.remove()
                    continue
                }
            }

            // Despawn when coin has passed behind camera
            if (coin.z > GameConfig.DESPAWN_Z_METRES) {
                it.remove()
            }
        }

        return newlyCollected
    }
}
