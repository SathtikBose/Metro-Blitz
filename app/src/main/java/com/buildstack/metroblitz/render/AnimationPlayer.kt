package com.buildstack.metroblitz.render

import io.github.sceneview.node.ModelNode

/**
 * Wraps glTF skeletal animation playback for SceneView nodes.
 */
class AnimationPlayer(
    private var modelNode: ModelNode? = null
) {
    var currentClip: String? = null
        private set

    fun attach(node: ModelNode) {
        this.modelNode = node
    }

    fun play(clipName: String, loop: Boolean = true, speed: Float = 1.0f) {
        currentClip = clipName
        modelNode?.playAnimation(clipName, speed = speed, loop = loop)
    }

    fun isFinished(): Boolean = false
}
