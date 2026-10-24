package com.buildstack.metroblitz.render

import com.buildstack.metroblitz.core.GameConfig
import com.buildstack.metroblitz.core.TrackSystem
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

/**
 * Preloaded object pools for track segments and scenery props.
 * Initialized during LOADING state to ensure zero allocations during gameplay.
 */
class ModelPool(
    private val modelLoader: ModelLoader
) {
    val trackNodes: Array<ModelNode>
    val propNodes: Array<List<ModelNode>>

    init {
        val tracks = Array(GameConfig.SEGMENT_POOL_SIZE) { i ->
            val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_TRACK_SEGMENT)
            ModelNode(modelInstance = inst).apply {
                position = Position(x = 0.0f, y = 0.0f, z = (1 - i) * GameConfig.SEGMENT_LENGTH_METRES)
            }
        }
        trackNodes = tracks

        // Building variants to distribute across the 7 segments
        val buildingPairs = listOf(
            Pair(AssetCatalog.MODEL_BUILDING_A, AssetCatalog.MODEL_BUILDING_B),
            Pair(AssetCatalog.MODEL_BUILDING_C, AssetCatalog.MODEL_BUILDING_D),
            Pair(AssetCatalog.MODEL_BUILDING_A, AssetCatalog.MODEL_BUILDING_C),
            Pair(AssetCatalog.MODEL_BUILDING_B, AssetCatalog.MODEL_BUILDING_D),
            Pair(AssetCatalog.MODEL_BUILDING_C, AssetCatalog.MODEL_BUILDING_A),
            Pair(AssetCatalog.MODEL_BUILDING_D, AssetCatalog.MODEL_BUILDING_B),
            Pair(AssetCatalog.MODEL_BUILDING_B, AssetCatalog.MODEL_BUILDING_C)
        )

        val props = Array<List<ModelNode>>(GameConfig.SEGMENT_POOL_SIZE) { i ->
            val segmentProps = mutableListOf<ModelNode>()
            val pair = buildingPairs[i % buildingPairs.size]

            // 0: Left Building
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(pair.first)))
            // 1: Right Building
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(pair.second)).apply {
                rotation = Rotation(y = 180.0f)
            })
            // 2: Left Lamp
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(AssetCatalog.MODEL_LAMP_POST)))
            // 3: Right Lamp
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(AssetCatalog.MODEL_LAMP_POST)).apply {
                rotation = Rotation(y = 180.0f)
            })
            // 4: Left Tree
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(AssetCatalog.MODEL_TREE)))
            // 5: Right Tree
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(AssetCatalog.MODEL_TREE)))
            // 6: Left Fence
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(AssetCatalog.MODEL_FENCE)))
            // 7: Right Fence
            segmentProps.add(ModelNode(modelInstance = modelLoader.createModelInstance(AssetCatalog.MODEL_FENCE)))

            segmentProps
        }
        propNodes = props
    }

    val allNodes: List<Node> by lazy {
        val list = mutableListOf<Node>()
        for (i in 0 until GameConfig.SEGMENT_POOL_SIZE) {
            list.add(trackNodes[i])
            list.addAll(propNodes[i])
        }
        list
    }

    /**
     * Synchronizes 3D node positions with core simulation without per-frame allocations.
     */
    fun sync(trackSystem: TrackSystem) {
        for (i in 0 until GameConfig.SEGMENT_POOL_SIZE) {
            val seg = trackSystem.segments[i]
            trackNodes[i].position = Position(x = 0.0f, y = 0.0f, z = seg.z)

            val segProps = propNodes[i]
            for (j in 0 until seg.decorations.size.coerceAtMost(segProps.size)) {
                val item = seg.decorations[j]
                val node = segProps[j]
                node.position = Position(x = item.x, y = item.y, z = seg.z + item.zOffset)
                if (item.rotationY != 0.0f) {
                    node.rotation = Rotation(y = item.rotationY)
                }
            }
        }
    }
}
