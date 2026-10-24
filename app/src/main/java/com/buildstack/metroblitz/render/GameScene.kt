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
import com.buildstack.metroblitz.core.PlayerAction
import com.buildstack.metroblitz.ui.GameViewModel
import com.buildstack.metroblitz.util.FpsTracker
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

/**
 * 3D SceneView host composable.
 * Renders the endless runner 3D scene (Blitz hero, blob shadow, 7 track segments, scenery props)
 * and synchronizes simulation state and animation clips with zero per-frame allocations.
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
    val blobShadowNode = remember {
        CubeNode(
            engine = engine,
            size = Float3(0.6f, 0.01f, 0.6f),
            center = Float3(0.0f, 0.005f, 0.0f)
        )
    }
    val modelPool = remember { ModelPool(modelLoader) }
    val allSceneNodes: List<Node> = remember {
        listOf(playerNode.modelNode, blobShadowNode) + modelPool.allNodes
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

                    // Synchronize character position
                    playerNode.modelNode.position = Position(x = snapshot.playerX, y = snapshot.playerY, z = snapshot.playerZ)

                    // Determine character animation clip based on player action
                    val targetClip = when {
                        snapshot.state == GameState.HIT -> AssetCatalog.CLIP_HIT
                        snapshot.state == GameState.MENU -> AssetCatalog.CLIP_IDLE
                        snapshot.playerAction == PlayerAction.JUMPING -> AssetCatalog.CLIP_JUMP
                        snapshot.playerAction == PlayerAction.SLIDING -> AssetCatalog.CLIP_SLIDE
                        snapshot.isChangingLane -> if (snapshot.laneChangeDirection < 0) AssetCatalog.CLIP_LEAN_LEFT else AssetCatalog.CLIP_LEAN_RIGHT
                        else -> AssetCatalog.CLIP_RUN
                    }

                    val isLoop = (targetClip == AssetCatalog.CLIP_RUN || targetClip == AssetCatalog.CLIP_IDLE)
                    val animSpeed = if (targetClip == AssetCatalog.CLIP_RUN) (snapshot.speed / GameConfig.START_SPEED) else 1.0f

                    if (targetClip != lastClip || targetClip == AssetCatalog.CLIP_RUN) {
                        playerNode.play(targetClip, loop = isLoop, speed = animSpeed)
                        lastClip = targetClip
                    }

                    // Additive lean tilt during lane change
                    val tiltZ = if (snapshot.isChangingLane) {
                        if (snapshot.laneChangeDirection < 0) 12.0f else -12.0f
                    } else {
                        0.0f
                    }
                    playerNode.modelNode.rotation = Rotation(x = 0.0f, y = 0.0f, z = tiltZ)

                    // Synchronize blob shadow position and size beneath player
                    val shadowScale = (1.0f - (snapshot.playerY / GameConfig.JUMP_HEIGHT_METRES) * 0.4f).coerceIn(0.5f, 1.0f)
                    blobShadowNode.position = Position(x = snapshot.playerX, y = 0.01f, z = snapshot.playerZ)
                    blobShadowNode.scale = Float3(shadowScale, 1.0f, shadowScale)

                    // Synchronize camera
                    cameraRig.update(cameraNode, snapshot.state, snapshot.playerX, rawDt)
                }
                lastNanos = frameTimeNanos
            }
        )
    }
}
