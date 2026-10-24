package com.buildstack.metroblitz.render

import com.buildstack.metroblitz.core.GameConfig

/**
 * Camera positioning, smooth follow on X, and hit shake.
 */
class CameraRig {
    var currentX: Float = 0.0f
        private set

    fun update(playerX: Float, dt: Float) {
        val targetX = playerX * 0.35f
        currentX += (targetX - currentX) * (GameConfig.CAMERA_LERP_FACTOR * dt).coerceAtMost(1.0f)
    }
}
