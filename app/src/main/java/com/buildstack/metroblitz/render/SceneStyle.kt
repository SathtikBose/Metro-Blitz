package com.buildstack.metroblitz.render

import androidx.compose.ui.graphics.Color
import com.buildstack.metroblitz.core.GameConfig

/**
 * Lighting, atmospheric sky gradient, and environmental styling (Phase 8).
 */
object SceneStyle {
    val skyColorHex: Long = GameConfig.COLOR_SKY_BLUE

    val skyGradient = listOf(
        Color(0xFF0288D1), // Deep vibrant azure top
        Color(GameConfig.COLOR_SKY_BLUE), // Bright Sky Blue
        Color(0xFFE1F5FE)  // Atmospheric sunny horizon haze
    )
}
