package com.buildstack.metroblitz.util

import android.util.Log

/**
 * Global game logging utility for Metro Blitz.
 */
object MbLog {
    private const val TAG = "MetroBlitz"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
