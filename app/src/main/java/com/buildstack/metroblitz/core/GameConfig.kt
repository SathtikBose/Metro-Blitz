package com.buildstack.metroblitz.core

/**
 * Single source of truth for all tunable gameplay, physics, and world configuration values.
 * Pure Kotlin — zero platform imports.
 */
object GameConfig {
    // World coordinates & lanes
    const val LANE_COUNT = 3
    const val LANE_SPACING_METRES = 2.0f
    const val TRACK_WIDTH_METRES = 6.8f

    // Lane index: 0 = left (-2.0m), 1 = middle (0.0m), 2 = right (+2.0m)
    fun laneX(laneIndex: Int): Float = (laneIndex - 1) * LANE_SPACING_METRES

    // Speed progression
    const val START_SPEED = 9.0f // m/s
    const val SPEED_GAIN_PER_SECOND = 0.10f // +m/s per sec
    const val MAX_SPEED = 22.0f // m/s

    // Difficulty tier distance thresholds (metres)
    const val TIER_1_DISTANCE = 60.0f
    const val TIER_2_DISTANCE = 400.0f
    const val TIER_3_DISTANCE = 1000.0f

    // Player movement & physics
    const val LANE_CHANGE_DURATION_SECONDS = 0.12f
    const val JUMP_HEIGHT_METRES = 1.7f
    const val JUMP_DURATION_SECONDS = 0.62f

    // Derived jump physics: h = (1/8) * g * T^2 => g = 8h / T^2, v0 = 4h / T
    val GRAVITY: Float = (8.0f * JUMP_HEIGHT_METRES) / (JUMP_DURATION_SECONDS * JUMP_DURATION_SECONDS)
    val JUMP_INITIAL_VELOCITY: Float = (4.0f * JUMP_HEIGHT_METRES) / JUMP_DURATION_SECONDS

    const val SLIDE_DURATION_SECONDS = 0.65f
    const val FAST_FALL_VELOCITY = -14.0f // m/s
    const val INPUT_BUFFER_SECONDS = 0.15f
    const val MAX_DELTA_TIME = 0.05f // 50 ms max simulation step

    // World generation & pooling
    const val SEGMENT_LENGTH_METRES = 30.0f
    const val SEGMENT_POOL_SIZE = 7
    const val SPAWN_HORIZON_METRES = 120.0f
    const val DESPAWN_Z_METRES = 30.0f
    const val GRACE_DISTANCE_METRES = 40.0f

    // Camera
    const val CAMERA_FOV = 55.0f // vertical degrees
    const val CAMERA_Y = 3.6f
    const val CAMERA_Z = 6.0f
    const val CAMERA_LOOK_Y = 1.0f
    const val CAMERA_LOOK_Z = -8.0f
    const val CAMERA_LERP_FACTOR = 8.0f

    // Hit boxes (width x, height y, depth z in metres)
    const val PLAYER_WIDTH = 0.8f
    const val PLAYER_HEIGHT_STANDING = 1.7f
    const val PLAYER_HEIGHT_SLIDING = 0.8f
    const val PLAYER_DEPTH = 0.6f

    const val BARRICADE_WIDTH = 1.8f
    const val BARRICADE_HEIGHT = 0.9f
    const val BARRICADE_DEPTH = 0.5f

    const val BEAM_WIDTH = 1.8f
    const val BEAM_BOTTOM_Y = 1.1f
    const val BEAM_TOP_Y = 1.6f
    const val BEAM_DEPTH = 0.5f

    const val TRAIN_WIDTH = 1.8f
    const val TRAIN_HEIGHT = 3.0f
    const val TRAIN_DEPTH = 10.0f

    const val COIN_RADIUS = 0.5f
    const val HIT_DURATION_SECONDS = 0.8f
    const val RESUME_COUNTDOWN_SECONDS = 3.0f

    // Debug flags
    var DEBUG_SHOW_HITBOXES: Boolean = false
    var DEBUG_INVINCIBLE: Boolean = false

    // Palette hex colors (ARGB long/int representations)
    const val COLOR_SKY_BLUE = 0xFF4FC3F7
    const val COLOR_SUNNY_YELLOW = 0xFFFFD93D
    const val COLOR_CORAL_RED = 0xFFFF6B6B
    const val COLOR_MINT_GREEN = 0xFF6BCB77
    const val COLOR_ORANGE = 0xFFFF9F43
    const val COLOR_VIOLET = 0xFF7C5CFF
    const val COLOR_CREAM = 0xFFFFF4E0
    const val COLOR_DEEP_NAVY = 0xFF1B1F3B
    const val COLOR_SLATE = 0xFF8D99AE
    const val COLOR_BROWN = 0xFF8B5E3C
    const val COLOR_SKIN = 0xFFF2C29B
    const val COLOR_JEANS_BLUE = 0xFF2F6FED
    const val COLOR_WHITE = 0xFFFFFFFF
}
