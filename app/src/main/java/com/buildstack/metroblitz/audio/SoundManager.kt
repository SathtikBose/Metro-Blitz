package com.buildstack.metroblitz.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.buildstack.metroblitz.R

/**
 * Audio manager handling sound effects (SoundPool) and background music (MediaPlayer).
 * Implements low-latency playback, master toggles, and lifecycle management (LIF-08).
 */
class SoundManager(private val context: Context) {
    companion object {
        private const val TAG = "SoundManager"
    }

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                resumeMusic()
            } else {
                pauseMusic()
            }
        }

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(audioAttributes)
        .build()

    private val soundIds = mutableMapOf<Int, Int>()
    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingMusic = false

    init {
        loadSounds()
        initMusic()
    }

    private fun loadSounds() {
        try {
            soundIds[R.raw.sfx_jump] = soundPool.load(context, R.raw.sfx_jump, 1)
            soundIds[R.raw.sfx_slide] = soundPool.load(context, R.raw.sfx_slide, 1)
            soundIds[R.raw.sfx_coin] = soundPool.load(context, R.raw.sfx_coin, 1)
            soundIds[R.raw.sfx_hit] = soundPool.load(context, R.raw.sfx_hit, 1)
            soundIds[R.raw.sfx_tap] = soundPool.load(context, R.raw.sfx_tap, 1)
            soundIds[R.raw.sfx_countdown] = soundPool.load(context, R.raw.sfx_countdown, 1)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load sound effects: ${e.message}")
        }
    }

    private fun initMusic() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.bgm_main)?.apply {
                isLooping = true
                setVolume(0.7f, 0.7f)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize background music: ${e.message}")
        }
    }

    private fun playSfx(resId: Int, volume: Float = 1.0f) {
        if (!soundEnabled) return
        val soundId = soundIds[resId] ?: return
        try {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.w(TAG, "Error playing sound $resId: ${e.message}")
        }
    }

    fun playJump() = playSfx(R.raw.sfx_jump, 0.85f)
    fun playSlide() = playSfx(R.raw.sfx_slide, 0.85f)
    fun playCoin() = playSfx(R.raw.sfx_coin, 0.95f)
    fun playHit() = playSfx(R.raw.sfx_hit, 1.0f)
    fun playTap() = playSfx(R.raw.sfx_tap, 0.7f)
    fun playCountdown() = playSfx(R.raw.sfx_countdown, 0.85f)

    fun startMusic() {
        if (!musicEnabled) return
        try {
            if (mediaPlayer == null) {
                initMusic()
            }
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlayingMusic = true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error starting music: ${e.message}")
        }
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
            isPlayingMusic = false
        } catch (e: Exception) {
            Log.w(TAG, "Error pausing music: ${e.message}")
        }
    }

    fun resumeMusic() {
        if (musicEnabled && !isPlayingMusic) {
            startMusic()
        }
    }

    fun onPause() {
        pauseMusic()
        soundPool.autoPause()
    }

    fun onResume() {
        soundPool.autoResume()
        if (musicEnabled) {
            resumeMusic()
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            soundPool.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio resources: ${e.message}")
        }
    }
}
