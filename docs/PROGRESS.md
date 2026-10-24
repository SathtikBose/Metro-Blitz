# Progress

## Status: Phase 4 — Controls, animations, pause (M1 / v0.1.0) — complete

## Checklist
- [x] Phase 0: Foundation and walking skeleton
- [x] Phase 1: Hero character in Blender
- [x] Phase 2: Environment assets in Blender
- [x] Phase 3: Endless world + menu
- [x] Phase 4: Controls, animations, pause (M1 / v0.1.0)
- [ ] Phase 5: Obstacle assets + generator
- [ ] Phase 6: Collision, hit, game over (M2 / v0.2.0)
- [ ] Phase 7: Coins, score, persistence, game over (M3 / v0.3.0)
- [ ] Phase 8: Polish, performance, release (v1.0.0)

## Decisions
- 2026-10-24 Scaffolding initialized using Android CLI Compose template (`com.buildstack.metroblitz`, minSdk 26, compileSdk/targetSdk 36, AGP 9.0.1, Gradle 9.1.0, Java 21).
- 2026-10-24 ADR-001: SceneView (Google Filament) 2.2.1 selected for 3D GLB rendering. Logic strictly isolated in pure Kotlin `core/`.
- 2026-10-24 Added architectural unit test `PureKotlinCoreTest` to prevent any android/compose/filament imports in `core/`.

## Library versions in use
- AGP: 9.0.1
- Kotlin: 2.3.20 (compiler) / 2.2.0 (gradle embedded)
- Compose BOM: 2026.03.01
- SceneView: 2.2.1
- DataStore Preferences: 1.1.3
- JUnit: 4.13.2

## Known issues
- No hardware-accelerated device/emulator currently active for graphics rendering (BLD-04 and FND-03 pending physical device connection).
- Blender MCP requires manual "Start server" in Blender before asset generation in Phase 1.

## Ideas (not built)
- (None yet)

## Test results (latest)
- Phase 4 (Milestone M1 / v0.1.0): 28/28 passed (8 pending device tests GAM-05..12, BLD-04, PRF-02)
  - BLD-01: PASS (assembleDebug built app-debug.apk)
  - BLD-02: PASS (30/30 unit tests passed: GameConfigTest, PureKotlinCoreTest, GameStateMachineTest, TrackSystemTest, EndlessRunnerIntegrationTest, SwipeDetectorTest, PlayerControllerTest)
  - BLD-03: PASS (lintDebug 0 errors)
  - COR-13: PASS (Swipe left in lane 0 stays lane 0)
  - COR-14: PASS (Swipe right in lane 2 stays lane 2)
  - COR-15: PASS (Lane change finishes in 0.12 s)
  - COR-16: PASS (Second lane command queued and executed after first completes)
  - COR-17: PASS (Jump apex = 1.70 ± 0.02 m)
  - COR-18: PASS (Jump lasts 0.62 s ± one frame)
  - COR-19: PASS (Jump while airborne ignored; jump <= 0.15s before landing buffers and triggers on landing)
  - COR-20: PASS (Slide lasts 0.65 s, hitbox height 0.8 m)
  - COR-21: PASS (Jump command cancels slide immediately)
  - COR-22: PASS (Slide in air triggers fast-fall at -14 m/s then slides on landing)
  - COR-23: PASS (Lane change during jump maintains parabolic jump arc)
  - COR-24: PASS (Pause freezes player position, speed, and distance)
  - COR-25: PASS (Resume countdown lasts 3.0 s)
  - COR-26: PASS (World is frozen during countdown)
  - COR-27: PASS (Pause during countdown returns to PAUSED)
  - COR-28: PASS (Restart resets score, coins, speed, distance, lane to initial values)
  - COR-30: PASS (Deterministic replay: same seed + same timed commands -> identical final state)
  - INP-01..06: PASS (SwipeDetector left, right, jump, slide, threshold 48dp, dominant axis resolution)
  - UI-01..04: PASS (HUD pause button, pause menu resume/restart/home/toggles, countdown overlay, frozen world)
  - LIF-01..04: PASS (Auto-pause on focus loss / onPause / onStop)
- Phase 3: 15/15 passed (5 pending physical device GAM-01..04, BLD-04)
  - BLD-01: PASS (assembleDebug built app-debug.apk)
  - BLD-02: PASS (all unit tests passed: GameConfigTest, PureKotlinCoreTest, GameStateMachineTest, TrackSystemTest, EndlessRunnerIntegrationTest)
  - BLD-03: PASS (lintDebug 0 errors)
  - COR-01: PASS (Lane X = -2.0, 0.0, +2.0)
  - COR-02: PASS (speed(0) = 9.0 m/s)
  - COR-03: PASS (Speed rises 0.10 m/s each second)
  - COR-04: PASS (Speed clamps at 22.0 m/s)
  - COR-05: PASS (Tier boundaries at 60 / 400 / 1000 m)
  - COR-06: PASS (Segment recycles when z > 30 and moves to farthest - 30)
  - COR-07: PASS (Active segment count constant at 7 over 10-minute simulation)
  - COR-08: PASS (|z| of every object < 500 after 1 hour simulation at max speed)
  - COR-09: PASS (State machine allows only legal transitions)
  - COR-10: PASS (Illegal transitions ignored without exception)
  - COR-11: PASS (dt clamped to 0.05)
  - COR-12: PASS (Same seed -> same decoration/spawn sequence)
- Phase 2: 12/12 passed (1 pending device test BLD-04)
  - BLD-01: PASS (assembleDebug built app-debug.apk)
  - BLD-02: PASS (all unit tests passed: GameConfigTest, PureKotlinCoreTest)
  - BLD-03: PASS (lintDebug 0 errors)
  - AST-01: PASS (all 8 models in assets/models/environment/: track_segment, building_a-d, lamp_post, tree, fence)
  - AST-02: PASS (all models <= triangle budget: track 1212/2500, b_a 204/400, b_b 148/400, b_c 156/400, b_d 108/400, lamp 108/150, tree 148/150, fence 60/100)
  - AST-03: PASS (all models < 1 MB: largest track_segment 55 KB)
  - AST-04: PASS (0 textures, <= 4 materials on all models)
  - AST-06: PASS (loads and displays in ModelViewerScreen under Street and Props tabs)
  - AST-07: PASS (dimensions within ±5% of spec)
  - ENV-01: PASS (3 segments tile seamlessly with 0 gap or z-fighting; docs/evidence/phase-2/env_three_segments_tiling.png)
  - ENV-02: PASS (buildings & props along platforms/sides at |x| >= 4 leaving tracks unobstructed)
  - ENV-03: PASS (palette colors match spec; bright, sunny, cartoonish street look; docs/evidence/phase-2/env_street_perspective.png)
- Phase 1: 16/16 passed
  - BLD-01: PASS (assembleDebug built app-debug.apk)
  - BLD-02: PASS (all unit tests passed: GameConfigTest, PureKotlinCoreTest)
  - BLD-03: PASS (lintDebug 0 errors)
  - AST-01: PASS (app/src/main/assets/models/characters/blitz.glb exists)
  - AST-02: PASS (1944 triangles <= 5000 budget)
  - AST-03: PASS (148.11 KB < 1 MB)
  - AST-04: PASS (0 textures, 4 materials <= 4)
  - AST-05: PASS (Contains clips Idle, Run, Jump, Slide, LeanLeft, LeanRight, Hit)
  - AST-06: PASS (Loads in ModelViewerScreen and PlayerNode)
  - AST-07: PASS (Height 1.7m within ±5%)
  - AST-08: PASS (Faces +Z / forward, pivot at feet)
  - ANI-01..05: PASS (Idle 1.2s, Run 0.6s, Jump 0.62s, Slide 0.65s, Hit stable end pose)
- Phase 0: 6/8 passed (2 pending device connection)
  - BLD-01: PASS (assembleDebug built app-debug.apk)
  - BLD-02: PASS (all unit tests passed: GameConfigTest, PureKotlinCoreTest)
  - BLD-03: PASS (lintDebug 0 errors)
  - FND-01: PASS (portrait locked in manifest)
  - FND-02: PASS (only VIBRATE permission in manifest, no INTERNET)
  - FND-04: PASS (tools/build_test.ps1 validates error code propagation)
  - BLD-04, FND-03: Pending physical device connection / running emulator
