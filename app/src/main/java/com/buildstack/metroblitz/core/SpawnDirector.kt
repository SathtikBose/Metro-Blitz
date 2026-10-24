package com.buildstack.metroblitz.core

/**
 * Metadata for a generated row of obstacles.
 * Pure Kotlin — zero platform imports.
 */
data class SpawnedRow(
    val pattern: RowPattern,
    val distance: Float,
    val speed: Float,
    val gapToNext: Float
)

/**
 * Directs fair, deterministic procedural obstacle spawning along the endless track.
 * Enforces difficulty tiers, row gap formulas, and fairness invariants (SPN-01..10).
 * Pure Kotlin — zero platform imports.
 */
class SpawnDirector(
    val seed: Long = 1337L
) {
    val rng = Rng(seed)
    val activeObstacles = mutableListOf<Obstacle>()

    var nextObstacleId: Int = 1
        private set

    var nextSpawnDistance: Float = GameConfig.TIER_1_DISTANCE
        private set

    var lastSpawnedRow: RowPattern? = null
        private set

    var lastRowHadTrain: Boolean = false
        private set

    fun reset(newSeed: Long = seed) {
        activeObstacles.clear()
        rng.reset(newSeed)
        nextObstacleId = 1
        nextSpawnDistance = GameConfig.TIER_1_DISTANCE
        lastSpawnedRow = null
        lastRowHadTrain = false
    }

    /**
     * Calculates the fair row gap to the next obstacle row according to the GDD formula:
     * max(14 m, speed * 0.9 s + (train-length if previous row had a train))
     * Also enforces SPN-03 landing clearance when a barricade is followed by a beam in the same lane.
     */
    fun calculateRowGap(
        speed: Float,
        pattern: RowPattern,
        candidatePattern: RowPattern? = null
    ): Float {
        val trainExtra = if (pattern.hasTrain) GameConfig.TRAIN_DEPTH else 0.0f
        var gap = maxOf(14.0f, speed * 0.9f + trainExtra)

        // SPN-03: Ensure gap between barricade and beam in the same lane is strictly >= speed * 0.9s
        if (candidatePattern != null) {
            for (lane in 0 until GameConfig.LANE_COUNT) {
                if (pattern.getLane(lane) == ObstacleType.BARRICADE_LOW &&
                    candidatePattern.getLane(lane) == ObstacleType.OVERHEAD_BEAM
                ) {
                    gap = maxOf(gap, speed * 0.9f + 1.0f)
                }
            }
        }

        return gap
    }

    /**
     * Selects a fair pattern for the given tier and applies random mirroring.
     */
    fun selectPattern(tier: Int, currentSpeed: Float, lastRow: RowPattern? = null): RowPattern {
        val patterns = PatternLibrary.getPatternsForTier(tier)
        if (patterns.isEmpty()) {
            return PatternLibrary.tier1Patterns[0]
        }

        val idx = rng.nextInt(0, patterns.size)
        var selected = patterns[idx]

        // 50% chance of mirroring across lanes
        if (rng.nextBoolean()) {
            selected = selected.mirrored()
        }

        return selected
    }

    /**
     * Generates a single row metadata entry deterministically (used by both live spawning and property tests).
     */
    fun generateNextRow(distance: Float, speed: Float): SpawnedRow {
        val tier = Difficulty.tierForDistance(distance)
        val pattern = selectPattern(tier, speed, lastSpawnedRow)
        val gap = calculateRowGap(speed, pattern)

        lastSpawnedRow = pattern
        lastRowHadTrain = pattern.hasTrain

        return SpawnedRow(
            pattern = pattern,
            distance = distance,
            speed = speed,
            gapToNext = gap
        )
    }

    /**
     * Updates obstacle scrolling, despawns passed obstacles, and spawns new rows up to the spawn horizon.
     * Optionally spawns coin arcs over barricades and coin lines/zig-zags in free lanes.
     */
    fun update(speed: Float, dt: Float, distanceMetres: Float, coinSystem: CoinSystem? = null) {
        // 1. Move active obstacles toward player (in +Z direction)
        val it = activeObstacles.iterator()
        while (it.hasNext()) {
            val obs = it.next()
            obs.z += speed * dt

            // Despawn when obstacle has passed behind the player / camera
            if (obs.minZ > GameConfig.DESPAWN_Z_METRES) {
                it.remove()
            }
        }

        // 2. Spawn rows ahead up to spawn horizon
        val spawnLimit = distanceMetres + GameConfig.SPAWN_HORIZON_METRES
        while (nextSpawnDistance <= spawnLimit) {
            val tier = Difficulty.tierForDistance(nextSpawnDistance)
            if (tier == 0) {
                // Tier 0 is warm-up (0-60m), skip to Tier 1 distance
                nextSpawnDistance = GameConfig.TIER_1_DISTANCE
                continue
            }

            val pattern = selectPattern(tier, speed, lastSpawnedRow)
            val spawnZ = -(nextSpawnDistance - distanceMetres)

            val freeLanes = mutableListOf<Int>()

            // Spawn obstacles in lanes
            for (lane in 0 until GameConfig.LANE_COUNT) {
                val type = pattern.getLane(lane)
                if (type != null) {
                    val obs = Obstacle(
                        id = nextObstacleId++,
                        type = type,
                        lane = lane,
                        z = spawnZ
                    )
                    activeObstacles.add(obs)

                    // Arc of coins over barricade
                    if (type == ObstacleType.BARRICADE_LOW && coinSystem != null) {
                        coinSystem.spawnArcOverBarricade(obs)
                    }
                } else {
                    freeLanes.add(lane)
                }
            }

            // Spawn coin lines in a safe lane of the row with 60% probability
            if (coinSystem != null && freeLanes.isNotEmpty() && rng.nextFloat() < 0.60f) {
                val safeLane = freeLanes[rng.nextInt(0, freeLanes.size)]
                coinSystem.spawnLine(
                    lane = safeLane,
                    startZ = spawnZ,
                    count = 5,
                    spacingZ = 2.0f,
                    obstacles = activeObstacles
                )
            }

            val gap = calculateRowGap(speed, pattern)

            // In Tier 2+ with large gaps (>20m), spawn zig-zag in the gap with 40% probability
            if (coinSystem != null && tier >= 2 && gap >= 20.0f && rng.nextFloat() < 0.40f) {
                val zigZagZ = spawnZ - (gap * 0.4f)
                coinSystem.spawnZigZag(
                    startLane = rng.nextInt(0, GameConfig.LANE_COUNT),
                    startZ = zigZagZ,
                    count = 5,
                    spacingZ = 3.0f,
                    obstacles = activeObstacles
                )
            }

            nextSpawnDistance += gap
            lastSpawnedRow = pattern
            lastRowHadTrain = pattern.hasTrain
        }
    }
}
