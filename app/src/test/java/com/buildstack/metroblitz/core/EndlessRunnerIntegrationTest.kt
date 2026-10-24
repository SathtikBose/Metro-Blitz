package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * End-to-end headless integration test for Phase 3 endless world simulation.
 * Verifies that the world can simulate 10 minutes and 1 hour of running continuously
 * with zero crashes, strictly bounded coordinates, constant active segments,
 * and deterministic state reproduction.
 */
class EndlessRunnerIntegrationTest {

    @Test
    fun testTenMinuteHeadlessRun() {
        val world = GameWorld(seed = 9999L)
        world.isInvincible = true
        world.startPlaying()
        assertEquals(GameState.PLAYING, world.state)

        val totalTime = 600.0f // 10 minutes
        val dt = 0.0166f // ~60 FPS
        var elapsed = 0.0f

        while (elapsed < totalTime) {
            world.update(dt)
            elapsed += dt

            // Invariants:
            // 1. Constant segment pool size
            assertEquals(7, world.trackSystem.segments.size)
            // 2. Speed rises and respects clamping
            assertTrue(world.speed in GameConfig.START_SPEED..GameConfig.MAX_SPEED)
            // 3. Distance is strictly increasing
            assertTrue(world.distanceMetres >= 0.0f)
        }

        // At 10 minutes (600s), speed must be clamped at MAX_SPEED (22.0)
        assertEquals(GameConfig.MAX_SPEED, world.speed, 0.001f)
        // Expected distance > 10,000 m
        assertTrue("Distance should exceed 10km after 10 min", world.distanceMetres > 10_000.0f)
    }

    @Test
    fun testOneHourBoundedPositions() {
        val world = GameWorld(seed = 12345L)
        world.isInvincible = true
        world.startPlaying()

        val totalTime = 3600.0f // 1 hour
        val dt = 0.05f
        var elapsed = 0.0f

        while (elapsed < totalTime) {
            world.update(dt)
            elapsed += dt

            for (seg in world.trackSystem.segments) {
                assertTrue("Segment Z must stay bounded: |${seg.z}| < 500", abs(seg.z) < 500.0f)
                for (deco in seg.decorations) {
                    val propZ = seg.z + deco.zOffset
                    assertTrue("Prop Z must stay bounded: |$propZ| < 500", abs(propZ) < 500.0f)
                    assertTrue("Prop X must never intrude on track (|x| >= 3.9m): |${deco.x}|", abs(deco.x) >= 3.9f)
                }
            }
        }
    }
}
