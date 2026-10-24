package com.buildstack.metroblitz.input

import android.view.MotionEvent
import kotlin.math.abs

/**
 * Recognizes directional swipes immediately when threshold is reached.
 */
class SwipeDetector(
    private val thresholdPx: Float = 48f,
    private val maxDurationMs: Long = 400L,
    private val onCommand: (Command) -> Unit
) {
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var triggered = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = System.currentTimeMillis()
                triggered = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (triggered) return true

                val dx = event.x - downX
                val dy = event.y - downY
                val elapsed = System.currentTimeMillis() - downTime

                if (elapsed <= maxDurationMs) {
                    val cmd = resolveSwipe(dx, dy, thresholdPx)
                    if (cmd != null) {
                        triggered = true
                        onCommand(cmd)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!triggered) {
                    val elapsed = System.currentTimeMillis() - downTime
                    val dx = abs(event.x - downX)
                    val dy = abs(event.y - downY)
                    if (elapsed < 250 && dx < thresholdPx / 2f && dy < thresholdPx / 2f) {
                        onCommand(Command.TAP)
                    }
                }
                triggered = false
            }

            MotionEvent.ACTION_CANCEL -> {
                triggered = false
            }
        }
        return false
    }

    companion object {
        /**
         * Pure function resolving a dx, dy motion to a directional Command.
         * Validates threshold, dominant axis, and direction.
         */
        fun resolveSwipe(dx: Float, dy: Float, thresholdPx: Float = 48f): Command? {
            val absDx = abs(dx)
            val absDy = abs(dy)
            if (absDx < thresholdPx && absDy < thresholdPx) return null
            return if (absDx > absDy) {
                if (dx > 0) Command.RIGHT else Command.LEFT
            } else {
                if (dy > 0) Command.SLIDE else Command.JUMP
            }
        }
    }
}
