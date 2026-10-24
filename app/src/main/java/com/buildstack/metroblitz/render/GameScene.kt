package com.buildstack.metroblitz.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.GameState
import com.buildstack.metroblitz.ui.GameViewModel
import com.buildstack.metroblitz.util.FpsTracker
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

/**
 * 3D SceneView host composable.
 * Renders the endless runner 3D scene (Blitz hero, 7 track segments, scenery props)
 * and synchronizes simulation state with zero per-frame allocations.
 */
@Composable
fun GameScene(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val fpsTracker = remember { FpsTracker() }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine)
    val cameraRig = remember { CameraRig() }

    val playerNode = remember { PlayerNode.create(modelLoader) }
    val modelPool = remember { ModelPool(modelLoader) }
    val allSceneNodes: List<Node> = remember {
        listOf(playerNode.modelNode) + modelPool.allNodes
    }

    var lastClip by remember { mutableStateOf("") }
    var lastNanos by remember { mutableStateOf(0L) }

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
            childNodes = allSceneNodes,
            onFrame = { frameTimeNanos: Long ->
                if (lastNanos != 0L) {
                    val rawDt = ((frameTimeNanos - lastNanos) / 1_000_000_000.0f).coerceIn(0.0f, GameConfig.MAX_DELTA_TIME)
                    fpsTracker.onFrame(frameTimeNanos)
                    val fps = fpsTracker.currentFps
                    viewModel.onFrame(rawDt, fps)

                    val world = viewModel.world
                    val snapshot = world.snapshot()

                    // Synchronize track segments and scenery props
                    modelPool.sync(world.trackSystem)

                    // Synchronize character position and animation
                    playerNode.modelNode.position = Position(x = snapshot.playerX, y = snapshot.playerY, z = snapshot.playerZ)

                    val targetClip = if (snapshot.state == GameState.PLAYING) AssetCatalog.CLIP_RUN else AssetCatalog.CLIP_IDLE
                    val animSpeed = if (snapshot.state == GameState.PLAYING) (snapshot.speed / GameConfig.START_SPEED) else 1.0f

                    if (targetClip != lastClip || targetClip == AssetCatalog.CLIP_RUN) {
                        playerNode.play(targetClip, loop = true, speed = animSpeed)
                        lastClip = targetClip
                    }

                    // Synchronize camera
                    cameraRig.update(cameraNode, snapshot.state, snapshot.playerX, rawDt)
                }
                lastNanos = frameTimeNanos
            }
        )
    }
}
