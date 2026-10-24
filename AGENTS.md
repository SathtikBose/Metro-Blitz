# AGENTS.md — Metro Blitz (permanent rules, read at the start of EVERY task)

Project: **Metro Blitz** — 3D endless runner (Subway Surfers style), portrait, Android, Kotlin.
Package: `com.buildstack.metroblitz`. College project. Full specs live in `/docs`. If two files disagree, follow the "Priority of truth" list in `docs/00_README_START_HERE.md`; ask the human only if still unclear.

## 1. Golden rules
1. **Phase gate.** Work on ONE phase at a time (`docs/07_PHASE_PLAN.md`). When a phase passes its tests: commit, tag, update `docs/PROGRESS.md`, print the Phase Report, then STOP and wait for the human to type `next`. Never start the next phase on your own (unless the human set `MODE = AUTO` in the prompt).
2. **Test before you proceed.** Run the phase's test cases (`docs/08_TEST_PLAN.md`). Failing tests = fix, not skip. Never mark a test passed without actually running it.
3. **Always playable.** `main` must always build and launch. Never commit a broken build. If a change breaks the game, revert it.
4. **Commit after each stable build** using Conventional Commits (`docs/09_GIT_AND_QUALITY.md`): `type(scope): imperative summary`. No "update", "fix stuff", "wip".
5. **Do not invent APIs.** For SceneView / Filament / Compose / DataStore / Gradle versions and APIs, look them up with the Context7 MCP or the official README for the exact version in use BEFORE writing code. Isolate third-party 3D APIs behind the `render/` package so a wrong guess touches few files.
6. **Keep it simple.** Build exactly what the docs specify. No extra features, no new libraries, no refactors outside the phase. Anything not in the docs goes to `docs/PROGRESS.md` under "Ideas (not built)".
7. **Production quality.** No TODO left in committed code unless logged in PROGRESS.md. No hard-coded magic numbers: use `GameConfig`. No per-frame allocations in the game loop. No `!!` on nullable values without a reason.

## 2. Tech decisions (fixed)
- Kotlin, Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`), single module `app`.
- UI: Jetpack Compose (overlays only). 3D: SceneView (Filament) rendering `.glb` files. State: `ViewModel` + `StateFlow`. Persistence: DataStore Preferences. No DI framework, no ads, no analytics, no network permission.
- minSdk 26. compileSdk/targetSdk = latest stable installed. Portrait locked. Full-screen immersive. Keep screen on while playing.
- **Game logic is pure Kotlin** in `core/` (no Android, Compose or Filament imports) so it is unit-testable. `render/` only draws what `core/` says.
- World moves toward the player; the player stays near z = 0 (no floating-point drift). 1 unit = 1 metre. Lanes at x = -2, 0, +2.

## 3. Windows rules
- Shell is PowerShell. Use `.\gradlew.bat`, not `./gradlew`. Paths with backslashes in scripts, forward slashes in Gradle/Kotlin.
- `adb` must be on PATH. Check with `adb devices` before any device test.

## 4. Blender MCP rules (details in `docs/05_BLENDER_MCP_PLAYBOOK.md`)
- Assets are built by Python scripts saved in `blender/scripts/` and run through the Blender MCP. The scripts are the source of truth; `.blend` files in `blender/source/` are saved after every asset.
- Follow `docs/04_BLENDER_ASSET_SPEC.md`: flat-colour materials only (no image textures), triangle budgets, exact clip names, 1 unit = 1 m, export `.glb` to `app/src/main/assets/models/`.
- After every build step, take a viewport screenshot and look at it. Fix floating parts, wrong scale, missing faces before exporting.
- Never run destructive Blender operations (factory reset, delete all data) — the MCP addon lives inside Blender. Delete only objects you created.
- If the Blender MCP disconnects: tell the human to press "Start server" in Blender's sidebar, then continue.

## 5. Testing rules
- Unit tests: `.\gradlew.bat testDebugUnitTest`. Build: `.\gradlew.bat assembleDebug`. Lint: `.\gradlew.bat lintDebug`.
- Device checks: use `adb` scripts in `tools/` or the Mobile MCP (screenshot, swipe, tap). Save evidence screenshots to `docs/evidence/phase-N/`.
- Debug-only helpers (FPS overlay, invincibility, keyboard controls, fixed seed) live behind `BuildConfig.DEBUG`.

## 6. Phase Report format (print after every phase)
```
PHASE N COMPLETE — <name>
Built: <3-6 bullets>
Tests: <passed>/<total> (list any skipped + why)
Commit: <hash> <message>   Tag: <tag>
Known issues: <list or "none">
Next phase preview: <1-2 lines>
Type "next" to continue.
```

## 7. When stuck
Try twice with different approaches. Then log the problem in `docs/PROGRESS.md`, apply the fallback from `docs/11_RISKS_AND_FALLBACKS.md`, and tell the human exactly what you need. Never silently lower scope.
