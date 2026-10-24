package com.buildstack.metroblitz.render

import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.GameState
import io.github.sceneview.math.Position
import io.github.sceneview.node.CameraNode

/**
 * Camera controller managing menu presentation, gameplay follow, and pull-back transitions.
 */
class CameraRig {
    var posX: Float = 0.0f
    var posY: Float = 1.3f
    var posZ: Float = 3.6f

    var lookX: Float = 0.0f
    var lookY: Float = 0.85f
    var lookZ: Float = 0.0f

    fun update(cameraNode: CameraNode, state: GameState, playerX: Float, dt: Float) {
        val targetX = playerX * 0.35f
        posX += (targetX - posX) * (GameConfig.CAMERA_LERP_FACTOR * dt).coerceIn(0.0f, 1.0f)
        lookX = posX

        if (state == GameState.MENU) {
            // Closer hero shot for menu
            val lerp = (5.0f * dt).coerceIn(0.0f, 1.0f)
            posY += (1.3f - posY) * lerp
            posZ += (3.6f - posZ) * lerp
            lookY += (0.85f - lookY) * lerp
            lookZ += (0.0f - lookZ) * lerp
        } else {
            // Gameplay pull-back follow
            val lerp = (4.0f * dt).coerceIn(0.0f, 1.0f)
            posY += (GameConfig.CAMERA_Y - posY) * lerp
            posZ += (GameConfig.CAMERA_Z - posZ) * lerp
            lookY += (GameConfig.CAMERA_LOOK_Y - lookY) * lerp
            lookZ += (GameConfig.CAMERA_LOOK_Z - lookZ) * lerp
        }

        cameraNode.position = Position(x = posX, y = posY, z = posZ)
        cameraNode.lookAt(Position(x = lookX, y = lookY, z = lookZ))
    }

    fun resetToMenu(cameraNode: CameraNode) {
        posX = 0.0f
        posY = 1.3f
        posZ = 3.6f
        lookX = 0.0f
        lookY = 0.85f
        lookZ = 0.0f
        cameraNode.position = Position(x = posX, y = posY, z = posZ)
        cameraNode.lookAt(Position(x = lookX, y = lookY, z = lookZ))
    }
}
