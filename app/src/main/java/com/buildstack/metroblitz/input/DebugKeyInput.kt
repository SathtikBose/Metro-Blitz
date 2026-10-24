package com.buildstack.metroblitz.input

import android.view.KeyEvent

/**
 * Maps hardware keyboard keys to commands for emulator/adb testing.
 */
object DebugKeyInput {
    fun onKeyDown(keyCode: Int): Command? {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> Command.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> Command.RIGHT
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_SPACE -> Command.JUMP
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> Command.SLIDE
            KeyEvent.KEYCODE_ENTER -> Command.TAP
            else -> null
        }
    }
}
