package com.buildstack.metroblitz.render

import com.buildstack.metroblitz.core.Coin
import com.buildstack.metroblitz.core.GameConfig
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

/**
 * Pre-allocated object pool for 3D coin models.
 * Zero per-frame allocations during gameplay (CON-10).
 */
class CoinPool(
    private val modelLoader: ModelLoader
) {
    companion object {
        private const val PARKED_Y = -100.0f
    }

    val coinNodes: Array<ModelNode> = Array(GameConfig.COIN_POOL_SIZE) {
        val inst = modelLoader.createModelInstance(AssetCatalog.MODEL_COIN)
        ModelNode(modelInstance = inst).apply {
            position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            isVisible = false
        }
    }

    /**
     * Attaches all pooled coin nodes to the root 3D scene node.
     */
    fun attachTo(parent: Node) {
        for (node in coinNodes) {
            parent.addChildNode(node)
        }
    }

    /**
     * Synchronizes active coins with pooled 3D model nodes.
     * Applies rotation (180 deg/s spin), bob offset, and pop scale-down on collection.
     */
    fun update(
        coins: List<Coin>,
        spinAngleDegrees: Float,
        bobOffsetY: Float
    ) {
        val activeCount = coins.size.coerceAtMost(coinNodes.size)

        for (i in 0 until activeCount) {
            val coin = coins[i]
            val node = coinNodes[i]

            val scale = if (coin.isCollected) {
                val progress = (coin.popTimer / GameConfig.COIN_POP_DURATION_SECONDS).coerceIn(0.0f, 1.0f)
                (1.0f - progress).coerceAtLeast(0.0f)
            } else {
                1.0f
            }

            if (scale <= 0.001f) {
                node.isVisible = false
                node.position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            } else {
                node.isVisible = true
                node.position = Position(
                    x = coin.x,
                    y = coin.y + bobOffsetY,
                    z = coin.z
                )
                node.rotation = Rotation(x = 0.0f, y = spinAngleDegrees, z = 0.0f)
                node.scale = Scale(scale, scale, scale)
            }
        }

        // Park remaining unused nodes
        for (i in activeCount until coinNodes.size) {
            val node = coinNodes[i]
            if (node.isVisible) {
                node.isVisible = false
                node.position = Position(x = 0.0f, y = PARKED_Y, z = 0.0f)
            }
        }
    }
}
