package com.buildstack.metroblitz.core

/**
 * Represents a single decorative prop positioned relative to a track segment.
 * Pure Kotlin — zero platform imports.
 */
data class SceneryItem(
    val modelId: String,
    val x: Float,
    val y: Float,
    val zOffset: Float, // relative to segment center (-15m to +15m)
    val rotationY: Float = 0.0f
)

/**
 * Represents a 30m track segment with position along the travel axis and attached scenery.
 * Pure Kotlin — zero platform imports.
 */
class TrackSegment(
    var z: Float,
    val decorations: MutableList<SceneryItem> = ArrayList(16)
)

/**
 * Manages track segment positions, recycling, and seeded scenery decorations.
 * Guarantees zero floating-point drift and bounded object coordinates.
 * Pure Kotlin — zero platform imports.
 */
class TrackSystem(
    val poolSize: Int = GameConfig.SEGMENT_POOL_SIZE,
    val segmentLength: Float = GameConfig.SEGMENT_LENGTH_METRES,
    val rng: Rng = Rng(1337L)
) {
    // Models available for decoration
    companion object {
        const val MODEL_BUILDING_A = "models/environment/building_a.glb"
        const val MODEL_BUILDING_B = "models/environment/building_b.glb"
        const val MODEL_BUILDING_C = "models/environment/building_c.glb"
        const val MODEL_BUILDING_D = "models/environment/building_d.glb"
        const val MODEL_LAMP_POST = "models/environment/lamp_post.glb"
        const val MODEL_TREE = "models/environment/tree.glb"
        const val MODEL_FENCE = "models/environment/fence.glb"

        val BUILDING_MODELS = arrayOf(
            MODEL_BUILDING_A,
            MODEL_BUILDING_B,
            MODEL_BUILDING_C,
            MODEL_BUILDING_D
        )
    }

    // 7 segments: initial positions at z = +30.0f, 0.0f, -30.0f, -60.0f, -90.0f, -120.0f, -150.0f
    val segments: Array<TrackSegment> = Array(poolSize) { index ->
        val initialZ = (1 - index) * segmentLength
        val seg = TrackSegment(z = initialZ)
        decorateSegment(seg)
        seg
    }

    /**
     * Updates world track positions by moving segments in the +Z direction toward player.
     * Recycles any segment that has fully passed behind the player (z > 30m).
     */
    fun update(speed: Float, dt: Float) {
        val delta = speed * dt
        for (i in 0 until poolSize) {
            segments[i].z += delta
        }
        recycleBehindSegments()
    }

    fun recycleBehindSegments() {
        for (i in 0 until poolSize) {
            if (segments[i].z > GameConfig.DESPAWN_Z_METRES) {
                // Find farthest segment ahead (minimum Z value)
                var minZ = Float.MAX_VALUE
                for (j in 0 until poolSize) {
                    if (segments[j].z < minZ) {
                        minZ = segments[j].z
                    }
                }
                // Place behind the farthest segment (farthest - 30m)
                segments[i].z = minZ - segmentLength
                decorateSegment(segments[i])
            }
        }
    }

    /**
     * Decorates the segment with buildings, lamps, trees, and fences.
     * All items are strictly placed at |x| >= 3.95m, ensuring zero intrusion on tracks (|x| < 3.4m).
     */
    fun decorateSegment(segment: TrackSegment) {
        segment.decorations.clear()

        // 1. Buildings: 1 on Left (x = -7.2m), 1 on Right (x = +7.2m)
        val leftBuilding = BUILDING_MODELS[rng.nextInt(0, BUILDING_MODELS.size)]
        val rightBuilding = BUILDING_MODELS[rng.nextInt(0, BUILDING_MODELS.size)]
        val bldgOffsetZ = (rng.nextFloat() - 0.5f) * 10.0f

        segment.decorations.add(
            SceneryItem(modelId = leftBuilding, x = -7.2f, y = 0.0f, zOffset = bldgOffsetZ, rotationY = 0.0f)
        )
        segment.decorations.add(
            SceneryItem(modelId = rightBuilding, x = 7.2f, y = 0.0f, zOffset = -bldgOffsetZ, rotationY = 180.0f)
        )

        // 2. Street Lamps: 1-2 per segment at x = ±4.0m
        val lampZ = -8.0f
        segment.decorations.add(
            SceneryItem(modelId = MODEL_LAMP_POST, x = -4.0f, y = 0.2f, zOffset = lampZ, rotationY = 0.0f)
        )
        segment.decorations.add(
            SceneryItem(modelId = MODEL_LAMP_POST, x = 4.0f, y = 0.2f, zOffset = -lampZ, rotationY = 180.0f)
        )

        // 3. Trees: 1-2 per segment at x = ±4.8m
        val treeZ = 6.0f
        segment.decorations.add(
            SceneryItem(modelId = MODEL_TREE, x = -4.8f, y = 0.2f, zOffset = treeZ)
        )
        segment.decorations.add(
            SceneryItem(modelId = MODEL_TREE, x = 4.8f, y = 0.2f, zOffset = -treeZ)
        )

        // 4. Platform Fences: at x = ±3.95m
        segment.decorations.add(
            SceneryItem(modelId = MODEL_FENCE, x = -3.95f, y = 0.2f, zOffset = 0.0f)
        )
        segment.decorations.add(
            SceneryItem(modelId = MODEL_FENCE, x = 3.95f, y = 0.2f, zOffset = 0.0f)
        )
    }

    fun reset(seed: Long = 1337L) {
        rng.reset(seed)
        for (i in 0 until poolSize) {
            segments[i].z = (1 - i) * segmentLength
            decorateSegment(segments[i])
        }
    }
}
