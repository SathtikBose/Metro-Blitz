package com.buildstack.metroblitz.render

import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.Obstacle
import com.buildstack.metroblitz.core.ObstacleType
import com.google.android.filament.Engine
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

/**
 * Pre-allocated object pools for 3D obstacle models and debug wireframe/hitbox volumes.
 * Initialized during LOADING state to guarantee zero allocations during gameplay.
 */
class ObstaclePool(
    private val engine: Engine,
    private val modelLoader: ModelLoader
) {
    companion object {
        const val POOL_BARRICADES = 12
        const val POOL_BEAMS = 8
        const val POOL_TRAINS_BLUE = 4
        const val POOL_TRAINS_ORANGE = 4
        private const val PARKED_Y = -100.0f
    }

    // 3D Model nodes
    val barricadeNodes: Array<ModelNode>
    val beamNodes: Array<ModelNode>
    val trainBlueNodes: Array<ModelNode>
    val trainOrangeNodes: Array<ModelNode>

    // Debug Hitbox CubeNodes (debug only)
    val barricadeBoxNodes: Array<CubeNode>
    val beamBoxNodes: Array<CubeNode>
    val trainBlueBoxNodes: Array<CubeNode>
    val trainOrangeBoxNodes: Array<CubeNode>

    init {
        // 1. Barricade nodes
        barricadeNodes = Array(POOL_BARRICADES) {
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_BARRICADE_LOW)
            ModelNode(modelInstance = inst).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }
        barricadeBoxNodes = Array(POOL_BARRICADES) {
            CubeNode(
                engine = engine,
                size = Float3(GameConfig.BARRICADE_WIDTH, GameConfig.BARRICADE_HEIGHT, GameConfig.BARRICADE_DEPTH),
                center = Float3(0.0f, GameConfig.BARRICADE_HEIGHT / 2.0f, 0.0f)
            ).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }

        // 2. Overhead Beam nodes
        beamNodes = Array(POOL_BEAMS) {
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_OVERHEAD_BEAM)
            ModelNode(modelInstance = inst).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }
        val beamBoxHeight = GameConfig.BEAM_TOP_Y - GameConfig.BEAM_BOTTOM_Y
        beamBoxNodes = Array(POOL_BEAMS) {
            CubeNode(
                engine = engine,
                size = Float3(GameConfig.BEAM_WIDTH, beamBoxHeight, GameConfig.BEAM_DEPTH),
                center = Float3(0.0f, GameConfig.BEAM_BOTTOM_Y + beamBoxHeight / 2.0f, 0.0f)
            ).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }

        // 3. Train Blue nodes
        trainBlueNodes = Array(POOL_TRAINS_BLUE) {
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_TRAIN_BLUE)
            ModelNode(modelInstance = inst).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }
        trainBlueBoxNodes = Array(POOL_TRAINS_BLUE) {
            CubeNode(
                engine = engine,
                size = Float3(GameConfig.TRAIN_WIDTH, GameConfig.TRAIN_HEIGHT, GameConfig.TRAIN_DEPTH),
                center = Float3(0.0f, GameConfig.TRAIN_HEIGHT / 2.0f, 0.0f)
            ).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }

        // 4. Train Orange nodes
        trainOrangeNodes = Array(POOL_TRAINS_ORANGE) {
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_TRAIN_ORANGE)
            ModelNode(modelInstance = inst).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }
        trainOrangeBoxNodes = Array(POOL_TRAINS_ORANGE) {
            CubeNode(
                engine = engine,
                size = Float3(GameConfig.TRAIN_WIDTH, GameConfig.TRAIN_HEIGHT, GameConfig.TRAIN_DEPTH),
                center = Float3(0.0f, GameConfig.TRAIN_HEIGHT / 2.0f, 0.0f)
            ).apply {
                position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }
    }

    val allNodes: List<Node> by lazy {
        val list = mutableListOf<Node>()
        list.addAll(barricadeNodes)
        list.addAll(beamNodes)
        list.addAll(trainBlueNodes)
        list.addAll(trainOrangeNodes)
        list.addAll(barricadeBoxNodes)
        list.addAll(beamBoxNodes)
        list.addAll(trainBlueBoxNodes)
        list.addAll(trainOrangeBoxNodes)
        list
    }

    /**
     * Synchronizes 3D obstacle models and hitboxes with core simulation without per-frame allocations.
     */
    fun sync(activeObstacles: List<Obstacle>, showHitboxes: Boolean = false) {
        var barIdx = 0
        var beamIdx = 0
        var trBlueIdx = 0
        var trOrangeIdx = 0

        for (i in activeObstacles.indices) {
            val obs = activeObstacles[i]
            when (obs.type) {
                ObstacleType.BARRICADE_LOW -> {
                    if (barIdx < POOL_BARRICADES) {
                        barricadeNodes[barIdx].position = Position(x = obs.x, y = 0.0f, z = obs.z)
                        val boxY = if (showHitboxes) 0.0f else PARKED_Y
                        barricadeBoxNodes[barIdx].position = Position(x = obs.x, y = boxY, z = obs.z)
                        barIdx++
                    }
                }
                ObstacleType.OVERHEAD_BEAM -> {
                    if (beamIdx < POOL_BEAMS) {
                        beamNodes[beamIdx].position = Position(x = obs.x, y = 0.0f, z = obs.z)
                        val boxY = if (showHitboxes) 0.0f else PARKED_Y
                        beamBoxNodes[beamIdx].position = Position(x = obs.x, y = boxY, z = obs.z)
                        beamIdx++
                    }
                }
                ObstacleType.TRAIN_BLUE -> {
                    if (trBlueIdx < POOL_TRAINS_BLUE) {
                        trainBlueNodes[trBlueIdx].position = Position(x = obs.x, y = 0.0f, z = obs.z)
                        val boxY = if (showHitboxes) 0.0f else PARKED_Y
                        trainBlueBoxNodes[trBlueIdx].position = Position(x = obs.x, y = boxY, z = obs.z)
                        trBlueIdx++
                    }
                }
                ObstacleType.TRAIN_ORANGE -> {
                    if (trOrangeIdx < POOL_TRAINS_ORANGE) {
                        trainOrangeNodes[trOrangeIdx].position = Position(x = obs.x, y = 0.0f, z = obs.z)
                        val boxY = if (showHitboxes) 0.0f else PARKED_Y
                        trainOrangeBoxNodes[trOrangeIdx].position = Position(x = obs.x, y = boxY, z = obs.z)
                        trOrangeIdx++
                    }
                }
            }
        }

        // Park unused nodes off-screen
        while (barIdx < POOL_BARRICADES) {
            barricadeNodes[barIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            barricadeBoxNodes[barIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            barIdx++
        }
        while (beamIdx < POOL_BEAMS) {
            beamNodes[beamIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            beamBoxNodes[beamIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            beamIdx++
        }
        while (trBlueIdx < POOL_TRAINS_BLUE) {
            trainBlueNodes[trBlueIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            trainBlueBoxNodes[trBlueIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            trBlueIdx++
        }
        while (trOrangeIdx < POOL_TRAINS_ORANGE) {
            trainOrangeNodes[trOrangeIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            trainOrangeBoxNodes[trOrangeIdx].position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            trOrangeIdx++
        }
    }
}
