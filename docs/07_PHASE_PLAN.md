# 07 — Phase Plan

**Protocol for every phase:** Plan (3 lines) → Build → Run tests from `08_TEST_PLAN.md` → Fix → Commit (+ tag) → Update `docs/PROGRESS.md` → Print Phase Report → **STOP and wait for `next`.**
Commit messages below are the suggested ones; split into more commits if the phase is big. All tests listed must pass before the tag.

| Phase | Name | Milestone | Tag |
|-------|------|-----------|-----|
| 0 | Foundation and walking skeleton | — | `phase-0` |
| 1 | Hero character in Blender | — | `phase-1` |
| 2 | Environment assets in Blender | — | `phase-2` |
| 3 | Endless world + menu | — | `phase-3` |
| 4 | Controls, animations, pause | **M1** | `phase-4`, `v0.1.0` |
| 5 | Obstacle assets + generator | — | `phase-5` |
| 6 | Collision, hit, game over (basic) | **M2** | `phase-6`, `v0.2.0` |
| 7 | Coins, score, persistence, game over (final) | **M3** | `phase-7`, `v0.3.0` |
| 8 | Polish, performance, release | Release | `phase-8`, `v1.0.0` |

---

## Phase 0 — Foundation and walking skeleton
**Goal:** the tools work and a blank 3D screen runs on a device.
**Human pre-step (before the prompt):** create the project in Android Studio (Empty Activity / Compose, name Metro Blitz, package `com.buildstack.metroblitz`, min SDK 26, Kotlin DSL) in the repo folder; copy `docs/` and `AGENTS.md` into it.
**Tasks**
1. Tool check: `java -version`, `adb devices`, Gradle wrapper works, Blender MCP scene info, Context7 and Mobile MCP respond. Report a table of what works.
2. `git init` (if needed), `.gitignore` (Android + Blender backups `*.blend1` + `release/` + `local.properties`), first commit.
3. Create `docs/PROGRESS.md` (phase checklist, decisions, known issues, ideas not built) and `CREDITS.md`.
4. Version catalog, dependencies (Compose, lifecycle, DataStore, SceneView, JUnit). Verify versions via Context7.
5. Manifest: portrait lock, `VIBRATE` only, no INTERNET; immersive full screen; keep screen on.
6. Create the package layout from `03` with empty/skeleton files and `GameConfig` from the GDD.
7. Walking skeleton: a SceneView screen showing a spinning coloured cube on the sky-blue background.
8. `tools/build_test.ps1` (build + unit tests + lint), `tools/run_device.ps1` (install + launch + logcat tail), `tools/screenshot.ps1`.
9. One real unit test (e.g., `GameConfig` lane positions) to prove the test pipeline.
**Tests:** BLD-01..05, FND-01..04.
**Commits:** `chore: initialize repository and project documentation` · `build: configure dependencies and version catalog` · `feat(app): add walking skeleton with 3D scene` · `chore(tools): add build, run and screenshot scripts`
**Tag:** `phase-0`

## Phase 1 — Hero character in Blender
**Goal:** Blitz exists, is rigged, animated and exported, and plays every clip in the app.
**Tasks**
1. `blender/scripts/mb_helpers.py` (from the playbook).
2. Build Blitz meshes with palette materials (`blender/scripts/build_blitz.py`). Screenshot review.
3. Rig (bones exactly per spec), rigid weights, test poses.
4. Animate clips: `Idle, Run, Jump, Slide, LeanLeft, LeanRight, Hit` (`animate_blitz.py`). Screenshot key frames.
5. Export `characters/blitz.glb`; write `tools/validate_glb.py`; run it; fix budget issues.
6. In-app debug "Model Viewer" screen (debug builds): shows Blitz, buttons to play each clip, rotates model. `AnimationPlayer` wrapper in `render/`.
7. Confirm the model loads, scale/orientation correct, all clips play, loops seamless.
**Tests:** AST-01..08 for the character, ANI-01..05, BLD-01..04.
**Commits:** `feat(blender): add scripts to build and rig the Blitz character` · `feat(blender): add Blitz animation clips` · `feat(assets): export Blitz character model` · `feat(render): add animation player and debug model viewer` · `test(assets): add GLB validation script`
**Tag:** `phase-1`

## Phase 2 — Environment assets in Blender
**Goal:** the world looks like a colorful metro street.
**Tasks:** build and export `track_segment`, `building_a–d`, `lamp_post`, `tree`, `fence` (see `04`). Seamless segment ends. Extend the debug viewer to show a row of three segments with buildings and props, with sky colour and a directional light. Take screenshots.
**Tests:** AST-01..08 for all, ENV-01..03, BLD-01..04.
**Commits:** `feat(blender): add track segment generator` · `feat(blender): add buildings and street props` · `feat(assets): export environment models` · `feat(render): add environment preview in debug viewer`
**Tag:** `phase-2`

## Phase 3 — Endless world + menu
**Goal:** open the app → Blitz idles on the track → tap → world scrolls forever at increasing speed with Blitz running (no controls yet).
**Tasks**
1. `core/`: `GameConfig`, `GameState` machine, `Difficulty`, `TrackSystem` (segment recycling), `Rng`, `GameWorld.update`.
2. `render/`: `AssetCatalog`, `ModelPool` (preload pools behind LOADING), `GameScene`, `PlayerNode`, `CameraRig`, `SceneStyle` (sky, light, optional fog).
3. Scenery decoration per segment (buildings, lamps, trees) with seeded variants, no overlap with track.
4. `GameViewModel` + `MenuOverlay` ("TAP TO PLAY"), tap → PLAYING, camera pull-back transition, `Run` animation speed scaled by game speed.
5. Debug overlay (FPS, speed, distance, active segments).
6. Unit tests for recycling, difficulty, state machine; 10-minute simulated headless run proving bounded positions.
**Tests:** COR-01..12, GAM-01..04, PRF-01, BLD-01..04.
**Commits:** `feat(core): add game config, difficulty curve and state machine` · `feat(core): add endless track segment recycling` · `feat(render): add model pools and game scene` · `feat(ui): add menu overlay and tap-to-play` · `test(core): add track and difficulty tests`
**Tag:** `phase-3`

## Phase 4 — Controls, animations, pause (Milestone M1 / v1)
**Goal:** fully controllable runner in an empty endless world.
**Tasks**
1. `SwipeDetector`, `DebugKeyInput`, `Command`s.
2. `PlayerController`: lane switching with buffer, jump (gravity from height/duration), slide, fast-fall, cancels, input buffer.
3. Animations: `Jump`, `Slide`, `LeanLeft/Right` (or tilt fallback), `Run` speed scale, back to `Run` after actions; blob shadow follows player.
4. HUD with pause button; `PauseOverlay` (Resume/Restart/Home + toggles stub); `CountdownOverlay` 3-2-1; auto-pause on `onPause`/focus loss.
5. Camera follow on X, FOV widening.
6. Tests for movement math and state transitions; device tests with adb swipes.
**Tests:** COR-13..30, INP-01..08, GAM-05..12, UI-01..04, LIF-01..04, PRF-02, BLD-01..04, REG-M1.
**Commits:** `feat(input): add swipe detector and debug keyboard input` · `feat(core): add player controller with lanes, jump and slide` · `feat(render): drive character animations from player state` · `feat(ui): add HUD, pause menu and resume countdown` · `feat(app): auto-pause when app loses focus` · `test(core): add player controller tests`
**Tag:** `phase-4` and `v0.1.0` (M1: "v1 playable runner")

## Phase 5 — Obstacle assets + generator
**Goal:** obstacles appear in the world in fair patterns (no collision yet).
**Tasks**
1. Blender: `barricade_low`, `overhead_beam`, `train_blue`, `train_orange` (scripts, screenshots, export, validate).
2. `PatternLibrary`, `SpawnDirector` (rows, gaps, tiers, seeded RNG, grace distance), obstacle pools in `render/`.
3. Fairness tests: 10 000 rows property test, determinism test, gap test.
4. Debug toggle to show obstacle hit boxes as wireframes (debug only).
**Tests:** AST-01..08 (obstacles), SPN-01..10, GAM-13..15, PRF-03, BLD-01..04.
**Commits:** `feat(blender): add obstacle models` · `feat(assets): export barricade, beam and train models` · `feat(core): add pattern library and fair spawn director` · `feat(render): add obstacle pools and spawning` · `test(core): add spawn fairness property tests`
**Tag:** `phase-5`

## Phase 6 — Collision, hit, basic game over (Milestone M2 / v2)
**Goal:** obstacles matter. Hit → animation → popup → try again.
**Tasks**
1. `CollisionSystem` AABB with exact boxes from GDD; barricade cleared by jump, beam by slide, train never; side collisions when changing lane into a train.
2. `HIT` state: slowdown, `Hit` clip, camera shake, optional star particles (simple cubes), then `GameOverDialog` (basic: title + Play Again + Home).
3. Full reset on Play Again without reloading assets (<1.5 s).
4. Debug invincibility toggle.
5. Tests of every collision case; automated bot run in headless simulation that dodges correctly and never collides when it plays optimally (validates fairness end-to-end).
**Tests:** COL-01..12, GAM-16..22, UI-05..06, REG-M2.
**Commits:** `feat(core): add collision system` · `feat(core): add hit state and game over flow` · `feat(ui): add basic game over dialog` · `feat(render): add hit animation and camera shake` · `test(core): add collision and optimal-bot tests`
**Tag:** `phase-6` and `v0.2.0`

## Phase 7 — Coins, score, persistence, final game over (Milestone M3 / v3)
**Goal:** complete core loop.
**Tasks**
1. Blender: `coin.glb`. Export and validate.
2. `CoinSystem` placement (line, arc over barricade, zig-zag), pooling, spin/bob animation, collection and pop effect.
3. `ScoreSystem` (formula in GDD), HUD live score and coins, 'NEW BEST!'.
4. `ScoreRepository` (DataStore): best score, total coins; show on menu.
5. Final `GameOverDialog` per UI spec; menu shows best and total coins.
6. Tests: score math, coin collection, no coin/obstacle overlap, persistence with a fake repository + one instrumented persistence test.
**Tests:** CON-01..10, SCR-01..08, UI-07..10, GAM-23..28, REG-M3.
**Commits:** `feat(blender): add coin model` · `feat(core): add coin patterns and collection` · `feat(core): add scoring system` · `feat(data): persist best score and total coins` · `feat(ui): add live score, coins and final game over dialog` · `test(core): add coin and score tests`
**Tag:** `phase-7` and `v0.3.0`

## Phase 8 — Polish, performance, release (v1.0.0)
**Tasks**
1. Audio: SFX + music via `SoundManager`, toggles wired to settings; CC0 files only, listed in `CREDITS.md`. Vibration on hit.
2. Visual polish: fog, sky gradient, speed lines (optional), landing dust, coin sparkle, button animations.
3. Icon (rendered in Blender through MCP) + adaptive icon + splash.
4. Performance pass: measure with `dumpsys gfxinfo`, Android Studio profiler if available; fix top issues; add dynamic resolution if needed. Allocation check in hot path.
5. Release build: R8 + shrink; run full regression on the **release** APK.
6. Final soak test 10 minutes with an adb swipe script.
7. README.md with screenshots, features, build/run steps, architecture summary; final docs sync.
8. Produce `release/MetroBlitz-1.0.0-debug-signed.apk` (git-ignored) and attach to GitHub release if a remote exists.
**Tests:** full `08_TEST_PLAN.md` regression, PRF-01..08, LIF-01..08, REL-01..10.
**Commits:** `feat(audio): add sound effects, music and settings toggles` · `feat(app): add haptic feedback on hit` · `style(ui): add polish effects and fog` · `feat(app): add launcher icon and splash screen` · `perf: reduce allocations and tune render settings` · `build: enable R8 minification for release` · `docs: add README with screenshots and build guide` · `chore(release): prepare v1.0.0`
**Tag:** `phase-8` and `v1.0.0`

---
## Progress log (`docs/PROGRESS.md`) template
```
# Progress
## Status: Phase X — <name> — <in progress|complete>
## Checklist
- [ ] Phase 0 ... [ ] Phase 8
## Decisions
- <date> <decision + reason>
## Library versions in use
## Known issues
## Ideas (not built)
## Test results (latest)
```
