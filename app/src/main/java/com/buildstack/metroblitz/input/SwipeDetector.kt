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
                val absDx = abs(dx)
                val absDy = abs(dy)
                val elapsed = System.currentTimeMillis() - downTime

                if (elapsed <= maxDurationMs && (absDx >= thresholdPx || absDy >= thresholdPx)) {
                    triggered = true
                    if (absDx > absDy) {
                        if (dx > 0) onCommand(Command.RIGHT) else onCommand(Command.LEFT)
                    } else {
                        if (dy > 0) onCommand(Command.SLIDE) else onCommand(Command.JUMP) // Swipe down = slide, up = jump
                    }
                    return true
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
}
