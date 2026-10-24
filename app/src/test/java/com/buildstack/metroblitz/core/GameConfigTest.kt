package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GameConfig constants, lane positioning, and derived physics.
 * Validates COR-01, COR-02, COR-03, COR-04, COR-05.
 */
class GameConfigTest {

    @Test
    fun testLaneCoordinates() {
        // COR-01: Lane X = -2.0, 0.0, +2.0
        assertEquals(-2.0f, GameConfig.laneX(0), 0.001f)
        assertEquals(0.0f, GameConfig.laneX(1), 0.001f)
        assertEquals(2.0f, GameConfig.laneX(2), 0.001f)
    }

    @Test
    fun testInitialSpeedAndRamp() {
        // COR-02: speed(0) = 9.0
        assertEquals(9.0f, Difficulty.calculateSpeed(0f), 0.001f)

        // COR-03: Speed rises 0.10 m/s each second
        assertEquals(10.0f, Difficulty.calculateSpeed(10f), 0.001f)
        assertEquals(15.0f, Difficulty.calculateSpeed(60f), 0.001f)

        // COR-04: Speed clamps at 22.0 m/s
        assertEquals(22.0f, Difficulty.calculateSpeed(200f), 0.001f)
    }

    @Test
    fun testDifficultyTiers() {
        // COR-05: Tier boundaries at 60 / 400 / 1000 m
        assertEquals(0, Difficulty.tierForDistance(0f))
        assertEquals(0, Difficulty.tierForDistance(59.9f))
        assertEquals(1, Difficulty.tierForDistance(60f))
        assertEquals(1, Difficulty.tierForDistance(399.9f))
        assertEquals(2, Difficulty.tierForDistance(400f))
        assertEquals(2, Difficulty.tierForDistance(999.9f))
        assertEquals(3, Difficulty.tierForDistance(1000f))
        assertEquals(3, Difficulty.tierForDistance(2500f))
    }

    @Test
    fun testDerivedJumpPhysics() {
        // Jump height 1.7m, duration 0.62s
        // g = 8h / T^2 = 8 * 1.7 / (0.62^2) = 13.6 / 0.3844 ≈ 35.3798 m/s^2
        assertTrue("Gravity must be positive and non-zero", GameConfig.GRAVITY > 30.0f)

        // v0 = 4h / T = 4 * 1.7 / 0.62 ≈ 10.9677 m/s
        assertTrue("Jump velocity must be positive", GameConfig.JUMP_INITIAL_VELOCITY > 10.0f)
    }

    @Test
    fun testScoringFormula() {
        assertEquals(150, ScoreSystem.calculateScore(100.5f, 5))
        assertEquals(0, ScoreSystem.calculateScore(0.0f, 0))
    }
}
