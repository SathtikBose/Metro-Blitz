# 03 — Technical Architecture

## 1. Stack
| Layer | Choice | Why |
|-------|--------|-----|
| Language | Kotlin | Required |
| UI overlays | Jetpack Compose | Easy menus, HUD, dialogs |
| 3D rendering | **SceneView** (Google Filament, Compose-native, loads `.glb` with skeletal animations) | Smallest path from Blender GLB to Android screen |
| Game logic | Pure Kotlin (`core/`) | Unit-testable, engine independent |
| State | `ViewModel` + `StateFlow` | Survives rotation-free lifecycle, simple |
| Storage | DataStore Preferences | best score, coins, toggles |
| Audio | `SoundPool` (SFX) + `MediaPlayer` (music) | Built in |
| Build | Gradle Kotlin DSL, version catalog | Standard |
| Tests | JUnit4 (+ kotlin test), adb scripts, Mobile MCP | |

**Rule:** look up the latest stable SceneView, Compose BOM, DataStore, AGP and Kotlin versions with Context7 / official docs. Do not copy versions from memory. SceneView's API has changed between major versions, so confirm node/animation APIs for the exact version.

**Engine decision record (ADR-001):** SceneView chosen over libGDX because the game needs only ~30 simultaneous objects and Compose UI overlays are trivial. If SceneView cannot play named glTF animations or hits a blocking bug, the fallback is libGDX + a glTF loader (see `11_RISKS_AND_FALLBACKS.md`). Because `core/` has no engine imports, only `render/` would be rewritten.

## 2. Package layout (`app/src/main/java/com/buildstack/metroblitz/`)
```
MainActivity.kt              single activity, immersive, portrait, hosts GameScreen
GameApp.kt                   Application class (creates repositories)
core/                        PURE KOTLIN — no android.*, no Compose, no Filament
  GameConfig.kt              every tunable number (see GDD)
  GameState.kt               enum + sealed UI snapshots
  GameWorld.kt               owns simulation: update(dt) advances everything
  PlayerController.kt        lane, jump, slide state machine, input buffer
  Difficulty.kt              speed(t), tier(distance)
  TrackSystem.kt             segment pool positions + recycling (data only)
  SpawnDirector.kt           rows/patterns, fairness rules, seeded RNG
  PatternLibrary.kt          row definitions per tier
  CollisionSystem.kt         AABB tests
  CoinSystem.kt              coin placement + collection
  ScoreSystem.kt             score, best
  Rng.kt                     seeded deterministic RNG wrapper
render/                      SceneView adapter — the ONLY place that knows Filament/SceneView
  GameScene.kt               composable hosting the 3D view and per-frame callback
  AssetCatalog.kt            file names + animation clip names (one place)
  ModelPool.kt               preloaded node pools (segments, obstacles, coins, props)
  PlayerNode.kt              wraps character model + AnimationPlayer
  AnimationPlayer.kt         play(name, loop, speed), isFinished()
  CameraRig.kt               follow, shake, FOV
  SceneStyle.kt              sky colour, fog, lights
input/
  SwipeDetector.kt           touch → Command(LEFT, RIGHT, JUMP, SLIDE, TAP)
  DebugKeyInput.kt           arrows/space/S for emulator (debug only)
ui/
  GameViewModel.kt           exposes StateFlow<UiState>, receives Commands, owns the loop clock
  MenuOverlay.kt  HudOverlay.kt  PauseOverlay.kt  CountdownOverlay.kt  GameOverDialog.kt
  theme/ (Color.kt, Type.kt, Theme.kt)
data/
  SettingsRepository.kt      sound, vibration
  ScoreRepository.kt         bestScore, totalCoins
audio/
  SoundManager.kt
util/
  Haptics.kt  Perf.kt (debug FPS)  Log.kt
```
Tests: `app/src/test/java/com/buildstack/metroblitz/core/...` mirror `core/`.
Other folders at repo root: `blender/scripts`, `blender/source`, `tools`, `docs`, `release` (git-ignored).

## 3. Game loop
```
Choreographer/SceneView onFrame(nanos)
  dt = clamp((now - last) / 1e9, 0.0, 0.05)       // never simulate > 50 ms
  if state == PLAYING:  world.update(dt)
  elif state == HIT:    world.updateHit(dt)
  elif state in (MENU, PAUSED, RESUME_COUNTDOWN): world.idle(dt)   // animations only where needed
  renderer.sync(world.snapshot())                  // move pooled nodes; no allocation
  publish UiState only when a visible value changed (score integer, coins, state)
```
- Single thread (main). No coroutines in the hot path. Countdown uses the same clock (dt) so it freezes with the app.
- **World snapshot** = flat arrays/preallocated lists of (type, lane, z, y) so rendering never allocates.

## 4. Object pools
Preload at startup behind the LOADING state (a progress-free fast load; target < 1.5 s):
- 7 track segments (each with its own decorations pool), 8 buildings, 6 lamp posts, 6 trees
- obstacles: 12 barricades, 8 beams, 8 trains (4 per colour)
- 40 coins · 1 player · 1 blob shadow
Nodes are shown/hidden and repositioned. **Never load a model during gameplay.**

## 5. State machine (owned by `GameViewModel`)
```
LOADING --assets ready--> MENU
MENU --tap--> PLAYING
PLAYING --pause button / onPause--> PAUSED
PAUSED --resume--> RESUME_COUNTDOWN (3→2→1) --done--> PLAYING
PAUSED --restart--> PLAYING (fresh run) ; PAUSED --home--> MENU
PLAYING --collision--> HIT --0.8 s--> GAME_OVER
GAME_OVER --play again--> PLAYING ; GAME_OVER --home--> MENU
```
Illegal transitions are ignored and logged. State machine lives in `core` and is unit-tested.

## 6. Input
`SwipeDetector` consumes `MotionEvent` on the 3D view, emits `Command` into the ViewModel. Commands are applied at the start of the next `update`. Debug builds add keyboard (←→↑↓, Space) so the emulator and `adb shell input keyevent` can test without touch.

## 7. Performance rules
1. No allocation in `update`/`sync` (preallocated arrays, no lambdas created per frame, no string building).
2. Render: dynamic resolution allowed, minimum scale 0.7; no real-time shadows, no SSAO, no bloom; FXAA or MSAA off on low-end. Cap at 60 fps.
3. All models share a handful of flat-colour materials; triangle budget per `04`.
4. Keep `.glb` files small, no textures. No Draco/meshopt.
5. Pause rendering updates when the activity is not resumed; release audio on stop.

## 8. Persistence keys (DataStore)
`best_score:Int`, `total_coins:Int`, `sound_on:Boolean(true)`, `vibration_on:Boolean(true)`, `music_on:Boolean(true)`.

## 9. Error handling
- Model load failure → show friendly error screen "Could not load game assets" with Retry, log the file name.
- Device without GLES 3.0 → message and exit gracefully.
- Never crash on bad DataStore data: fall back to defaults.

## 10. Build variants
`debug`: FPS overlay, invincibility toggle, fixed seed option, keyboard input, verbose log.
`release`: R8 minify + shrink resources, no debug helpers. Both use applicationId `com.buildstack.metroblitz`.

## 11. Dependencies allowed (nothing else)
Compose BOM + Material3, Activity-Compose, Lifecycle (ViewModel/Runtime Compose), DataStore Preferences, SceneView, JUnit.
