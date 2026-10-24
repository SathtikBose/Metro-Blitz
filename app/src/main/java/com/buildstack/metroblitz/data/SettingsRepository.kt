package com.buildstack.metroblitz.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

/**
 * Manages player settings (sound, vibration, music) backed by DataStore Preferences.
 */
class SettingsRepository(private val context: Context) {
    companion object {
        val KEY_SOUND_ON = booleanPreferencesKey("sound_on")
        val KEY_VIBRATION_ON = booleanPreferencesKey("vibration_on")
        val KEY_MUSIC_ON = booleanPreferencesKey("music_on")
    }

    val soundOn: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_SOUND_ON] ?: true
    }

    val vibrationOn: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_VIBRATION_ON] ?: true
    }

    val musicOn: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_MUSIC_ON] ?: true
    }

    suspend fun setSoundOn(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_SOUND_ON] = enabled }
    }

    suspend fun setVibrationOn(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_VIBRATION_ON] = enabled }
    }

    suspend fun setMusicOn(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_MUSIC_ON] = enabled }
    }
}
