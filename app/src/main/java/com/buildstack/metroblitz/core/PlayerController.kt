package com.buildstack.metroblitz.core

/**
 * High-level action states for the player.
 * Pure Kotlin — zero platform imports.
 */
enum class PlayerAction {
    RUNNING,
    JUMPING,
    SLIDING
}

/**
 * Player lane switching, jump physics, slide, fast-fall, and input buffering.
 * Pure Kotlin — zero platform imports.
 */
class PlayerController {
    var currentLane: Int = 1
        private set

    var targetLane: Int = 1
        private set

    var x: Float = 0.0f
        private set
    var y: Float = 0.0f
        private set
    var z: Float = 0.0f
        private set

    var actionState: PlayerAction = PlayerAction.RUNNING
        private set

    // Lane change parameters
    private var laneChangeTimer: Float = 0.0f
    private var startLaneX: Float = 0.0f
    private var targetLaneX: Float = 0.0f
    private var queuedLaneDirection: Int? = null

    // Jump physics parameters
    private var jumpTimer: Float = 0.0f
    private var isFastFalling: Boolean = false
    private var hasBufferedJump: Boolean = false

    // Slide parameters
    private var slideTimer: Float = 0.0f

    val isChangingLane: Boolean
        get() = laneChangeTimer > 0.0f

    val laneChangeDirection: Int
        get() = if (isChangingLane) (targetLane - currentLane).coerceIn(-1, 1) else 0

    val hitBoxHeight: Float
        get() = if (actionState == PlayerAction.SLIDING) GameConfig.PLAYER_HEIGHT_SLIDING else GameConfig.PLAYER_HEIGHT_STANDING

    fun onLeft() {
        if (!isChangingLane) {
            if (currentLane > 0) {
                startLaneChange(currentLane - 1)
            }
        } else {
            // Queue at most one subsequent lane command
            if (queuedLaneDirection == null && (targetLane > 0 || currentLane > 0)) {
                queuedLaneDirection = -1
            }
        }
    }

    fun onRight() {
        if (!isChangingLane) {
            if (currentLane < GameConfig.LANE_COUNT - 1) {
                startLaneChange(currentLane + 1)
            }
        } else {
            // Queue at most one subsequent lane command
            if (queuedLaneDirection == null && (targetLane < GameConfig.LANE_COUNT - 1 || currentLane < GameConfig.LANE_COUNT - 1)) {
                queuedLaneDirection = 1
            }
        }
    }

    private fun startLaneChange(nextLane: Int) {
        startLaneX = x
        targetLane = nextLane.coerceIn(0, GameConfig.LANE_COUNT - 1)
        targetLaneX = GameConfig.laneX(targetLane)
        laneChangeTimer = GameConfig.LANE_CHANGE_DURATION_SECONDS
    }

    fun onJump() {
        when (actionState) {
            PlayerAction.SLIDING -> {
                // COR-21: Jump command cancels slide
                slideTimer = 0.0f
                startJump()
            }
            PlayerAction.RUNNING -> {
                startJump()
            }
            PlayerAction.JUMPING -> {
                // COR-19: Jump while airborne ignored; a jump commanded <= 0.15 s before landing executes on landing
                val remainingTime = GameConfig.JUMP_DURATION_SECONDS - jumpTimer
                if (remainingTime in 0.0f..GameConfig.INPUT_BUFFER_SECONDS) {
                    hasBufferedJump = true
                }
            }
        }
    }

    private fun startJump() {
        actionState = PlayerAction.JUMPING
        jumpTimer = 0.0f
        isFastFalling = false
        hasBufferedJump = false
    }

    fun onSlide() {
        when (actionState) {
            PlayerAction.RUNNING -> {
                actionState = PlayerAction.SLIDING
                slideTimer = GameConfig.SLIDE_DURATION_SECONDS
            }
            PlayerAction.SLIDING -> {
                slideTimer = GameConfig.SLIDE_DURATION_SECONDS
            }
            PlayerAction.JUMPING -> {
                // COR-22: Slide command in air triggers fast-fall then slide on landing
                isFastFalling = true
            }
        }
    }

    fun update(dt: Float) {
        // 1. Update lane change
        if (laneChangeTimer > 0.0f) {
            laneChangeTimer -= dt
            if (laneChangeTimer <= 0.0f) {
                laneChangeTimer = 0.0f
                currentLane = targetLane
                x = targetLaneX

                // Execute queued lane command if present (COR-16)
                val queued = queuedLaneDirection
                queuedLaneDirection = null
                if (queued != null) {
                    val nextLane = currentLane + queued
                    if (nextLane in 0 until GameConfig.LANE_COUNT) {
                        startLaneChange(nextLane)
                    }
                }
            } else {
                val progress = 1.0f - (laneChangeTimer / GameConfig.LANE_CHANGE_DURATION_SECONDS)
                x = startLaneX + (targetLaneX - startLaneX) * progress.coerceIn(0.0f, 1.0f)
            }
        }

        // 2. Update vertical / jump physics
        if (actionState == PlayerAction.JUMPING) {
            if (isFastFalling) {
                y += GameConfig.FAST_FALL_VELOCITY * dt
                if (y <= 0.0f) {
                    y = 0.0f
                    isFastFalling = false
                    // Enter slide on landing (COR-22)
                    actionState = PlayerAction.SLIDING
                    slideTimer = GameConfig.SLIDE_DURATION_SECONDS
                }
            } else {
                jumpTimer += dt
                if (jumpTimer >= GameConfig.JUMP_DURATION_SECONDS) {
                    jumpTimer = GameConfig.JUMP_DURATION_SECONDS
                    y = 0.0f
                    actionState = PlayerAction.RUNNING

                    if (hasBufferedJump) {
                        hasBufferedJump = false
                        startJump()
                    }
                } else {
                    val t = jumpTimer
                    y = (GameConfig.JUMP_INITIAL_VELOCITY * t - 0.5f * GameConfig.GRAVITY * t * t)
                        .coerceAtLeast(0.0f)
                }
            }
        }

        // 3. Update slide
        if (actionState == PlayerAction.SLIDING) {
            slideTimer -= dt
            if (slideTimer <= 0.0f) {
                slideTimer = 0.0f
                actionState = PlayerAction.RUNNING
            }
        }
    }

    fun reset() {
        currentLane = 1
        targetLane = 1
        x = GameConfig.laneX(1)
        y = 0.0f
        z = 0.0f
        actionState = PlayerAction.RUNNING
        laneChangeTimer = 0.0f
        queuedLaneDirection = null
        jumpTimer = 0.0f
        isFastFalling = false
        hasBufferedJump = false
        slideTimer = 0.0f
    }
}
