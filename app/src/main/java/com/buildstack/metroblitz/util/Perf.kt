package com.buildstack.metroblitz.util

/**
 * Debug FPS and frame metrics calculator.
 */
class FpsTracker {
    private var lastTimeNanos: Long = 0L
    private var frameCount: Int = 0
    var currentFps: Int = 0
        private set

    fun onFrame(frameTimeNanos: Long) {
        if (lastTimeNanos == 0L) {
            lastTimeNanos = frameTimeNanos
            return
        }

        frameCount++
        val elapsedNanos = frameTimeNanos - lastTimeNanos
        if (elapsedNanos >= 1_000_000_000L) { // 1 second
            currentFps = frameCount
            frameCount = 0
            lastTimeNanos = frameTimeNanos
        }
    }
}
