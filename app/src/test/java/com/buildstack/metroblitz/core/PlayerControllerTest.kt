package com.buildstack.metroblitz.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests validating player controls, jump physics, slide, fast-fall,
 * pause, countdown, and deterministic replay.
 * Validates COR-13..30.
 */
class PlayerControllerTest {

    @Test
    fun testLaneChangeAndBoundaryClamping() {
        val player = PlayerController()
        assertEquals(1, player.currentLane)

        // Move to lane 0
        player.onLeft()
        player.update(GameConfig.LANE_CHANGE_DURATION_SECONDS)
        assertEquals(0, player.currentLane)
        assertEquals(GameConfig.laneX(0), player.x, 0.001f)

        // COR-13: Swipe left in lane 0 stays lane 0
        player.onLeft()
        player.update(GameConfig.LANE_CHANGE_DURATION_SECONDS)
        assertEquals(0, player.currentLane)
        assertEquals(GameConfig.laneX(0), player.x, 0.001f)

        // Move to lane 1
        player.onRight()
        player.update(GameConfig.LANE_CHANGE_DURATION_SECONDS)
        assertEquals(1, player.currentLane)

        // Move to lane 2
        player.onRight()
        player.update(GameConfig.LANE_CHANGE_DURATION_SECONDS)
        assertEquals(2, player.currentLane)
        assertEquals(GameConfig.laneX(2), player.x, 0.001f)

        // COR-14: Swipe right in lane 2 stays lane 2
        player.onRight()
        player.update(GameConfig.LANE_CHANGE_DURATION_SECONDS)
        assertEquals(2, player.currentLane)
        assertEquals(GameConfig.laneX(2), player.x, 0.001f)
    }

    @Test
    fun testLaneChangeDurationAndQueuing() {
        // COR-15: Lane change finishes in 0.12 s
        val player = PlayerController()
        player.onLeft()
        assertTrue(player.isChangingLane)

        // Halfway at 0.06s
        player.update(0.06f)
        assertTrue(player.isChangingLane)
        assertTrue(player.x < 0.0f && player.x > -2.0f)

        // COR-16: A second lane command during a change is queued (max 1 queued)
        player.onRight() // queued command to return to lane 1

        // Finish first change
        player.update(0.06f)
        assertEquals(0, player.currentLane)

        // Queued command immediately starts executing
        assertTrue(player.isChangingLane)
        player.update(0.12f)
        assertEquals(1, player.currentLane)
        assertFalse(player.isChangingLane)
    }

    @Test
    fun testJumpApexAndDuration() {
        val player = PlayerController()
        player.onJump()
        assertEquals(PlayerAction.JUMPING, player.actionState)

        var maxY = 0.0f
        var time = 0.0f
        val dt = 0.01f

        while (player.actionState == PlayerAction.JUMPING) {
            player.update(dt)
            time += dt
            if (player.y > maxY) maxY = player.y
        }

        // COR-17: Jump apex = 1.7 ± 0.02
        assertEquals(1.7f, maxY, 0.02f)

        // COR-18: Jump lasts 0.62 s ± one frame
        assertEquals(0.62f, time, 0.03f)
    }

    @Test
    fun testJumpBufferingNearLanding() {
        val player = PlayerController()
        player.onJump()

        // Advance to apex (t = 0.31s)
        player.update(0.31f)
        // Mid-air jump too early (remaining 0.31s > 0.15s) -> ignored
        player.onJump()

        // Advance to t = 0.50s (remaining 0.12s <= 0.15s buffer window)
        player.update(0.19f)
        // COR-19: Jump commanded <= 0.15 s before landing executes on landing
        player.onJump()

        // Land the first jump
        player.update(0.12f)

        // Immediately enters second jump upon landing!
        assertEquals(PlayerAction.JUMPING, player.actionState)
        assertTrue(player.y >= 0.0f)
    }

    @Test
    fun testSlideDurationAndHitbox() {
        val player = PlayerController()
        player.onSlide()

        // COR-20: Slide lasts 0.65 s, hit box height 0.8
        assertEquals(PlayerAction.SLIDING, player.actionState)
        assertEquals(0.8f, player.hitBoxHeight, 0.001f)

        player.update(0.64f)
        assertEquals(PlayerAction.SLIDING, player.actionState)
        assertEquals(0.8f, player.hitBoxHeight, 0.001f)

        player.update(0.02f)
        assertEquals(PlayerAction.RUNNING, player.actionState)
        assertEquals(1.7f, player.hitBoxHeight, 0.001f)
    }

    @Test
    fun testJumpCancelsSlide() {
        // COR-21: Jump command cancels slide
        val player = PlayerController()
        player.onSlide()
        assertEquals(PlayerAction.SLIDING, player.actionState)

        player.update(0.2f)
        player.onJump()
        assertEquals(PlayerAction.JUMPING, player.actionState)
    }

    @Test
    fun testSlideInAirFastFallsThenSlidesOnLanding() {
        // COR-22: Slide command in air triggers fast-fall then slide on landing
        val player = PlayerController()
        player.onJump()
        player.update(0.31f) // at apex

        val apexY = player.y
        assertTrue(apexY > 1.5f)

        player.onSlide()
        // Fast-falls at -14 m/s -> should reach ground in ~0.15s instead of normal ~0.31s
        player.update(0.15f)
        assertEquals(0.0f, player.y, 0.001f)
        assertEquals(PlayerAction.SLIDING, player.actionState)
        assertEquals(0.8f, player.hitBoxHeight, 0.001f)
    }

    @Test
    fun testLaneChangeDuringJumpKeepsArcUnchanged() {
        // COR-23: Lane change during jump keeps arc unchanged
        val jumpSolo = PlayerController()
        val jumpWithLaneChange = PlayerController()

        jumpSolo.onJump()
        jumpWithLaneChange.onJump()

        // Update partially
        jumpSolo.update(0.1f)
        jumpWithLaneChange.update(0.1f)
        assertEquals(jumpSolo.y, jumpWithLaneChange.y, 0.001f)

        // Lane change during jump
        jumpWithLaneChange.onLeft()
        for (i in 0 until 20) {
            jumpSolo.update(0.02f)
            jumpWithLaneChange.update(0.02f)
            assertEquals(jumpSolo.y, jumpWithLaneChange.y, 0.001f)
        }
    }

    @Test
    fun testPauseFreezesWorldAndCountdownResumes() {
        val world = GameWorld()
        world.startPlaying()

        world.update(2.0f)
        val distBeforePause = world.distanceMetres
        val speedBeforePause = world.speed

        // COR-24: Pause freezes player, distance, speed
        world.pause()
        assertEquals(GameState.PAUSED, world.state)

        world.update(2.0f)
        assertEquals(distBeforePause, world.distanceMetres, 0.001f)
        assertEquals(speedBeforePause, world.speed, 0.001f)

        // COR-25 & 26: Resume countdown lasts 3.0 s and world is frozen during countdown
        world.resumeCountdown()
        assertEquals(GameState.RESUME_COUNTDOWN, world.state)
        assertEquals(3.0f, world.countdownRemaining, 0.05f)

        // Advance 1.0 second in 20 steps of 0.05s (respecting dt <= 0.05)
        for (i in 0 until 20) {
            world.update(0.05f)
        }
        assertEquals(distBeforePause, world.distanceMetres, 0.001f) // frozen!
        assertEquals(2.0f, world.countdownRemaining, 0.05f)

        // COR-27: Pause during countdown returns to PAUSED
        world.pause()
        assertEquals(GameState.PAUSED, world.state)

        // Resume again and complete countdown (3.0 seconds)
        world.resumeCountdown()
        for (i in 0 until 65) {
            world.update(0.05f)
        }
        assertEquals(GameState.PLAYING, world.state)
    }

    @Test
    fun testRestartResetsAll() {
        // COR-28: Restart resets score, coins, speed, distance, lane, obstacles
        val world = GameWorld()
        world.startPlaying()
        world.player.onLeft()
        world.player.update(0.15f)
        for (i in 0 until 200) {
            world.update(0.05f)
        }

        assertTrue(world.distanceMetres > 50.0f)
        world.coinsCollected = 15

        world.reset()
        assertEquals(0, world.score)
        assertEquals(0, world.coinsCollected)
        assertEquals(GameConfig.START_SPEED, world.speed, 0.001f)
        assertEquals(0.0f, world.distanceMetres, 0.001f)
        assertEquals(1, world.player.currentLane)
        assertEquals(GameState.PLAYING, world.state)
    }

    @Test
    fun testDeterministicReplay() {
        // COR-30: Deterministic replay: same seed + same timed commands -> identical final state
        val world1 = GameWorld(seed = 42L)
        val world2 = GameWorld(seed = 42L)

        world1.startPlaying()
        world2.startPlaying()

        val steps = 300 // 5 seconds at dt = 0.0166s
        val dt = 0.0166f

        for (step in 0 until steps) {
            if (step == 30) {
                world1.onLeft()
                world2.onLeft()
            }
            if (step == 80) {
                world1.onJump()
                world2.onJump()
            }
            if (step == 150) {
                world1.onSlide()
                world2.onSlide()
            }
            if (step == 220) {
                world1.onRight()
                world2.onRight()
            }

            world1.update(dt)
            world2.update(dt)
        }

        val s1 = world1.snapshot()
        val s2 = world2.snapshot()

        assertEquals(s1.distanceMetres, s2.distanceMetres, 0.001f)
        assertEquals(s1.speed, s2.speed, 0.001f)
        assertEquals(s1.playerX, s2.playerX, 0.001f)
        assertEquals(s1.playerY, s2.playerY, 0.001f)
        assertEquals(s1.playerLane, s2.playerLane)
        assertEquals(s1.playerAction, s2.playerAction)
    }
}
