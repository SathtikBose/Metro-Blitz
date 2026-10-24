package com.buildstack.metroblitz.core

/**
 * Types of obstacles that can spawn in the endless world.
 * Pure Kotlin — zero platform imports.
 */
enum class ObstacleType(
    val width: Float,
    val height: Float,
    val length: Float,
    val isJumpClearable: Boolean,
    val isSlideClearable: Boolean
) {
    BARRICADE_LOW(
        width = GameConfig.BARRICADE_WIDTH,
        height = GameConfig.BARRICADE_HEIGHT,
        length = GameConfig.BARRICADE_DEPTH,
        isJumpClearable = true,
        isSlideClearable = false
    ),
    OVERHEAD_BEAM(
        width = GameConfig.BEAM_WIDTH,
        height = GameConfig.BEAM_TOP_Y,
        length = GameConfig.BEAM_DEPTH,
        isJumpClearable = false,
        isSlideClearable = true
    ),
    TRAIN_BLUE(
        width = GameConfig.TRAIN_WIDTH,
        height = GameConfig.TRAIN_HEIGHT,
        length = GameConfig.TRAIN_DEPTH,
        isJumpClearable = false,
        isSlideClearable = false
    ),
    TRAIN_ORANGE(
        width = GameConfig.TRAIN_WIDTH,
        height = GameConfig.TRAIN_HEIGHT,
        length = GameConfig.TRAIN_DEPTH,
        isJumpClearable = false,
        isSlideClearable = false
    );

    val isTrain: Boolean
        get() = this == TRAIN_BLUE || this == TRAIN_ORANGE
}
