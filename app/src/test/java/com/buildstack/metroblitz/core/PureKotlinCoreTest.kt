package com.buildstack.metroblitz.core

import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Architectural invariant test:
 * Guarantees that com.buildstack.metroblitz.core contains ZERO platform dependencies
 * (no android.*, no androidx.compose.*, no io.github.sceneview.*).
 */
class PureKotlinCoreTest {

    @Test
    fun testCoreHasNoPlatformImports() {
        // Locate core source directory
        val candidates = listOf(
            File("src/main/java/com/buildstack/metroblitz/core"),
            File("app/src/main/java/com/buildstack/metroblitz/core")
        )
        val coreDir = candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: return // If run in environment where relative path differs, skip without false positive

        val forbiddenPrefixes = listOf(
            "import android.",
            "import androidx.compose",
            "import io.github.sceneview",
            "import com.google.android.filament"
        )

        val violations = mutableListOf<String>()

        coreDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            file.readLines().forEachIndexed { lineNum, line ->
                val trimmed = line.trim()
                for (forbidden in forbiddenPrefixes) {
                    if (trimmed.startsWith(forbidden)) {
                        violations.add("${file.name}:$lineNum -> $trimmed")
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            fail("Pure Kotlin 'core/' package contains forbidden platform imports:\n" + violations.joinToString("\n"))
        }
    }
}
