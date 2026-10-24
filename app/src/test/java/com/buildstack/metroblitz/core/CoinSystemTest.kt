package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for coin patterns, pickup radius, obstacle separation, and collection lifecycle (CON-01..10, GAM-23..25).
 */
class CoinSystemTest {

    @Test
    fun `CON-01 and GAM-23 - player inside pickup radius collects coin and increments count`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController() // lane 1, x=0, y=0, z=0

        // Coin placed directly in player's path
        val coin = Coin(id = 1, lane = 1, z = 0.0f, y = GameConfig.COIN_GROUND_Y)
        coinSystem.activeCoins.add(coin)

        val collected = coinSystem.checkPickups(player)
        assertEquals(1, collected.size)
        assertEquals(coin.id, collected[0].id)
        assertTrue(coin.isCollected)
        assertEquals(1, coinSystem.coinsCollectedThisRun)
    }

    @Test
    fun `CON-02 - player does not collect coins in adjacent lanes`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController() // lane 1, x=0, y=0, z=0

        // Coins in lane 0 (x=-2.0m) and lane 2 (x=+2.0m) at z = 0.0m
        val coinLeft = Coin(id = 1, lane = 0, z = 0.0f, y = GameConfig.COIN_GROUND_Y)
        val coinRight = Coin(id = 2, lane = 2, z = 0.0f, y = GameConfig.COIN_GROUND_Y)
        coinSystem.activeCoins.addAll(listOf(coinLeft, coinRight))

        val collected = coinSystem.checkPickups(player)
        assertTrue("Coins in adjacent lanes must not be collected", collected.isEmpty())
        assertFalse(coinLeft.isCollected)
        assertFalse(coinRight.isCollected)
        assertEquals(0, coinSystem.coinsCollectedThisRun)
    }

    @Test
    fun `CON-03 - coins never overlap obstacle bounding boxes across 10 000 spawned rows`() {
        val world = GameWorld(seed = 9999L)
        world.isInvincible = true
        world.startPlaying()

        val dt = 0.05f // 50ms simulation step

        // Simulate 2 000m of continuous gameplay
        while (world.distanceMetres < 2000.0f) {
            world.update(dt)

            for (coin in world.coinSystem.activeCoins) {
                for (obs in world.spawnDirector.activeObstacles) {
                    val overlap = coin.intersects(obs)
                    assertFalse(
                        "Coin $coin must never overlap obstacle $obs! Overlap detected at distance ${world.distanceMetres} m",
                        overlap
                    )
                }
            }
        }
    }

    @Test
    fun `CON-04 and GAM-24 - coin pattern shapes are mathematically correct`() {
        val coinSystem = CoinSystem(seed = 100L)

        // 1. Line pattern
        coinSystem.spawnLine(lane = 1, startZ = -10.0f, count = 5, spacingZ = 2.0f)
        assertEquals(5, coinSystem.activeCoins.size)
        for (i in 0 until 5) {
            val c = coinSystem.activeCoins[i]
            assertEquals(1, c.lane)
            assertEquals(-10.0f - (i * 2.0f), c.z, 0.001f)
            assertEquals(GameConfig.COIN_GROUND_Y, c.y, 0.001f)
        }

        coinSystem.reset()

        // 2. Arc pattern over a low barricade
        val barricade = Obstacle(id = 1, type = ObstacleType.BARRICADE_LOW, lane = 0, z = -20.0f)
        coinSystem.spawnArcOverBarricade(barricade)
        assertEquals(7, coinSystem.activeCoins.size)

        // Arc apex is at the barricade's Z and peaks at 2.2m
        val apexCoin = coinSystem.activeCoins.first { it.z == -20.0f }
        assertEquals(0, apexCoin.lane)
        assertEquals(GameConfig.COIN_ARC_PEAK_Y, apexCoin.y, 0.001f)

        coinSystem.reset()

        // 3. Zig-zag pattern
        coinSystem.spawnZigZag(startLane = 0, startZ = -15.0f, count = 5, spacingZ = 3.0f)
        assertEquals(5, coinSystem.activeCoins.size)
        val lanes = coinSystem.activeCoins.map { it.lane }
        assertEquals(listOf(0, 1, 2, 1, 0), lanes)
    }

    @Test
    fun `CON-05 - coins are recycled when passing behind camera or completing pop animation`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController()

        // Coin far behind camera (z > DESPAWN_Z_METRES = 30m)
        val behindCoin = Coin(id = 1, lane = 1, z = 35.0f)
        coinSystem.activeCoins.add(behindCoin)

        coinSystem.update(speed = 10.0f, dt = 0.016f, player = player)
        assertTrue("Coin behind camera must be despawned", coinSystem.activeCoins.isEmpty())

        // Collected coin finishes pop animation
        val collectedCoin = Coin(id = 2, lane = 1, z = 0.0f, isCollected = true, popTimer = 0.14f)
        coinSystem.activeCoins.add(collectedCoin)

        // Update past pop duration (0.15s)
        coinSystem.update(speed = 10.0f, dt = 0.02f, player = player)
        assertTrue("Collected coin after pop duration must be removed", coinSystem.activeCoins.isEmpty())
    }

    @Test
    fun `CON-06 - collected coin cannot be collected a second time`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController()

        val coin = Coin(id = 1, lane = 1, z = 0.0f, isCollected = true)
        coinSystem.activeCoins.add(coin)

        val collected = coinSystem.checkPickups(player)
        assertTrue("Already collected coin cannot be re-collected", collected.isEmpty())
        assertEquals(0, coinSystem.coinsCollectedThisRun)
    }

    @Test
    fun `CON-07 - coin count increments exactly by 1 per collected coin`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController()

        coinSystem.spawnLine(lane = 1, startZ = 0.0f, count = 3, spacingZ = 0.0f)
        val collected = coinSystem.checkPickups(player)

        assertEquals(3, collected.size)
        assertEquals(3, coinSystem.coinsCollectedThisRun)
    }

    @Test
    fun `CON-08 and GAM-25 - arc requires jump timing to collect apex coin`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController()

        // Coin at arc apex (y = 2.2m)
        val apexCoin = Coin(id = 1, lane = 1, z = 0.0f, y = GameConfig.COIN_ARC_PEAK_Y)
        coinSystem.activeCoins.add(apexCoin)

        // 1. Running on ground (player.y = 0.0m, standing top = 1.7m)
        val groundCollected = coinSystem.checkPickups(player)
        assertTrue("Ground running player cannot reach arc apex coin at 2.2m", groundCollected.isEmpty())
        assertFalse(apexCoin.isCollected)

        // 2. Jumping player near apex (player.y = 1.5m)
        player.onJump()
        // Simulate jump until player.y reaches apex (~1.7m)
        for (i in 0 until 18) {
            player.update(1.0f / 60.0f)
        }
        assertTrue("Player must be in airborne jump arc", player.y >= 1.4f)

        val jumpCollected = coinSystem.checkPickups(player)
        assertEquals(1, jumpCollected.size)
        assertTrue(apexCoin.isCollected)
    }

    @Test
    fun `CON-09 - spin angle and bob offset advance with delta time`() {
        val coinSystem = CoinSystem(seed = 42L)
        val player = PlayerController()

        assertEquals(0.0f, coinSystem.spinAngleDegrees, 0.001f)

        coinSystem.update(speed = 10.0f, dt = 0.5f, player = player)
        // 180 deg/s * 0.5s = 90 degrees
        assertEquals(90.0f, coinSystem.spinAngleDegrees, 0.1f)
        assertEquals(0.5f, coinSystem.totalTimeSeconds, 0.001f)
        assertTrue(coinSystem.bobOffsetY != 0.0f)
    }
}
