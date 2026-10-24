package com.buildstack.metroblitz.render

/**
 * Wraps glTF skeletal animation playback for SceneView nodes.
 */
class AnimationPlayer {
    var currentClip: String? = null
        private set

    fun play(clipName: String, loop: Boolean = true, speed: Float = 1.0f) {
        currentClip = clipName
    }

    fun isFinished(): Boolean = false
}
