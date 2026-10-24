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
import androidx.compose.runtime.LaunchedEffect
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

enum class ViewerTab {
    BLITZ,
    STREET,
    PROPS
}

/**
 * Debug Model Viewer screen (debug builds only) for testing 3D assets, animations, and environment.
 */
@Composable
fun ModelViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine)

    var currentTab by remember { mutableStateOf(ViewerTab.BLITZ) }
    var selectedClip by remember { mutableStateOf(AssetCatalog.CLIP_IDLE) }
    var selectedProp by remember { mutableStateOf(AssetCatalog.MODEL_TRACK_SEGMENT) }
    var autoRotate by remember { mutableStateOf(true) }
    var currentRotationY by remember { mutableFloatStateOf(0.0f) }

    // Update camera per tab
    LaunchedEffect(currentTab) {
        when (currentTab) {
            ViewerTab.BLITZ -> {
                cameraNode.position = Position(x = 0.0f, y = 1.0f, z = 3.5f)
                cameraNode.lookAt(Position(x = 0.0f, y = 0.85f, z = 0.0f))
            }
            ViewerTab.STREET -> {
                cameraNode.position = Position(x = 0.0f, y = 3.2f, z = 22.0f)
                cameraNode.lookAt(Position(x = 0.0f, y = 1.0f, z = -15.0f))
            }
            ViewerTab.PROPS -> {
                cameraNode.position = Position(x = 0.0f, y = 3.5f, z = 9.0f)
                cameraNode.lookAt(Position(x = 0.0f, y = 2.0f, z = 0.0f))
            }
        }
    }

    // Blitz Node
    val blitzNode = remember {
        val instance = modelLoader.createModelInstance(AssetCatalog.MODEL_BLITZ)
        ModelNode(
            modelInstance = instance,
            scaleToUnits = 1.7f
        ).apply {
            position = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            playAnimation(AssetCatalog.CLIP_IDLE, loop = true)
        }
    }

    // Street Environment Nodes (3 segments tiling + scenery)
    val streetNodes = remember {
        val nodes = mutableListOf<ModelNode>()

        // 3 consecutive 30m track segments
        listOf(30.0f, 0.0f, -30.0f).forEach { zOffset ->
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_TRACK_SEGMENT)
            nodes.add(ModelNode(modelInstance = inst).apply {
                position = Position(x = 0.0f, y = 0.0f, z = zOffset)
            })
        }

        // Buildings along left and right sides (|x| >= 4)
        val buildings = listOf(
            Triple(AssetCatalog.MODEL_BUILDING_A, -7.5f, 0.0f),
            Triple(AssetCatalog.MODEL_BUILDING_B, 7.5f, -15.0f),
            Triple(AssetCatalog.MODEL_BUILDING_C, -7.5f, -25.0f),
            Triple(AssetCatalog.MODEL_BUILDING_D, 7.5f, 15.0f)
        )
        buildings.forEach { (model, x, z) ->
            val inst = modelLoader.createModelInstance(model)
            nodes.add(ModelNode(modelInstance = inst).apply {
                position = Position(x = x, y = 0.0f, z = z)
                if (x > 0) rotation = Rotation(y = 180.0f)
            })
        }

        // Street lamps
        listOf(Pair(-4.0f, 5.0f), Pair(4.0f, -10.0f), Pair(-4.0f, -20.0f)).forEach { (x, z) ->
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_LAMP_POST)
            nodes.add(ModelNode(modelInstance = inst).apply {
                position = Position(x = x, y = 0.2f, z = z)
                if (x > 0) rotation = Rotation(y = 180.0f)
            })
        }

        // Trees
        listOf(Pair(-4.8f, -10.0f), Pair(4.8f, 8.0f), Pair(-4.8f, 18.0f)).forEach { (x, z) ->
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_TREE)
            nodes.add(ModelNode(modelInstance = inst).apply {
                position = Position(x = x, y = 0.2f, z = z)
            })
        }

        // Fences
        listOf(Pair(-3.95f, -2.0f), Pair(3.95f, -2.0f), Pair(-3.95f, 1.0f), Pair(3.95f, 1.0f)).forEach { (x, z) ->
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_FENCE)
            nodes.add(ModelNode(modelInstance = inst).apply {
                position = Position(x = x, y = 0.2f, z = z)
            })
        }

        nodes
    }

    // Individual Prop Node for inspection
    var propNode by remember {
        val inst = modelLoader.createModelInstance(selectedProp)
        mutableStateOf(ModelNode(modelInstance = inst).apply {
            position = Position(x = 0.0f, y = 0.0f, z = 0.0f)
        })
    }

    LaunchedEffect(selectedProp) {
        val inst = modelLoader.createModelInstance(selectedProp)
        propNode = ModelNode(modelInstance = inst).apply {
            position = Position(x = 0.0f, y = 0.0f, z = 0.0f)
        }
    }

    val activeNodes = when (currentTab) {
        ViewerTab.BLITZ -> listOf(blitzNode)
        ViewerTab.STREET -> streetNodes
        ViewerTab.PROPS -> listOf(propNode)
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

    val propsList = listOf(
        "Track" to AssetCatalog.MODEL_TRACK_SEGMENT,
        "Build A" to AssetCatalog.MODEL_BUILDING_A,
        "Build B" to AssetCatalog.MODEL_BUILDING_B,
        "Build C" to AssetCatalog.MODEL_BUILDING_C,
        "Build D" to AssetCatalog.MODEL_BUILDING_D,
        "Lamp" to AssetCatalog.MODEL_LAMP_POST,
        "Tree" to AssetCatalog.MODEL_TREE,
        "Fence" to AssetCatalog.MODEL_FENCE
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
            childNodes = activeNodes,
            onFrame = { frameTimeNanos ->
                if (lastNanos != 0L && autoRotate) {
                    val dt = ((frameTimeNanos - lastNanos) / 1_000_000_000.0f).coerceIn(0.0f, 0.05f)
                    currentRotationY = (currentRotationY + 45.0f * dt) % 360.0f
                    if (currentTab == ViewerTab.BLITZ) {
                        blitzNode.rotation = Rotation(y = currentRotationY)
                    } else if (currentTab == ViewerTab.PROPS) {
                        propNode.rotation = Rotation(y = currentRotationY)
                    }
                }
                lastNanos = frameTimeNanos
            }
        )

        // Top Header & Tab Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3D VIEWER: ${currentTab.name}",
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

            Spacer(modifier = Modifier.height(8.dp))

            // Tab bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewerTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab
                    Button(
                        onClick = {
                            currentTab = tab
                            currentRotationY = 0.0f
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(GameConfig.COLOR_SUNNY_YELLOW) else Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = tab.name,
                            color = if (isSelected) Color(GameConfig.COLOR_DEEP_NAVY) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Bottom Controls depending on active tab
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(GameConfig.COLOR_DEEP_NAVY).copy(alpha = 0.85f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            when (currentTab) {
                ViewerTab.BLITZ -> {
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
                                    blitzNode.playAnimation(clip, loop = isLoop)
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

                ViewerTab.STREET -> {
                    Text(
                        text = "Endless Track Preview: 3 Seamless 30m Segments",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(GameConfig.COLOR_SUNNY_YELLOW)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ballast, rails, sleepers, platforms, buildings, trees, lamps & fences.",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                ViewerTab.PROPS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prop: ${propsList.firstOrNull { it.second == selectedProp }?.first ?: ""}",
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
                        propsList.forEach { (label, modelPath) ->
                            val isSelected = modelPath == selectedProp
                            Button(
                                onClick = { selectedProp = modelPath },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(GameConfig.COLOR_SUNNY_YELLOW) else Color.White
                                )
                            ) {
                                Text(
                                    text = label,
                                    color = Color(GameConfig.COLOR_DEEP_NAVY),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
