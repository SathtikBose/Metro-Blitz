package com.buildstack.metroblitz.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.buildstack.metroblitz.core.ScoreSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.scoreDataStore by preferencesDataStore(name = "score_prefs")

/**
 * Interface contract for score and lifetime coin persistence.
 */
interface ScoreRepository {
    val bestScore: Flow<Int>
    val totalCoins: Flow<Int>
    suspend fun getBestScore(): Int
    suspend fun getTotalCoins(): Int
    suspend fun saveRunResult(score: Int, coinsThisRun: Int)
    suspend fun resetAll()
}

/**
 * DataStore Preferences implementation of [ScoreRepository].
 * Handles IO exceptions gracefully by falling back to empty preferences (defaults to 0).
 */
open class DataStoreScoreRepository(private val context: Context) : ScoreRepository {

    companion object {
        val KEY_BEST_SCORE = intPreferencesKey("best_score")
        val KEY_TOTAL_COINS = intPreferencesKey("total_coins")
    }

    private val dataStore = context.scoreDataStore

    override val bestScore: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> prefs[KEY_BEST_SCORE] ?: 0 }

    override val totalCoins: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> prefs[KEY_TOTAL_COINS] ?: 0 }

    override suspend fun getBestScore(): Int = bestScore.first()

    override suspend fun getTotalCoins(): Int = totalCoins.first()

    override suspend fun saveRunResult(score: Int, coinsThisRun: Int) {
        dataStore.edit { prefs ->
            val currentBest = prefs[KEY_BEST_SCORE] ?: 0
            prefs[KEY_BEST_SCORE] = ScoreSystem.updateBestScore(score, currentBest)

            val currentCoins = prefs[KEY_TOTAL_COINS] ?: 0
            prefs[KEY_TOTAL_COINS] = currentCoins + coinsThisRun.coerceAtLeast(0)
        }
    }

    override suspend fun resetAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

/**
 * Default ScoreRepository alias for standard instantiation.
 */
class ScoreRepository(context: Context) : DataStoreScoreRepository(context)

/**
 * In-memory test double of [ScoreRepository] for deterministic unit tests (SCR-05..08).
 */
class FakeScoreRepository(
    initialBestScore: Int = 0,
    initialTotalCoins: Int = 0
) : ScoreRepository {

    private val _bestScore = MutableStateFlow(initialBestScore)
    override val bestScore: Flow<Int> = _bestScore.asStateFlow()

    private val _totalCoins = MutableStateFlow(initialTotalCoins)
    override val totalCoins: Flow<Int> = _totalCoins.asStateFlow()

    override suspend fun getBestScore(): Int = _bestScore.value

    override suspend fun getTotalCoins(): Int = _totalCoins.value

    override suspend fun saveRunResult(score: Int, coinsThisRun: Int) {
        _bestScore.value = ScoreSystem.updateBestScore(score, _bestScore.value)
        _totalCoins.value += coinsThisRun.coerceAtLeast(0)
    }

    override suspend fun resetAll() {
        _bestScore.value = 0
        _totalCoins.value = 0
    }
}
