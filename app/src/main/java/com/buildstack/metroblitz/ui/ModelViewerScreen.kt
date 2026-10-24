package com.buildstack.metroblitz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.render.AssetCatalog
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

/**
 * Debug Model Viewer screen (debug builds only) for testing 3D assets and animation clips.
 */
@Composable
fun ModelViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = 1.0f, z = 3.5f)
        lookAt(Position(x = 0.0f, y = 0.85f, z = 0.0f))
    }

    var selectedClip by remember { mutableStateOf(AssetCatalog.CLIP_IDLE) }
    var autoRotate by remember { mutableStateOf(true) }
    var currentRotationY by remember { mutableFloatStateOf(0.0f) }

    val modelNode = remember {
        val instance = modelLoader.createModelInstance(AssetCatalog.MODEL_BLITZ)
        if (instance != null) {
            ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.7f
            ).apply {
                position = Position(x = 0.0f, y = 0.0f, z = 0.0f)
                playAnimation(AssetCatalog.CLIP_IDLE, loop = true)
            }
        } else {
            null
        }
    }

    val clips = listOf(
        AssetCatalog.CLIP_IDLE,
        AssetCatalog.CLIP_RUN,
        AssetCatalog.CLIP_JUMP,
        AssetCatalog.CLIP_SLIDE,
        AssetCatalog.CLIP_LEAN_LEFT,
        AssetCatalog.CLIP_LEAN_RIGHT,
        AssetCatalog.CLIP_HIT
    )

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
            childNodes = listOfNotNull(modelNode),
            onFrame = { frameTimeNanos ->
                if (lastNanos != 0L && autoRotate) {
                    val dt = ((frameTimeNanos - lastNanos) / 1_000_000_000.0f).coerceIn(0.0f, 0.05f)
                    currentRotationY = (currentRotationY + 45.0f * dt) % 360.0f
                    modelNode.rotation = Rotation(y = currentRotationY)
                }
                lastNanos = frameTimeNanos
            }
        )

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODEL VIEWER: BLITZ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(GameConfig.COLOR_CORAL_RED))
            ) {
                Text(text = "Back", color = Color.White)
            }
        }

        // Bottom Animation Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.85f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Clip: $selectedClip",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(GameConfig.COLOR_SUNNY_YELLOW)
                )

                Button(
                    onClick = { autoRotate = !autoRotate },
                    colors = ButtonDefaults.buttonColors(containerColor = if (autoRotate) Color(GameConfig.COLOR_MINT_GREEN) else Color(GameConfig.COLOR_SLATE))
                ) {
                    Text(text = if (autoRotate) "Rotate: ON" else "Rotate: OFF", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                clips.forEach { clip ->
                    val isSelected = clip == selectedClip
                    Button(
                        onClick = {
                            selectedClip = clip
                            val isLoop = (clip == AssetCatalog.CLIP_IDLE || clip == AssetCatalog.CLIP_RUN)
                            modelNode?.playAnimation(clip, loop = isLoop)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(GameConfig.COLOR_SUNNY_YELLOW) else Color.White
                        )
                    ) {
                        Text(
                            text = clip,
                            color = Color(GameConfig.COLOR_DEEP_NAVY),
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
