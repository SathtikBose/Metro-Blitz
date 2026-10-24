# Progress

## Status: Phase 0 — Foundation and walking skeleton — complete

## Checklist
- [x] Phase 0: Foundation and walking skeleton
- [ ] Phase 1: Hero character in Blender
- [ ] Phase 2: Environment assets in Blender
- [ ] Phase 3: Endless world + menu
- [ ] Phase 4: Controls, animations, pause (M1 / v0.1.0)
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
- Phase 0: 6/8 passed (2 pending device connection)
  - BLD-01: PASS (assembleDebug built app-debug.apk)
  - BLD-02: PASS (all unit tests passed: GameConfigTest, PureKotlinCoreTest)
  - BLD-03: PASS (lintDebug 0 errors)
  - FND-01: PASS (portrait locked in manifest)
  - FND-02: PASS (only VIBRATE permission in manifest, no INTERNET)
  - FND-04: PASS (tools/build_test.ps1 validates error code propagation)
  - BLD-04, FND-03: Pending physical device connection / running emulator
