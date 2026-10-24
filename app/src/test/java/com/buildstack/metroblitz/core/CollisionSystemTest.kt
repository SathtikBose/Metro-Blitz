package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for collision detection, hit state, game over flow, and optimal bot run (COL-01..12, GAM-16..22).
 */
class CollisionSystemTest {

    @Test
    fun `COL-01 and GAM-16 - standing into barricade results in hit`() {
        val barricade = Obstacle(id = 1, type = ObstacleType.BARRICADE_LOW, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(barricade)
        )
        assertNotNull("Standing into barricade must result in collision", hit)
        assertEquals(barricade.id, hit?.id)
    }

    @Test
    fun `COL-02 and GAM-17 - jump apex over barricade survives with no hit`() {
        // Barricade height is 0.9m. At jump apex y = 1.7m (bottom of player is at y = 1.7m > 0.9m)
        val barricade = Obstacle(id = 1, type = ObstacleType.BARRICADE_LOW, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 1.7f, z = 0.0f,
            actionState = PlayerAction.JUMPING,
            obstacles = listOf(barricade)
        )
        assertNull("Jump apex at 1.7m over 0.9m barricade must clear with no hit", hit)
    }

    @Test
    fun `COL-03 - landing before the barricade results in hit`() {
        // Player lands (y = 0.0m) while still overlapping the barricade in Z
        val barricade = Obstacle(id = 1, type = ObstacleType.BARRICADE_LOW, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.1f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(barricade)
        )
        assertNotNull("Landing at y=0 on barricade must result in hit", hit)
    }

    @Test
    fun `COL-04 - standing into beam results in hit`() {
        // Overhead beam occupies y = 1.1m to 1.6m. Standing player is 1.7m tall.
        val beam = Obstacle(id = 2, type = ObstacleType.OVERHEAD_BEAM, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(beam)
        )
        assertNotNull("Standing player must hit overhead beam", hit)
    }

    @Test
    fun `COL-05 and GAM-19 - jumping into beam results in hit`() {
        // Player jumps to y = 1.0m into beam occupying y = 1.1 to 1.6m
        val beam = Obstacle(id = 2, type = ObstacleType.OVERHEAD_BEAM, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 1.0f, z = 0.0f,
            actionState = PlayerAction.JUMPING,
            obstacles = listOf(beam)
        )
        assertNotNull("Jumping into overhead beam must result in hit", hit)
    }

    @Test
    fun `COL-06 and GAM-18 - sliding under beam survives with no hit`() {
        // Sliding player is 0.8m tall (y from 0.0m to 0.8m). Beam bottom is 1.1m.
        val beam = Obstacle(id = 2, type = ObstacleType.OVERHEAD_BEAM, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.SLIDING,
            obstacles = listOf(beam)
        )
        assertNull("Sliding player (0.8m tall) under beam (bottom 1.1m) must survive", hit)
    }

    @Test
    fun `COL-07 and GAM-20 - any contact with train results in hit`() {
        // Train is 3.0m tall, 10.0m long. Cannot be jumped over or slid under.
        val train = Obstacle(id = 3, type = ObstacleType.TRAIN_BLUE, lane = 1, z = 0.0f)

        val hitStanding = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(train)
        )
        assertNotNull("Standing into train must hit", hitStanding)

        val hitSliding = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.SLIDING,
            obstacles = listOf(train)
        )
        assertNotNull("Sliding into train must hit", hitSliding)

        val hitJumping = CollisionSystem.checkCollision(
            x = 0.0f, y = 1.7f, z = 0.0f,
            actionState = PlayerAction.JUMPING,
            obstacles = listOf(train)
        )
        assertNotNull("Jumping into train must hit", hitJumping)
    }

    @Test
    fun `COL-08 - obstacle in another lane results in no hit`() {
        val trainInLane0 = Obstacle(id = 4, type = ObstacleType.TRAIN_BLUE, lane = 0, z = 0.0f)
        val barricadeInLane2 = Obstacle(id = 5, type = ObstacleType.BARRICADE_LOW, lane = 2, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(trainInLane0, barricadeInLane2)
        )
        assertNull("Obstacles in adjacent lanes must not hit player", hit)
    }

    @Test
    fun `COL-09 and GAM-21 - mid lane change overlapping an occupied lane results in hit`() {
        // Player changing lane from 1 (x=0) to 0 (x=-2), currently at x = -1.0m
        // Player width 0.8m -> minX = -1.4m.
        // Train in lane 0 (x=-2) width 1.8m -> maxX = -1.1m.
        // -1.4m < -1.1m, so bounding boxes overlap!
        val trainInLane0 = Obstacle(id = 6, type = ObstacleType.TRAIN_BLUE, lane = 0, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = -1.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(trainInLane0)
        )
        assertNotNull("Moving sideways into an occupied lane overlapping obstacle must hit", hit)
    }

    @Test
    fun `COL-10 - exactly touching edges results in no hit`() {
        // Strict overlap check: if player.maxX == obstacle.minX, no collision
        val boxA = CollisionSystem.Box(minX = 0.0f, maxX = 1.0f, minY = 0.0f, maxY = 1.0f, minZ = 0.0f, maxZ = 1.0f)
        val boxB = CollisionSystem.Box(minX = 1.0f, maxX = 2.0f, minY = 0.0f, maxY = 1.0f, minZ = 0.0f, maxZ = 1.0f)

        assertFalse("Touching edges must not count as overlapping", boxA.intersects(boxB))
    }

    @Test
    fun `COL-11 and GAM-22 - invincible ignores collisions`() {
        val train = Obstacle(id = 7, type = ObstacleType.TRAIN_ORANGE, lane = 1, z = 0.0f)

        val hit = CollisionSystem.checkCollision(
            x = 0.0f, y = 0.0f, z = 0.0f,
            actionState = PlayerAction.RUNNING,
            obstacles = listOf(train),
            isInvincible = true
        )
        assertNull("Invincible flag must ignore all collisions", hit)
    }

    @Test
    fun `COL-12 and UI-05 - hit triggers only once and transitions to game over after 0_8s`() {
        val world = GameWorld(seed = 42L)
        world.startPlaying()
        assertEquals(GameState.PLAYING, world.state)

        // Inject an obstacle directly in player's lane at z = 0.0m
        val obstacle = Obstacle(id = 100, type = ObstacleType.BARRICADE_LOW, lane = 1, z = 0.0f)
        world.spawnDirector.activeObstacles.add(obstacle)

        // Frame update triggers collision
        world.update(0.016f)
        assertEquals("Collision must transition state to HIT", GameState.HIT, world.state)
        assertEquals(obstacle.id, world.lastHitObstacle?.id)

        // Subsequent updates in HIT state do not re-trigger hit
        val initialHitTimer = world.hitTimer
        world.update(0.05f)
        assertEquals(GameState.HIT, world.state)
        assertTrue("Hit timer must advance", world.hitTimer > initialHitTimer)

        // Advance past 0.8s
        for (i in 0 until 20) {
            world.update(0.05f)
        }

        assertEquals("After 0.8s in HIT, state must transition to GAME_OVER", GameState.GAME_OVER, world.state)
    }

    @Test
    fun `UI-06 - play again resets to playing immediately without reloading assets`() {
        val world = GameWorld(seed = 42L)
        world.startPlaying()
        world.hit()
        world.gameOver()
        assertEquals(GameState.GAME_OVER, world.state)

        world.reset()
        assertEquals("Play again must reset to PLAYING immediately", GameState.PLAYING, world.state)
        assertEquals(0.0f, world.distanceMetres, 0.001f)
        assertEquals(GameConfig.START_SPEED, world.speed, 0.001f)
        assertEquals(0, world.score)
    }

    @Test
    fun `Optimal Bot Test - automated bot plays 2 000 m without colliding`() {
        val world = GameWorld(seed = 12345L)
        world.startPlaying()

        val stepDt = 0.016f // 60 fps simulation step

        while (world.distanceMetres < 2000.0f && world.state == GameState.PLAYING) {
            val playerLane = world.player.targetLane
            val activeObstacles = world.spawnDirector.activeObstacles

            // Find obstacles in player's current lane ahead of the player (z <= 0)
            val aheadInLane = activeObstacles
                .filter { it.lane == playerLane && it.z <= 0.2f }
                .sortedByDescending { it.z }

            val immediate = aheadInLane.firstOrNull()

            // Helper to check if a candidate lane is safe to enter right now
            fun canSafelyEnterLane(cLane: Int): Boolean {
                // Must be clear alongside player
                val clearAdjacent = activeObstacles.none {
                    it.lane == cLane && it.minZ < 1.0f && it.maxZ > -1.0f
                }
                if (!clearAdjacent) return false

                // Cannot have an oncoming train within dangerous proximity (< 15m)
                val oncomingTrain = activeObstacles.any {
                    it.lane == cLane && it.type.isTrain && it.z in -15.0f..0.0f
                }
                return !oncomingTrain
            }

            // Decide action based on immediate obstacle
            if (immediate != null) {
                val dist = -immediate.z
                val idealJumpDist = (GameConfig.JUMP_DURATION_SECONDS / 2.0f) * world.speed

                if (immediate.type.isTrain) {
                    // Train approaching: must change lane
                    if (dist < kotlin.math.max(world.speed * 1.5f, 25.0f)) {
                        val candidateLanes = when (playerLane) {
                            1 -> listOf(0, 2)
                            0 -> listOf(1)
                            else -> listOf(1)
                        }

                        // Pick the candidate lane with the furthest upcoming train or obstacle
                        val validCandidates = candidateLanes.filter { canSafelyEnterLane(it) }
                        val bestLane = validCandidates.maxByOrNull { cLane ->
                            val nextObs = activeObstacles
                                .filter { it.lane == cLane && it.z <= 0.0f }
                                .maxByOrNull { it.z }
                            if (nextObs == null) 1000.0f else -nextObs.z
                        }

                        if (bestLane != null) {
                            if (bestLane > playerLane) {
                                world.onRight()
                            } else if (bestLane < playerLane) {
                                world.onLeft()
                            }
                        }
                    }
                } else if (world.player.actionState == PlayerAction.RUNNING) {
                    when (immediate.type) {
                        ObstacleType.BARRICADE_LOW -> {
                            if (dist <= idealJumpDist + 1.0f && dist >= idealJumpDist - 1.0f) {
                                world.onJump()
                            }
                        }
                        ObstacleType.OVERHEAD_BEAM -> {
                            if (dist <= 4.0f && dist >= 0.8f) {
                                world.onSlide()
                            }
                        }
                        else -> {}
                    }
                }
            }

            world.update(stepDt)
        }

        assertEquals(
            "Optimal bot must survive 2 000m without any collision (state should still be PLAYING). Hit info: ${world.lastHitObstacle} at distance ${world.distanceMetres}m, x=${world.player.x}, y=${world.player.y}, action=${world.player.actionState}",
            GameState.PLAYING,
            world.state
        )
        assertTrue(
            "Optimal bot must reach at least 2 000m (reached ${world.distanceMetres}m)",
            world.distanceMetres >= 2000.0f
        )
    }
}
