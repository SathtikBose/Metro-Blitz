package com.buildstack.metroblitz.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.util.FpsTracker
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

/**
 * 3D SceneView host composable.
 * Renders the Filament 3D scene and manages per-frame updates.
 */
@Composable
fun GameScene(
    onFrame: (dt: Float, fps: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val fpsTracker = remember { FpsTracker() }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = GameConfig.CAMERA_Y, z = GameConfig.CAMERA_Z)
        lookAt(Position(x = 0.0f, y = GameConfig.CAMERA_LOOK_Y, z = GameConfig.CAMERA_LOOK_Z))
    }

    val spinningCube = remember {
        CubeNode(
            engine = engine,
            size = Float3(1.0f, 1.0f, 1.0f),
            center = Float3(0.0f, 1.0f, 0.0f)
        )
    }

    var rotationY = 0.0f
    var lastNanos = 0L

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(GameConfig.COLOR_SKY_BLUE))
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            childNodes = listOf(spinningCube),
            onFrame = { frameTimeNanos: Long ->
                if (lastNanos != 0L) {
                    val dt = ((frameTimeNanos - lastNanos) / 1_000_000_000.0f).coerceIn(0.0f, 0.05f)
                    rotationY = (rotationY + 90.0f * dt) % 360.0f
                    spinningCube.rotation = Rotation(y = rotationY)
                    fpsTracker.onFrame(frameTimeNanos)
                    onFrame(dt, fpsTracker.currentFps)
                }
                lastNanos = frameTimeNanos
            }
        )
    }
}
