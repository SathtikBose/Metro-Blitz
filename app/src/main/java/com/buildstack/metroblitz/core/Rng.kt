package com.buildstack.metroblitz.core

import kotlin.random.Random

/**
 * Seeded deterministic pseudo-random number generator wrapper.
 * Pure Kotlin — zero platform imports.
 */
class Rng(seed: Long = 1337L) {
    private var random: Random = Random(seed)

    fun reset(seed: Long) {
        random = Random(seed)
    }

    fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)
    fun nextFloat(): Float = random.nextFloat()
    fun nextBoolean(): Boolean = random.nextBoolean()
}
