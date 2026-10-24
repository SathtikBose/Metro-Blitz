package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GameStateMachine.
 * Validates:
 * - COR-09: State machine allows only legal transitions
 * - COR-10: Illegal transitions ignored, no exception
 */
class GameStateMachineTest {

    @Test
    fun testLegalTransitions() {
        // COR-09: State machine allows only legal transitions
        val sm = GameStateMachine(GameState.LOADING)
        
        // LOADING -> MENU
        assertTrue(sm.transitionTo(GameState.MENU))
        assertEquals(GameState.MENU, sm.currentState)

        // MENU -> PLAYING
        assertTrue(sm.transitionTo(GameState.PLAYING))
        assertEquals(GameState.PLAYING, sm.currentState)

        // PLAYING -> PAUSED
        assertTrue(sm.transitionTo(GameState.PAUSED))
        assertEquals(GameState.PAUSED, sm.currentState)

        // PAUSED -> RESUME_COUNTDOWN
        assertTrue(sm.transitionTo(GameState.RESUME_COUNTDOWN))
        assertEquals(GameState.RESUME_COUNTDOWN, sm.currentState)

        // RESUME_COUNTDOWN -> PLAYING
        assertTrue(sm.transitionTo(GameState.PLAYING))
        assertEquals(GameState.PLAYING, sm.currentState)

        // PLAYING -> HIT
        assertTrue(sm.transitionTo(GameState.HIT))
        assertEquals(GameState.HIT, sm.currentState)

        // HIT -> GAME_OVER
        assertTrue(sm.transitionTo(GameState.GAME_OVER))
        assertEquals(GameState.GAME_OVER, sm.currentState)

        // GAME_OVER -> PLAYING (play again)
        assertTrue(sm.transitionTo(GameState.PLAYING))
        assertEquals(GameState.PLAYING, sm.currentState)

        // PLAYING -> PAUSED -> MENU (quit to menu)
        assertTrue(sm.transitionTo(GameState.PAUSED))
        assertTrue(sm.transitionTo(GameState.MENU))
        assertEquals(GameState.MENU, sm.currentState)
    }

    @Test
    fun testIllegalTransitionsIgnoredWithoutException() {
        // COR-10: Illegal transitions ignored, no exception
        val sm = GameStateMachine(GameState.MENU)

        // In MENU, cannot jump directly to PAUSED, HIT, GAME_OVER, etc.
        assertFalse(sm.transitionTo(GameState.PAUSED))
        assertEquals(GameState.MENU, sm.currentState)

        assertFalse(sm.transitionTo(GameState.GAME_OVER))
        assertEquals(GameState.MENU, sm.currentState)

        assertFalse(sm.transitionTo(GameState.HIT))
        assertEquals(GameState.MENU, sm.currentState)

        // Enter PLAYING
        sm.transitionTo(GameState.PLAYING)
        assertEquals(GameState.PLAYING, sm.currentState)

        // From PLAYING cannot jump directly to MENU, LOADING, GAME_OVER
        assertFalse(sm.transitionTo(GameState.MENU))
        assertEquals(GameState.PLAYING, sm.currentState)

        assertFalse(sm.transitionTo(GameState.GAME_OVER))
        assertEquals(GameState.PLAYING, sm.currentState)

        assertFalse(sm.transitionTo(GameState.LOADING))
        assertEquals(GameState.PLAYING, sm.currentState)
    }

    @Test
    fun testCountdownInterruption() {
        val sm = GameStateMachine(GameState.PAUSED)
        assertTrue(sm.transitionTo(GameState.RESUME_COUNTDOWN))
        // Interrupted back to paused
        assertTrue(sm.transitionTo(GameState.PAUSED))
        assertEquals(GameState.PAUSED, sm.currentState)
    }
}
