package com.buildstack.metroblitz.core

/**
 * Row pattern defining obstacle placement across 3 lanes (0 = Left, 1 = Middle, 2 = Right).
 * Pure Kotlin — zero platform imports.
 */
data class RowPattern(
    val id: String,
    val tier: Int,
    val left: ObstacleType?,
    val middle: ObstacleType?,
    val right: ObstacleType?
) {
    fun getLane(lane: Int): ObstacleType? = when (lane) {
        0 -> left
        1 -> middle
        2 -> right
        else -> null
    }

    val hasTrain: Boolean
        get() = left?.isTrain == true || middle?.isTrain == true || right?.isTrain == true

    val trainCount: Int
        get() = (if (left?.isTrain == true) 1 else 0) +
                (if (middle?.isTrain == true) 1 else 0) +
                (if (right?.isTrain == true) 1 else 0)

    val obstacleCount: Int
        get() = (if (left != null) 1 else 0) +
                (if (middle != null) 1 else 0) +
                (if (right != null) 1 else 0)

    val freeLaneCount: Int
        get() = 3 - obstacleCount

    fun mirrored(): RowPattern = RowPattern(
        id = id,
        tier = tier,
        left = right,
        middle = middle,
        right = left
    )

    /**
     * Checks if this row pattern has at least one survivable lane.
     * Passable lanes include: empty (run through), barricade (jump), beam (slide).
     */
    fun hasSurvivableLane(): Boolean {
        return isLanePassable(left) || isLanePassable(middle) || isLanePassable(right)
    }

    /**
     * Checks if at least one lane is free of trains when trains are present.
     */
    fun hasTrainFreeLane(): Boolean {
        return left?.isTrain != true || middle?.isTrain != true || right?.isTrain != true
    }

    companion object {
        fun isLanePassable(obstacle: ObstacleType?): Boolean {
            return obstacle == null || obstacle.isJumpClearable || obstacle.isSlideClearable
        }
    }
}

/**
 * Curated catalog of obstacle row patterns grouped by difficulty tier.
 * Pure Kotlin — zero platform imports.
 */
object PatternLibrary {

    val tier1Patterns = listOf(
        RowPattern("T1_BAR_L", 1, ObstacleType.BARRICADE_LOW, null, null),
        RowPattern("T1_BAR_M", 1, null, ObstacleType.BARRICADE_LOW, null),
        RowPattern("T1_BAR_R", 1, null, null, ObstacleType.BARRICADE_LOW),
        RowPattern("T1_BEAM_L", 1, ObstacleType.OVERHEAD_BEAM, null, null),
        RowPattern("T1_BEAM_M", 1, null, ObstacleType.OVERHEAD_BEAM, null),
        RowPattern("T1_BEAM_R", 1, null, null, ObstacleType.OVERHEAD_BEAM)
    )

    val tier2Patterns = listOf(
        RowPattern("T2_DBL_BAR_SIDES", 2, ObstacleType.BARRICADE_LOW, null, ObstacleType.BARRICADE_LOW),
        RowPattern("T2_DBL_BAR_LM", 2, ObstacleType.BARRICADE_LOW, ObstacleType.BARRICADE_LOW, null),
        RowPattern("T2_DBL_BAR_MR", 2, null, ObstacleType.BARRICADE_LOW, ObstacleType.BARRICADE_LOW),
        RowPattern("T2_DBL_BEAM_SIDES", 2, ObstacleType.OVERHEAD_BEAM, null, ObstacleType.OVERHEAD_BEAM),
        RowPattern("T2_DBL_BEAM_LM", 2, ObstacleType.OVERHEAD_BEAM, ObstacleType.OVERHEAD_BEAM, null),
        RowPattern("T2_DBL_BEAM_MR", 2, null, ObstacleType.OVERHEAD_BEAM, ObstacleType.OVERHEAD_BEAM),
        RowPattern("T2_MIX_BAR_BEAM_LM", 2, ObstacleType.BARRICADE_LOW, ObstacleType.OVERHEAD_BEAM, null),
        RowPattern("T2_MIX_BEAM_BAR_LM", 2, ObstacleType.OVERHEAD_BEAM, ObstacleType.BARRICADE_LOW, null),
        RowPattern("T2_MIX_BAR_BEAM_SIDES", 2, ObstacleType.BARRICADE_LOW, null, ObstacleType.OVERHEAD_BEAM),
        RowPattern("T2_TRAIN_BLUE_L", 2, ObstacleType.TRAIN_BLUE, null, null),
        RowPattern("T2_TRAIN_BLUE_M", 2, null, ObstacleType.TRAIN_BLUE, null),
        RowPattern("T2_TRAIN_BLUE_R", 2, null, null, ObstacleType.TRAIN_BLUE),
        RowPattern("T2_TRAIN_ORANGE_L", 2, ObstacleType.TRAIN_ORANGE, null, null),
        RowPattern("T2_TRAIN_ORANGE_M", 2, null, ObstacleType.TRAIN_ORANGE, null),
        RowPattern("T2_TRAIN_ORANGE_R", 2, null, null, ObstacleType.TRAIN_ORANGE)
    )

    val tier3Patterns = listOf(
        RowPattern("T3_TR_BLUE_L_BAR_M", 3, ObstacleType.TRAIN_BLUE, ObstacleType.BARRICADE_LOW, null),
        RowPattern("T3_TR_BLUE_R_BEAM_M", 3, null, ObstacleType.OVERHEAD_BEAM, ObstacleType.TRAIN_BLUE),
        RowPattern("T3_TR_ORANGE_L_BEAM_M", 3, ObstacleType.TRAIN_ORANGE, ObstacleType.OVERHEAD_BEAM, null),
        RowPattern("T3_TR_ORANGE_R_BAR_M", 3, null, ObstacleType.BARRICADE_LOW, ObstacleType.TRAIN_ORANGE),
        RowPattern("T3_TR_BLUE_M_BAR_L", 3, ObstacleType.BARRICADE_LOW, ObstacleType.TRAIN_BLUE, null),
        RowPattern("T3_TR_ORANGE_M_BEAM_L", 3, ObstacleType.OVERHEAD_BEAM, ObstacleType.TRAIN_ORANGE, null),
        RowPattern("T3_DBL_TR_SIDES", 3, ObstacleType.TRAIN_BLUE, null, ObstacleType.TRAIN_ORANGE),
        RowPattern("T3_DBL_TR_LM", 3, ObstacleType.TRAIN_BLUE, ObstacleType.TRAIN_ORANGE, null),
        RowPattern("T3_DBL_TR_MR", 3, null, ObstacleType.TRAIN_BLUE, ObstacleType.TRAIN_ORANGE),
        RowPattern("T3_MIX_ALL_BAR_BEAM", 3, ObstacleType.BARRICADE_LOW, ObstacleType.OVERHEAD_BEAM, ObstacleType.BARRICADE_LOW),
        RowPattern("T3_MIX_ALL_BEAM_BAR", 3, ObstacleType.OVERHEAD_BEAM, ObstacleType.BARRICADE_LOW, ObstacleType.OVERHEAD_BEAM),
        RowPattern("T3_TR_ORANGE_L_ALL_PASS", 3, ObstacleType.TRAIN_ORANGE, ObstacleType.BARRICADE_LOW, ObstacleType.OVERHEAD_BEAM)
    )

    val allPatterns = tier1Patterns + tier2Patterns + tier3Patterns

    fun getPatternsForTier(tier: Int): List<RowPattern> = when (tier) {
        1 -> tier1Patterns
        2 -> tier2Patterns
        3 -> tier3Patterns
        else -> emptyList()
    }
}
