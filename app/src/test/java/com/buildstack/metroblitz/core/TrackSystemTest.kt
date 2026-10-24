package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for TrackSystem and endless world simulation.
 * Validates:
 * - COR-06: Segment recycles when z > 30 and goes to farthest - 30
 * - COR-07: Active segment count constant over a 10-minute simulation
 * - COR-08: |z| of every object < 500 after a simulated 1 hour at max speed
 * - COR-11: dt clamped to 0.05
 * - COR-12: Same seed -> same decoration/spawn sequence
 */
class TrackSystemTest {

    @Test
    fun testSegmentRecyclingAtThreshold() {
        // COR-06: Segment recycles when z > 30 and goes to farthest - 30
        val track = TrackSystem(poolSize = 7, segmentLength = 30.0f)
        
        // Find segment at z = 30.0f (initially segment 0)
        assertEquals(30.0f, track.segments[0].z, 0.001f)
        val initialMinZ = track.segments.minOf { it.z }
        assertEquals(-150.0f, initialMinZ, 0.001f)

        // Advance slightly so segment 0 exceeds 30.0f
        track.update(speed = 10.0f, dt = 0.05f) // moves by +0.5m

        // Segment 0 must have recycled to (initialMinZ + 0.5m) - 30m = -179.5m
        val newMinZ = track.segments.minOf { it.z }
        assertEquals(-179.5f, newMinZ, 0.001f)
        assertEquals(-179.5f, track.segments[0].z, 0.001f)
    }

    @Test
    fun testConstantSegmentCountTenMinutesSimulation() {
        // COR-07: Active segment count constant over a 10-minute simulation
        val track = TrackSystem(poolSize = 7, segmentLength = 30.0f)
        val simSeconds = 600.0f // 10 minutes
        val dt = 0.016f // ~60 fps
        var elapsed = 0.0f

        while (elapsed < simSeconds) {
            val speed = Difficulty.calculateSpeed(elapsed)
            track.update(speed, dt)
            elapsed += dt
            assertEquals(7, track.segments.size)
        }
    }

    @Test
    fun testBoundedPositionsOneHourAtMaxSpeed() {
        // COR-08: |z| of every object < 500 after a simulated 1 hour at max speed
        val track = TrackSystem(poolSize = 7, segmentLength = 30.0f)
        val simSeconds = 3600.0f // 1 hour
        val dt = 0.05f
        var elapsed = 0.0f

        while (elapsed < simSeconds) {
            track.update(GameConfig.MAX_SPEED, dt)
            elapsed += dt

            for (seg in track.segments) {
                assertTrue(
                    "Segment Z (${seg.z}) must have |z| < 500",
                    abs(seg.z) < 500.0f
                )
                for (deco in seg.decorations) {
                    val propZ = seg.z + deco.zOffset
                    assertTrue(
                        "Scenery prop Z ($propZ) must have |z| < 500",
                        abs(propZ) < 500.0f
                    )
                }
            }
        }
    }

    @Test
    fun testDeltaTimeClamping() {
        // COR-11: dt clamped to 0.05
        val world = GameWorld()
        world.startPlaying()

        // Pass 0.5s delta time (huge lag spike)
        world.update(0.5f)

        // Elapsed time must only advance by at most MAX_DELTA_TIME (0.05f)
        assertEquals(GameConfig.MAX_DELTA_TIME, world.timeElapsedSeconds, 0.0001f)
    }

    @Test
    fun testDeterministicSequenceWithSameSeed() {
        // COR-12: Same seed -> same decoration/spawn sequence
        val track1 = TrackSystem(poolSize = 7, rng = Rng(42L))
        val track2 = TrackSystem(poolSize = 7, rng = Rng(42L))

        // Compare initial decorations
        for (i in 0 until 7) {
            assertEquals(track1.segments[i].decorations.size, track2.segments[i].decorations.size)
            for (j in track1.segments[i].decorations.indices) {
                val d1 = track1.segments[i].decorations[j]
                val d2 = track2.segments[i].decorations[j]
                assertEquals(d1.modelId, d2.modelId)
                assertEquals(d1.x, d2.x, 0.001f)
                assertEquals(d1.y, d2.y, 0.001f)
                assertEquals(d1.zOffset, d2.zOffset, 0.001f)
            }
        }

        // Run both for 100 seconds
        for (step in 0 until 1000) {
            track1.update(15.0f, 0.05f)
            track2.update(15.0f, 0.05f)
        }

        // Must still match exactly
        for (i in 0 until 7) {
            assertEquals(track1.segments[i].z, track2.segments[i].z, 0.001f)
            assertEquals(track1.segments[i].decorations.size, track2.segments[i].decorations.size)
            for (j in track1.segments[i].decorations.indices) {
                val d1 = track1.segments[i].decorations[j]
                val d2 = track2.segments[i].decorations[j]
                assertEquals(d1.modelId, d2.modelId)
            }
        }
    }
}
