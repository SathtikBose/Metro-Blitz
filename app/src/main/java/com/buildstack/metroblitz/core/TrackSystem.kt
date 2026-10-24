package com.buildstack.metroblitz.core

/**
 * Manages track segment positions and recycling data.
 * Pure Kotlin — zero platform imports.
 */
class TrackSystem(
    val poolSize: Int = GameConfig.SEGMENT_POOL_SIZE,
    val segmentLength: Float = GameConfig.SEGMENT_LENGTH_METRES
) {
    // Array of segment Z positions
    val segmentPositionsZ = FloatArray(poolSize) { index -> -index * segmentLength }

    fun update(speed: Float, dt: Float) {
        val delta = speed * dt
        for (i in 0 until poolSize) {
            segmentPositionsZ[i] += delta
        }
        recycleBehindSegments()
    }

    fun recycleBehindSegments() {
        for (i in 0 until poolSize) {
            if (segmentPositionsZ[i] > GameConfig.DESPAWN_Z_METRES) {
                var minZ = 0.0f
                for (j in 0 until poolSize) {
                    if (segmentPositionsZ[j] < minZ) minZ = segmentPositionsZ[j]
                }
                segmentPositionsZ[i] = minZ - segmentLength
            }
        }
    }
}
