package com.buildstack.metroblitz.core

/**
 * State machine enforcing legal gameplay state transitions.
 * Illegal transitions are safely ignored without throwing exceptions.
 * Pure Kotlin — zero platform imports.
 */
class GameStateMachine(initialState: GameState = GameState.MENU) {
    var currentState: GameState = initialState
        private set

    /**
     * Attempts to transition to the target state.
     * Returns true if legal and transitioned; false if ignored.
     */
    fun transitionTo(target: GameState): Boolean {
        if (canTransitionTo(target)) {
            currentState = target
            return true
        }
        return false
    }

    fun canTransitionTo(target: GameState): Boolean {
        return when (currentState) {
            GameState.LOADING -> target == GameState.MENU
            GameState.MENU -> target == GameState.PLAYING
            GameState.PLAYING -> target == GameState.PAUSED || target == GameState.HIT
            GameState.PAUSED -> target == GameState.RESUME_COUNTDOWN || target == GameState.PLAYING || target == GameState.MENU
            GameState.RESUME_COUNTDOWN -> target == GameState.PLAYING || target == GameState.PAUSED
            GameState.HIT -> target == GameState.GAME_OVER
            GameState.GAME_OVER -> target == GameState.PLAYING || target == GameState.MENU
        }
    }

    fun reset(state: GameState = GameState.MENU) {
        currentState = state
    }
}
