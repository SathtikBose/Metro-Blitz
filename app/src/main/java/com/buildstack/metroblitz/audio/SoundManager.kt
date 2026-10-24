package com.buildstack.metroblitz.audio

import android.content.Context

/**
 * Audio manager handling sound effects (SoundPool) and music (MediaPlayer).
 * Implemented in Phase 8.
 */
class SoundManager(private val context: Context) {
    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true

    fun playJump() {}
    fun playSlide() {}
    fun playCoin() {}
    fun playHit() {}
    fun playTap() {}
}
