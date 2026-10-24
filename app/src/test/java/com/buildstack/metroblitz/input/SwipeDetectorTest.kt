package com.buildstack.metroblitz.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for swipe detection math and command resolution.
 * Validates:
 * - INP-01..04: Swipe LEFT / RIGHT / UP / DOWN produce the right command
 * - INP-05: Movement under 48 dp ignored
 * - INP-06: Diagonal swipe resolves to dominant axis
 */
class SwipeDetectorTest {

    @Test
    fun testCardinalDirections() {
        val threshold = 48f

        // INP-01: Left
        assertEquals(Command.LEFT, SwipeDetector.resolveSwipe(-60f, 0f, threshold))

        // INP-02: Right
        assertEquals(Command.RIGHT, SwipeDetector.resolveSwipe(60f, 0f, threshold))

        // INP-03: Up (jump, negative Y in screen coordinates)
        assertEquals(Command.JUMP, SwipeDetector.resolveSwipe(0f, -60f, threshold))

        // INP-04: Down (slide, positive Y in screen coordinates)
        assertEquals(Command.SLIDE, SwipeDetector.resolveSwipe(0f, 60f, threshold))
    }

    @Test
    fun testMovementUnderThresholdIgnored() {
        // INP-05: Movement under 48 dp ignored
        val threshold = 48f
        assertNull(SwipeDetector.resolveSwipe(0f, 0f, threshold))
        assertNull(SwipeDetector.resolveSwipe(20f, 15f, threshold))
        assertNull(SwipeDetector.resolveSwipe(-47.9f, 0f, threshold))
        assertNull(SwipeDetector.resolveSwipe(0f, 47.9f, threshold))
    }

    @Test
    fun testDiagonalSwipeResolvesToDominantAxis() {
        // INP-06: Diagonal swipe resolves to dominant axis
        val threshold = 48f

        // dx = 80, dy = 50 -> Horizontal wins -> RIGHT
        assertEquals(Command.RIGHT, SwipeDetector.resolveSwipe(80f, 50f, threshold))

        // dx = 50, dy = 80 -> Vertical wins -> SLIDE
        assertEquals(Command.SLIDE, SwipeDetector.resolveSwipe(50f, 80f, threshold))

        // dx = -90, dy = -40 -> Horizontal wins -> LEFT
        assertEquals(Command.LEFT, SwipeDetector.resolveSwipe(-90f, -40f, threshold))

        // dx = -30, dy = -100 -> Vertical wins -> JUMP
        assertEquals(Command.JUMP, SwipeDetector.resolveSwipe(-30f, -100f, threshold))
    }
}
