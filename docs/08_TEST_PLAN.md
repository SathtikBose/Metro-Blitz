# 08 — Test Plan

**Rule:** a phase is done only when every test assigned to it passes (or is explicitly logged as "N/A + reason" in `docs/PROGRESS.md`). Type: **U** = JUnit unit test (automatic), **D** = device/emulator test (adb or Mobile MCP, evidence screenshot), **S** = static/script check.

## 0. Commands
```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug      # BLD-01..03  (tools\build_test.ps1)
adb devices                                                  # device must be listed as "device"
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.buildstack.metroblitz/.MainActivity
adb logcat -d -s AndroidRuntime:E MetroBlitz:V               # crashes + game log
adb exec-out screencap -p > docs\evidence\phase-N\name.png   # screenshot
adb shell dumpsys gfxinfo com.buildstack.metroblitz          # frame stats (reset with: dumpsys gfxinfo <pkg> reset)
adb shell dumpsys meminfo com.buildstack.metroblitz          # memory
adb shell am force-stop com.buildstack.metroblitz            # kill
```
Input (coordinates for a 1080×2400 screen; scale for your device):
```powershell
adb shell input tap 540 1200                          # tap
adb shell input swipe 300 1200 800 1200 80            # swipe RIGHT
adb shell input swipe 800 1200 300 1200 80            # swipe LEFT
adb shell input swipe 540 1500 540 900 80             # swipe UP  (jump)
adb shell input swipe 540 900 540 1500 80             # swipe DOWN (slide)
adb shell input keyevent KEYCODE_HOME                 # background the app
```
Soak script `tools/soak.ps1`: launches the app, taps to play, then every 0.7–1.5 s sends a random swipe for 10 minutes while sampling `meminfo` each minute; saves a CSV and the last `logcat`. Debug builds start with invincibility ON for the soak so it measures stability, not skill.

## 1. Build and foundation
| ID | Type | Check | Phase |
|----|------|-------|-------|
| BLD-01 | S | `assembleDebug` succeeds | all |
| BLD-02 | U | all unit tests pass | all |
| BLD-03 | S | `lintDebug` has 0 errors | all |
| BLD-04 | D | install + launch, no `FATAL EXCEPTION` in 10 s of logcat | all |
| BLD-05 | S | `assembleRelease` succeeds | 8 (and spot checks) |
| FND-01 | S | Manifest locks portrait | 0 |
| FND-02 | S | No `INTERNET` permission; only `VIBRATE` allowed | 0 |
| FND-03 | D | Skeleton cube visible (screenshot not blank) | 0 |
| FND-04 | S | `tools\build_test.ps1` exits non-zero when a test is made to fail | 0 |

## 2. Assets (run per `.glb`)
| ID | Type | Check |
|----|------|-------|
| AST-01 | S | File exists in `assets/models/...` with the exact name |
| AST-02 | S | Triangles ≤ budget in `04` |
| AST-03 | S | File size < 1 MB |
| AST-04 | S | 0 textures, ≤ 4 materials |
| AST-05 | S | Character contains clips `Idle, Run, Jump, Slide, LeanLeft, LeanRight, Hit` |
| AST-06 | D | Loads in the debug viewer without crash |
| AST-07 | S/D | Dimensions within ±5% of spec |
| AST-08 | D | Faces the right direction, pivot correct (screenshot with a ground grid) |
| ANI-01 | D | Each clip plays from the viewer buttons |
| ANI-02 | D | `Idle` and `Run` loop with no visible pop |
| ANI-03 | S | `Jump` duration 0.62 ± 0.05 s |
| ANI-04 | S | `Slide` duration 0.65 ± 0.05 s |
| ANI-05 | D | `Hit` ends on a stable pose, mesh not distorted |
| ENV-01 | D | Three segments side by side tile with no gap or z-fight |
| ENV-02 | D | Buildings/props never intrude on |x| < 4 |
| ENV-03 | D | Colours match palette; scene looks bright and friendly |

## 3. Core logic (JUnit)
| ID | Check |
|----|-------|
| COR-01 | Lane X = −2, 0, +2 |
| COR-02 | `speed(0) = 9.0` |
| COR-03 | Speed rises 0.10 m/s each second |
| COR-04 | Speed clamps at 22.0 |
| COR-05 | Tier boundaries at 60 / 400 / 1000 m |
| COR-06 | Segment recycles when z > 30 and goes to farthest − 30 |
| COR-07 | Active segment count constant over a 10-minute simulation |
| COR-08 | |z| of every object < 500 after a simulated 1 hour at max speed |
| COR-09 | State machine allows only legal transitions |
| COR-10 | Illegal transitions ignored, no exception |
| COR-11 | dt clamped to 0.05 |
| COR-12 | Same seed → same decoration/spawn sequence |
| COR-13 | Swipe left in lane 0 stays lane 0 |
| COR-14 | Swipe right in lane 2 stays lane 2 |
| COR-15 | Lane change finishes in 0.12 s |
| COR-16 | A second lane command during a change is queued and executed after (max 1 queued) |
| COR-17 | Jump apex = 1.7 ± 0.02 |
| COR-18 | Jump lasts 0.62 s ± one frame |
| COR-19 | Jump while airborne ignored; a jump commanded ≤ 0.15 s before landing executes on landing |
| COR-20 | Slide lasts 0.65 s, hit box height 0.8 |
| COR-21 | Jump command cancels slide |
| COR-22 | Slide command in air triggers fast-fall then slide on landing |
| COR-23 | Lane change during jump keeps arc unchanged |
| COR-24 | Pause freezes player, distance, speed |
| COR-25 | Resume countdown lasts 3.0 s ± 0.05 |
| COR-26 | World is frozen during countdown |
| COR-27 | Pause during countdown returns to PAUSED |
| COR-28 | Restart resets score, coins, speed, distance, lane, obstacles |
| COR-29 | Background event in PLAYING/COUNTDOWN → PAUSED |
| COR-30 | Deterministic replay: same seed + same timed commands → identical final state |

## 4. Input
| ID | Type | Check |
|----|------|-------|
| INP-01..04 | U | Swipe LEFT / RIGHT / UP / DOWN produce the right command |
| INP-05 | U | Movement under 48 dp ignored |
| INP-06 | U | Diagonal swipe resolves to dominant axis |
| INP-07 | U | One continuous swipe emits exactly one command |
| INP-08 | D | Tap in MENU starts; tap while PLAYING does nothing |

## 5. Gameplay on device (D)
| ID | Check | Phase |
|----|-------|-------|
| GAM-01 | Opens to Menu with idle Blitz in < 3 s | 3 |
| GAM-02 | Tap starts movement in < 150 ms, no countdown | 3 |
| GAM-03 | 2 minutes of running: no visible pop-in, no gaps, no flicker | 3 |
| GAM-04 | Speed visibly rises (debug overlay readings at 0, 30, 60 s match formula) | 3 |
| GAM-05..07 | Lane left, lane right, cannot leave edges | 4 |
| GAM-08 | Jump clears and lands, animation matches | 4 |
| GAM-09 | Slide lowers the character, recovers | 4 |
| GAM-10 | Lean animation or tilt on lane change | 4 |
| GAM-11 | Run animation speeds up with game speed | 4 |
| GAM-12 | Pause button works at any time while playing | 4 |
| GAM-13 | No obstacles before 60 m | 5 |
| GAM-14 | Nothing spawns within 40 m of start | 5 |
| GAM-15 | All three obstacle types appear within 2 000 m | 5 |
| GAM-16 | Barricade with no jump → hit | 6 |
| GAM-17 | Jump over barricade → survive | 6 |
| GAM-18 | Slide under beam → survive | 6 |
| GAM-19 | Jump into beam → hit | 6 |
| GAM-20 | Train front → hit | 6 |
| GAM-21 | Moving sideways into a train lane → hit | 6 |
| GAM-22 | Invincible debug switch works | 6 |
| GAM-23 | Coin collected: +1 coin, +10 score, coin disappears | 7 |
| GAM-24 | Coin lines appear in lanes | 7 |
| GAM-25 | Coin arc over barricade collectible during a jump | 7 |
| GAM-26 | Score rises with distance | 7 |
| GAM-27 | Best score survives force-stop and relaunch | 7 |
| GAM-28 | Play Again resets score, coins, speed, obstacles | 7 |

## 6. UI
| ID | Check | Phase |
|----|-------|-------|
| UI-01 | HUD shows pause button (score/coins from Phase 7) | 4 |
| UI-02 | Pause menu: Resume, Restart, Home all work | 4 |
| UI-03 | Countdown shows 3, 2, 1 each 1.0 ± 0.1 s | 4 |
| UI-04 | World frozen (screenshots 1 s apart identical) during countdown | 4 |
| UI-05 | Game over popup appears 0.8 ± 0.2 s after hit | 6 |
| UI-06 | Play Again starts in < 1.5 s | 6 |
| UI-07 | Score/coins update live | 7 |
| UI-08 | "NEW BEST!" shows only when beaten | 7 |
| UI-09 | Popup shows correct score, best, coins | 7 |
| UI-10 | Home from game over → menu with updated best/total coins | 7 |

## 7. Lifecycle
| ID | Check |
|----|-------|
| LIF-01 | Home button then return → PAUSED |
| LIF-02 | Screen off/on → PAUSED |
| LIF-03 | Pull notification shade → PAUSED |
| LIF-04 | Rotating the phone keeps portrait |
| LIF-05 | App switcher then return works |
| LIF-06 | Force-stop and relaunch → menu, saved data present |
| LIF-07 | Background 5 minutes then return: no crash |
| LIF-08 | Audio stops when backgrounded |

## 8. Spawning (JUnit)
| ID | Check |
|----|-------|
| SPN-01 | Over 10 000 seeded rows: every row has a survivable option |
| SPN-02 | Every row with a train has a train-free lane |
| SPN-03 | No beam closer than `speed × 0.9 s` after a barricade in the same lane |
| SPN-04 | Lane change distance reachable within the gap |
| SPN-05 | Determinism per seed |
| SPN-06 | Grace distance 40 m respected |
| SPN-07 | Row gap ≥ `max(14, speed × 0.9)` |
| SPN-08 | Tier gating respected |
| SPN-09 | Every pattern in the library is used |
| SPN-10 | No two obstacles overlap in the same lane |

## 9. Collision, coins, score
| ID | Check |
|----|-------|
| COL-01 | Standing into barricade → hit |
| COL-02 | Jump apex over barricade → no hit |
| COL-03 | Landing before the barricade → hit |
| COL-04 | Standing into beam → hit |
| COL-05 | Jumping into beam → hit |
| COL-06 | Sliding under beam → no hit |
| COL-07 | Any contact with train → hit |
| COL-08 | Obstacle in another lane → no hit |
| COL-09 | Mid lane-change overlapping an occupied lane → hit |
| COL-10 | Exactly touching edges → no hit (strict overlap) |
| COL-11 | Invincible ignores collisions |
| COL-12 | Hit triggers only once |
| CON-01..04 | Pickup radius; other lane not collected; never overlaps obstacles; pattern shapes correct |
| CON-05..08 | Pool recycles; collected coin hidden; coin count +1; arc needs jump timing |
| CON-09..10 | Spin/bob works; no allocations per frame (review + allocation sample) |
| SCR-01..04 | Formula `floor(distance) + 10 × coins`; distance integrates speed; best updates only if greater |
| SCR-05..08 | DataStore write/read; corrupted data → defaults; total coins accumulates; per-run reset |

## 10. Performance (D, test phone recommended, not emulator)
| ID | Target |
|----|--------|
| PRF-01 | ≥ 55 fps average for 60 s |
| PRF-02 | Swipe → lane motion starts < 100 ms |
| PRF-03 | ≥ 55 fps with obstacles and coins |
| PRF-04 | Janky frames < 5% (`gfxinfo`) |
| PRF-05 | RAM < 350 MB after 10 min |
| PRF-06 | < 1 GC event per 10 s while playing |
| PRF-07 | Cold start < 3 s |
| PRF-08 | Release APK < 40 MB |

## 11. Release
| ID | Check |
|----|-------|
| REL-01 | Release APK installs and plays |
| REL-02 | R8 does not break model loading, DataStore or SceneView |
| REL-03..04 | Icon and splash show |
| REL-05..06 | Sound / vibration toggles persist |
| REL-07 | No debug overlay or invincibility in release |
| REL-08 | Permissions = VIBRATE only |
| REL-09 | 10-minute soak, 0 crashes |
| REL-10 | Fresh clone → README steps → builds |

## 12. Regression bundles
- **REG-M1:** BLD-01..04, COR-01..30, INP-01..08, GAM-01..12, UI-01..04, LIF-01..04
- **REG-M2:** REG-M1 + SPN-01..10, COL-01..12, GAM-13..22, UI-05..06
- **REG-M3:** REG-M2 + CON-01..10, SCR-01..08, GAM-23..28, UI-07..10
- **Final:** everything + PRF + LIF + REL

## 13. Bug handling
Every bug found: write a failing test if it is logic (U), fix, rerun the bundle, commit `fix(scope): ...` mentioning the test. Log in `docs/PROGRESS.md` → "Known issues" if not fixed.

## 14. Test report (append to PROGRESS.md each phase)
`Phase N: <passed>/<total>. Failed/N/A: <ids + reason>. Device: <model, Android version>. Evidence: docs/evidence/phase-N/.`
