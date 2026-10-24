package com.buildstack.metroblitz.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Handles device haptic vibration feedback on player collision (Phase 8).
 * Uses VIBRATE permission only (REL-08).
 */
class HapticHelper(context: Context) {
    companion object {
        private const val TAG = "HapticHelper"
        private const val HIT_VIBRATION_DURATION_MS = 120L
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var vibrationEnabled: Boolean = true

    /**
     * Triggers a crisp haptic pulse when Blitz hits an obstacle.
     */
    fun vibrateHit() {
        if (!vibrationEnabled) return
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(
                        HIT_VIBRATION_DURATION_MS,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(HIT_VIBRATION_DURATION_MS)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error triggering haptic vibration: ${e.message}")
        }
    }
}
