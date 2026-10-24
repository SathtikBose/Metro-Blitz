package com.buildstack.metroblitz.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.scoreDataStore by preferencesDataStore(name = "score_prefs")

/**
 * Manages best score and lifetime coin count backed by DataStore Preferences.
 */
class ScoreRepository(private val context: Context) {
    companion object {
        val KEY_BEST_SCORE = intPreferencesKey("best_score")
        val KEY_TOTAL_COINS = intPreferencesKey("total_coins")
    }

    val bestScore: Flow<Int> = context.scoreDataStore.data.map { prefs ->
        prefs[KEY_BEST_SCORE] ?: 0
    }

    val totalCoins: Flow<Int> = context.scoreDataStore.data.map { prefs ->
        prefs[KEY_TOTAL_COINS] ?: 0
    }

    suspend fun updateBestScore(newScore: Int) {
        context.scoreDataStore.edit { prefs ->
            val current = prefs[KEY_BEST_SCORE] ?: 0
            if (newScore > current) {
                prefs[KEY_BEST_SCORE] = newScore
            }
        }
    }

    suspend fun addCoins(count: Int) {
        context.scoreDataStore.edit { prefs ->
            val current = prefs[KEY_TOTAL_COINS] ?: 0
            prefs[KEY_TOTAL_COINS] = current + count
        }
    }
}
