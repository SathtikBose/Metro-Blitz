# Progress

## Status: Phase 0 — Foundation and walking skeleton — in progress

## Checklist
- [ ] Phase 0: Foundation and walking skeleton
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
- 2026-10-24 ADR-001: SceneView (Google Filament) selected for 3D GLB rendering. Logic strictly isolated in pure Kotlin `core/`.

## Library versions in use
- AGP: 9.0.1
- Kotlin: 2.2.0 / 2.3.20
- Compose BOM: 2026.03.01
- SceneView: 2.2.1
- DataStore Preferences: 1.1.3
- JUnit: 4.13.2

## Known issues
- No hardware-accelerated device/emulator currently active for graphics rendering (BLD-04, FND-03 require device connection).

## Ideas (not built)
- (None yet)

## Test results (latest)
- Initial compilation and unit test pipeline: 24/24 Gradle tasks executed, BUILD SUCCESSFUL.
