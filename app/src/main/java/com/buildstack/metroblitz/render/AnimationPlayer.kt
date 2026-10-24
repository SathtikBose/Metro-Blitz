package com.buildstack.metroblitz.render

import io.github.sceneview.node.ModelNode
import java.util.concurrent.ConcurrentHashMap

/**
 * Wraps glTF skeletal animation playback for SceneView nodes.
 * Uses ConcurrentHashMap for playingAnimations and safe looping to eliminate SceneView ConcurrentModificationExceptions.
 */
class AnimationPlayer(
    private var modelNode: ModelNode? = null
) {
    var currentClip: String? = null
        private set

    init {
        modelNode?.let { node ->
            try {
                node.playingAnimations = ConcurrentHashMap()
            } catch (_: Throwable) {}
        }
    }

    fun attach(node: ModelNode) {
        this.modelNode = node
        try {
            node.playingAnimations = ConcurrentHashMap()
        } catch (_: Throwable) {}
    }

    fun play(clipName: String, loop: Boolean = true, speed: Float = 1.0f) {
        if (currentClip == clipName && (clipName == AssetCatalog.CLIP_HIT || clipName == AssetCatalog.CLIP_IDLE)) {
            return
        }
        currentClip = clipName
        try {
            modelNode?.let { node ->
                node.playingAnimations.clear()
                node.playAnimation(clipName, speed = speed, loop = true)
            }
        } catch (e: Exception) {
            android.util.Log.w("AnimationPlayer", "Error playing animation $clipName: ${e.message}")
        }
    }

    fun isFinished(): Boolean = false
}
