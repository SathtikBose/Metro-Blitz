package com.buildstack.metroblitz.core

import com.buildstack.metroblitz.data.FakeScoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for score calculation, high score tracking, and persistence (SCR-01..08, GAM-23..28).
 */
class ScoreSystemTest {

    @Test
    fun `SCR-01 - score calculation matches floor distance plus 10 times coins`() {
        // Zero distance, zero coins
        assertEquals(0, ScoreSystem.calculateScore(0.0f, 0))

        // Distance only
        assertEquals(10, ScoreSystem.calculateScore(10.2f, 0))
        assertEquals(99, ScoreSystem.calculateScore(99.9f, 0))

        // Coins only
        assertEquals(50, ScoreSystem.calculateScore(0.0f, 5))
        assertEquals(120, ScoreSystem.calculateScore(0.0f, 12))

        // Combined distance and coins
        assertEquals(40, ScoreSystem.calculateScore(10.5f, 3)) // 10 + 30 = 40
        assertEquals(2000, ScoreSystem.calculateScore(1500.9f, 50)) // 1500 + 500 = 2000
    }

    @Test
    fun `SCR-02 and GAM-26 - distance integrates speed over delta time and increases score`() {
        val world = GameWorld(seed = 42L)
        world.startPlaying()

        assertEquals(0.0f, world.distanceMetres, 0.001f)
        assertEquals(0, world.score)

        // Advance 1 second at START_SPEED (9.0 m/s)
        for (i in 0 until 60) {
            world.update(1.0f / 60.0f)
        }

        assertTrue("Distance must increase after 1 second", world.distanceMetres >= 8.9f)
        assertTrue("Score must rise with distance", world.score >= 8)
    }

    @Test
    fun `SCR-03 - updateBestScore updates only if current score is strictly greater`() {
        assertEquals(100, ScoreSystem.updateBestScore(100, 50))
        assertEquals(50, ScoreSystem.updateBestScore(30, 50))
        assertEquals(50, ScoreSystem.updateBestScore(50, 50))
        assertEquals(0, ScoreSystem.updateBestScore(0, 0))
    }

    @Test
    fun `SCR-04 - isNewBest returns true only when score exceeds previous best and is positive`() {
        assertTrue(ScoreSystem.isNewBest(currentScore = 150, previousBest = 100))
        assertTrue(ScoreSystem.isNewBest(currentScore = 1, previousBest = 0))

        assertFalse(ScoreSystem.isNewBest(currentScore = 0, previousBest = 0))
        assertFalse(ScoreSystem.isNewBest(currentScore = 80, previousBest = 100))
        assertFalse(ScoreSystem.isNewBest(currentScore = 100, previousBest = 100))
    }

    @Test
    fun `SCR-05 and GAM-27 - repository persists best score and total coins`() = runBlocking {
        val repo = FakeScoreRepository()

        repo.saveRunResult(score = 250, coinsThisRun = 15)
        assertEquals(250, repo.getBestScore())
        assertEquals(15, repo.getTotalCoins())

        // Run with lower score does not reduce best score
        repo.saveRunResult(score = 120, coinsThisRun = 5)
        assertEquals(250, repo.getBestScore())
        assertEquals(20, repo.getTotalCoins())
    }

    @Test
    fun `SCR-06 - uninitialized repository defaults to 0 best score and 0 coins`() = runBlocking {
        val repo = FakeScoreRepository()
        assertEquals(0, repo.getBestScore())
        assertEquals(0, repo.getTotalCoins())
    }

    @Test
    fun `SCR-07 - total coins accumulates across multiple runs`() = runBlocking {
        val repo = FakeScoreRepository()

        repo.saveRunResult(score = 50, coinsThisRun = 7)
        assertEquals(7, repo.getTotalCoins())

        repo.saveRunResult(score = 80, coinsThisRun = 13)
        assertEquals(20, repo.getTotalCoins())

        repo.saveRunResult(score = 10, coinsThisRun = 0)
        assertEquals(20, repo.getTotalCoins())
    }

    @Test
    fun `SCR-08 and GAM-28 - reset clears per run score and coins while repository retains totals`() = runBlocking {
        val repo = FakeScoreRepository()
        val world = GameWorld(seed = 42L)
        world.startPlaying()

        // Simulate collecting coins and advancing distance
        world.coinSystem.spawnLine(lane = 1, startZ = 0.0f, count = 3, spacingZ = 0.0f)
        world.update(0.016f)

        assertEquals(3, world.coinsCollected)
        assertTrue(world.score >= 30)

        // Save result
        repo.saveRunResult(world.score, world.coinsCollected)
        assertEquals(3, repo.getTotalCoins())
        val savedBest = repo.getBestScore()

        // Reset world for Play Again
        world.reset()
        assertEquals(0, world.score)
        assertEquals(0, world.coinsCollected)
        assertEquals(0.0f, world.distanceMetres, 0.001f)
        assertEquals(GameConfig.START_SPEED, world.speed, 0.001f)

        // Repository remains unaffected
        assertEquals(savedBest, repo.getBestScore())
        assertEquals(3, repo.getTotalCoins())
    }
}
