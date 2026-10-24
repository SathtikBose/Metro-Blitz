package com.buildstack.metroblitz.render

import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode

/**
 * Wraps Blitz character node and animation playback.
 */
class PlayerNode(
    val modelNode: ModelNode
) {
    val animationPlayer = AnimationPlayer(modelNode)

    fun play(clipName: String, loop: Boolean = true, speed: Float = 1.0f) {
        animationPlayer.play(clipName, loop = loop, speed = speed)
    }

    companion object {
        fun create(modelLoader: ModelLoader): PlayerNode {
            val instance = modelLoader.createModelInstance(AssetCatalog.MODEL_BLITZ)
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.7f
            ).apply {
                try {
                    playingAnimations = java.util.concurrent.ConcurrentHashMap()
                } catch (_: Throwable) {}
            }
            val playerNode = PlayerNode(node)
            playerNode.play(AssetCatalog.CLIP_IDLE, loop = true)
            return playerNode
        }
    }
}
