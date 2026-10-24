package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Spawning fairness and invariant tests (SPN-01..10, GAM-13..15).
 * Validates deterministic row generation, difficulty tier gating, gap formulas,
 * survivability, and collision-free spacing over 10,000 seeded rows.
 */
class SpawnDirectorTest {

    @Test
    fun `SPN-01 - over 10 000 seeded rows every row has a survivable option`() {
        val director = SpawnDirector(seed = 42L)
        var distance = 60.0f
        var time = 0.0f

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)
            assertTrue(
                "Row at distance $distance ($row) must have at least one survivable lane",
                row.pattern.hasSurvivableLane()
            )
            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `SPN-02 - every row with a train has a train-free lane`() {
        val director = SpawnDirector(seed = 999L)
        var distance = 60.0f
        var time = 0.0f

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)
            if (row.pattern.hasTrain) {
                assertTrue(
                    "Row with train at distance $distance ($row) must have at least one train-free lane",
                    row.pattern.hasTrainFreeLane()
                )
                assertTrue(
                    "Train count in row at distance $distance must be <= 2",
                    row.pattern.trainCount <= 2
                )
            }
            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `SPN-03 - no beam closer than speed times 0_9s after a barricade in the same lane`() {
        val director = SpawnDirector(seed = 777L)
        var distance = 60.0f
        var time = 0.0f
        var prevRow: SpawnedRow? = null

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)

            if (prevRow != null) {
                for (lane in 0 until GameConfig.LANE_COUNT) {
                    val prevType = prevRow.pattern.getLane(lane)
                    val currType = row.pattern.getLane(lane)

                    if (prevType == ObstacleType.BARRICADE_LOW && currType == ObstacleType.OVERHEAD_BEAM) {
                        val distanceBetween = row.distance - prevRow.distance
                        val requiredGap = prevRow.speed * 0.9f - 0.01f // With float precision tolerance
                        assertTrue(
                            "Beam at $distance in lane $lane after barricade at ${prevRow.distance} must have gap >= $requiredGap (was $distanceBetween)",
                            distanceBetween >= requiredGap
                        )
                    }
                }
            }

            prevRow = row
            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `SPN-04 - lane change distance reachable within the gap`() {
        // Double lane change takes 2 * 0.12s = 0.24s.
        // At max speed 22 m/s, distance covered is 22 * 0.24 = 5.28m.
        // Row gap must always exceed this reachable distance.
        val director = SpawnDirector(seed = 12345L)
        var distance = 60.0f
        var time = 0.0f

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)
            val requiredLaneChangeDistance = speed * (2.0f * GameConfig.LANE_CHANGE_DURATION_SECONDS)

            assertTrue(
                "Gap ${row.gapToNext} must exceed double lane change distance $requiredLaneChangeDistance",
                row.gapToNext >= requiredLaneChangeDistance
            )

            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `SPN-05 - determinism per seed`() {
        val seed = 88888L
        val directorA = SpawnDirector(seed = seed)
        val directorB = SpawnDirector(seed = seed)

        var distA = 60.0f
        var distB = 60.0f
        var time = 0.0f

        for (i in 0 until 2_000) {
            val speed = Difficulty.calculateSpeed(time)
            val rowA = directorA.generateNextRow(distA, speed)
            val rowB = directorB.generateNextRow(distB, speed)

            assertEquals("Row $i pattern id must match", rowA.pattern.id, rowB.pattern.id)
            assertEquals("Row $i distance must match", rowA.distance, rowB.distance, 0.001f)
            assertEquals("Row $i gap must match", rowA.gapToNext, rowB.gapToNext, 0.001f)
            assertEquals("Row $i left lane must match", rowA.pattern.left, rowB.pattern.left)
            assertEquals("Row $i middle lane must match", rowA.pattern.middle, rowB.pattern.middle)
            assertEquals("Row $i right lane must match", rowA.pattern.right, rowB.pattern.right)

            distA += rowA.gapToNext
            distB += rowB.gapToNext
            time += rowA.gapToNext / speed
        }
    }

    @Test
    fun `SPN-06 and GAM-14 - grace distance 40 m respected`() {
        val director = SpawnDirector(seed = 42L)
        // At start (distance = 0.0), verify that nextSpawnDistance >= 40.0m
        assertTrue(
            "Initial spawn distance ${director.nextSpawnDistance} must be >= grace distance (40.0m)",
            director.nextSpawnDistance >= GameConfig.GRACE_DISTANCE_METRES
        )

        // Run simulation update at start
        director.update(speed = GameConfig.START_SPEED, dt = 0.05f, distanceMetres = 0.0f)
        for (obs in director.activeObstacles) {
            assertTrue(
                "No obstacle at game start may be closer than 40m from player z=0 (was ${abs(obs.z)})",
                abs(obs.z) >= GameConfig.GRACE_DISTANCE_METRES
            )
        }
    }

    @Test
    fun `SPN-07 - row gap formula respected`() {
        val director = SpawnDirector(seed = 31415L)
        var distance = 60.0f
        var time = 0.0f

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)
            val minRequiredGap = maxOf(14.0f, speed * 0.9f)

            assertTrue(
                "Row gap ${row.gapToNext} must be >= min required gap $minRequiredGap at speed $speed",
                row.gapToNext >= minRequiredGap
            )

            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `SPN-08 and GAM-13 - tier gating respected`() {
        val director = SpawnDirector(seed = 27182L)
        var distance = 60.0f
        var time = 0.0f

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)

            when {
                distance < GameConfig.TIER_1_DISTANCE -> {
                    // GAM-13: No obstacles before 60m
                    assertTrue("No obstacles before 60m", false)
                }
                distance < GameConfig.TIER_2_DISTANCE -> {
                    // Tier 1 (60-400m): Exactly 1 obstacle, 2 free lanes, no trains
                    assertEquals("Tier 1 pattern expected at distance $distance", 1, row.pattern.tier)
                    assertEquals("Tier 1 must have exactly 1 obstacle", 1, row.pattern.obstacleCount)
                    assertEquals("Tier 1 must have 2 free lanes", 2, row.pattern.freeLaneCount)
                    assertFalse("Tier 1 must not contain trains", row.pattern.hasTrain)
                }
                distance < GameConfig.TIER_3_DISTANCE -> {
                    // Tier 2 (400-1000m): 2 obstacles with 1 free lane; or single train
                    assertEquals("Tier 2 pattern expected at distance $distance", 2, row.pattern.tier)
                    if (row.pattern.hasTrain) {
                        assertEquals("Tier 2 train row must have exactly 1 train", 1, row.pattern.trainCount)
                        assertEquals("Tier 2 train row must have 2 free lanes", 2, row.pattern.freeLaneCount)
                    } else {
                        assertEquals("Tier 2 non-train row must have exactly 2 obstacles", 2, row.pattern.obstacleCount)
                        assertEquals("Tier 2 non-train row must have 1 free lane", 1, row.pattern.freeLaneCount)
                    }
                }
                else -> {
                    // Tier 3 (1000m+): Combos, double trains, triples
                    assertEquals("Tier 3 pattern expected at distance $distance", 3, row.pattern.tier)
                    assertTrue("Tier 3 row must have survivable option", row.pattern.hasSurvivableLane())
                }
            }

            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `SPN-09 - every pattern in the library is used`() {
        val director = SpawnDirector(seed = 54321L)
        val usedPatternIds = mutableSetOf<String>()

        // Sample across all three tiers so every tier is thoroughly exercised
        // Tier 1: 60m - 400m
        for (i in 0 until 1_000) {
            val dist = 60.0f + (i % 20) * 15.0f
            val row = director.generateNextRow(dist, 10.0f)
            usedPatternIds.add(row.pattern.id)
        }

        // Tier 2: 400m - 1000m
        for (i in 0 until 2_000) {
            val dist = 400.0f + (i % 30) * 18.0f
            val row = director.generateNextRow(dist, 14.0f)
            usedPatternIds.add(row.pattern.id)
        }

        // Tier 3: 1000m+
        for (i in 0 until 2_000) {
            val dist = 1000.0f + (i % 40) * 20.0f
            val row = director.generateNextRow(dist, 18.0f)
            usedPatternIds.add(row.pattern.id)
        }

        val allExpectedIds = PatternLibrary.allPatterns.map { it.id }.toSet()
        val missing = allExpectedIds - usedPatternIds

        assertTrue(
            "All ${allExpectedIds.size} patterns in PatternLibrary must be used over sampled rows. Missing: $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun `SPN-10 - no two obstacles overlap in the same lane`() {
        val director = SpawnDirector(seed = 98765L)
        var distance = 60.0f
        var time = 0.0f

        val laneLastMaxZ = FloatArray(GameConfig.LANE_COUNT) { -Float.MAX_VALUE }

        for (i in 0 until 10_000) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)

            for (lane in 0 until GameConfig.LANE_COUNT) {
                val obsType = row.pattern.getLane(lane)
                if (obsType != null) {
                    val obsMinDist = distance - obsType.length / 2.0f
                    val obsMaxDist = distance + obsType.length / 2.0f

                    assertTrue(
                        "Obstacle $obsType at distance $distance in lane $lane must not overlap previous obstacle (lastMaxDist=${laneLastMaxZ[lane]}, newMinDist=$obsMinDist)",
                        obsMinDist >= laneLastMaxZ[lane]
                    )

                    laneLastMaxZ[lane] = obsMaxDist
                }
            }

            distance += row.gapToNext
            time += row.gapToNext / speed
        }
    }

    @Test
    fun `GAM-15 - all three obstacle types appear within 2 000 m`() {
        val director = SpawnDirector(seed = 1337L)
        var distance = 60.0f
        var time = 0.0f

        var foundBarricade = false
        var foundBeam = false
        var foundTrain = false

        while (distance <= 2000.0f) {
            val speed = Difficulty.calculateSpeed(time)
            val row = director.generateNextRow(distance, speed)

            for (lane in 0 until GameConfig.LANE_COUNT) {
                when (row.pattern.getLane(lane)) {
                    ObstacleType.BARRICADE_LOW -> foundBarricade = true
                    ObstacleType.OVERHEAD_BEAM -> foundBeam = true
                    ObstacleType.TRAIN_BLUE, ObstacleType.TRAIN_ORANGE -> foundTrain = true
                    null -> {}
                }
            }

            distance += row.gapToNext
            time += row.gapToNext / speed
        }

        assertTrue("Barricade must appear within 2 000 m", foundBarricade)
        assertTrue("Overhead beam must appear within 2 000 m", foundBeam)
        assertTrue("Train must appear within 2 000 m", foundTrain)
    }
}
