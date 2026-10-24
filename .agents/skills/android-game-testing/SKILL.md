---
name: android-game-testing
description: Use when building, installing, launching, testing or measuring the Metro Blitz Android app (unit tests, adb input, screenshots, performance, soak tests, Mobile MCP).
---

# Android game testing (Metro Blitz)

Read first: `docs/08_TEST_PLAN.md`.

## Loop
1. Windows PowerShell: `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` (or `tools\build_test.ps1`).
2. `adb devices` must list a device. Install: `adb install -r app\build\outputs\apk\debug\app-debug.apk`; launch with `adb shell am start -n com.buildstack.metroblitz/.MainActivity`.
3. Check crashes: `adb logcat -d -s AndroidRuntime:E MetroBlitz:V`. Any `FATAL EXCEPTION` = failed test.
4. Drive the game with the Mobile MCP (tap, swipe, screenshot) or `adb shell input swipe/tap` (commands in the test plan). Look at every screenshot before declaring a test passed.
5. Save evidence to `docs/evidence/phase-N/`. Record results as `<passed>/<total>` in `docs/PROGRESS.md`.
6. Performance: `adb shell dumpsys gfxinfo com.buildstack.metroblitz` and `dumpsys meminfo`. Compare with PRF targets.

## Rules
Do not mark a test passed without running it. A failing logic bug gets a unit test first, then the fix. Prefer a real phone over the emulator for performance numbers.
