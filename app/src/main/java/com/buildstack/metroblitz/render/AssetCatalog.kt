package com.buildstack.metroblitz.render

/**
 * Single source of truth for all 3D model asset file paths and animation clip names.
 */
object AssetCatalog {
    // Character models & animations
    const val MODEL_BLITZ = "models/characters/blitz.glb"
    const val CLIP_IDLE = "Idle"
    const val CLIP_RUN = "Run"
    const val CLIP_JUMP = "Jump"
    const val CLIP_SLIDE = "Slide"
    const val CLIP_LEAN_LEFT = "LeanLeft"
    const val CLIP_LEAN_RIGHT = "LeanRight"
    const val CLIP_HIT = "Hit"

    // Environment models
    const val MODEL_TRACK_SEGMENT = "models/environment/track_segment.glb"
    const val MODEL_BUILDING_A = "models/environment/building_a.glb"
    const val MODEL_BUILDING_B = "models/environment/building_b.glb"
    const val MODEL_BUILDING_C = "models/environment/building_c.glb"
    const val MODEL_BUILDING_D = "models/environment/building_d.glb"
    const val MODEL_LAMP_POST = "models/environment/lamp_post.glb"
    const val MODEL_TREE = "models/environment/tree.glb"
    const val MODEL_FENCE = "models/environment/fence.glb"

    // Obstacle models
    const val MODEL_BARRICADE_LOW = "models/obstacles/barricade_low.glb"
    const val MODEL_OVERHEAD_BEAM = "models/obstacles/overhead_beam.glb"
    const val MODEL_TRAIN_BLUE = "models/obstacles/train_blue.glb"
    const val MODEL_TRAIN_ORANGE = "models/obstacles/train_orange.glb"

    // Collectibles
    const val MODEL_COIN = "models/collectibles/coin.glb"
}
