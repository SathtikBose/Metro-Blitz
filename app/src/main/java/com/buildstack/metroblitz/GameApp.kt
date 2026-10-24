package com.buildstack.metroblitz

import android.app.Application
import com.buildstack.metroblitz.data.DataStoreScoreRepository
import com.buildstack.metroblitz.data.ScoreRepository
import com.buildstack.metroblitz.data.SettingsRepository

/**
 * Application class for Metro Blitz.
 * Initializes persistent repositories and global app scope dependencies.
 */
class GameApp : Application() {
    companion object {
        lateinit var instance: GameApp
            private set
    }

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var scoreRepository: ScoreRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = SettingsRepository(this)
        scoreRepository = DataStoreScoreRepository(this)
    }
}
