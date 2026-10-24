# 02 — Game Design Document (GDD)

All numbers below are the **defaults** and must live in one Kotlin object, `GameConfig`. Tune there only.

## 1. World and coordinates
- 1 unit = 1 metre. Y up. The player runs toward **−Z**. The camera sits behind the player at +Z.
- **The player stays at z = 0. The world scrolls toward +Z** at `speed` (prevents float drift in endless play).
- Lane index: `0 = left, 1 = middle, 2 = right`. Lane X = `(index − 1) × 2.0` → −2.0, 0.0, +2.0.
- Track visual width 6.8 m (three 2 m lanes + 0.4 m margins). Side platforms and scenery beyond x = ±4.

## 2. Game states
`LOADING → MENU → PLAYING → PAUSED → RESUME_COUNTDOWN → PLAYING → HIT → GAME_OVER → (Play Again) PLAYING / (Home) MENU`
- First start from MENU goes straight to PLAYING (no countdown).
- Resume from PAUSED goes through RESUME_COUNTDOWN (3, 2, 1; 1.0 s each; world frozen; character plays Idle→Run pose frozen).
- Pausing during RESUME_COUNTDOWN returns to PAUSED. App backgrounded in PLAYING/RESUME_COUNTDOWN → PAUSED.

## 3. Player movement
| Property | Value |
|----------|-------|
| Start speed | 9.0 m/s |
| Speed gain | +0.10 m/s per second of play |
| Max speed | 22.0 m/s (reached after ~130 s) |
| Lane change duration | 0.12 s, ease-out. Can happen while jumping. |
| Lane change while changing | Buffered: the next lane command is queued and executed when the current one finishes (max 1 queued) |
| Jump height | 1.7 m, duration 0.62 s. Gravity derived: `g = 8h / T²`, `v0 = 4h / T`. |
| Jump while airborne | Ignored (buffered 0.15 s so a landing-time swipe still jumps) |
| Slide duration | 0.65 s. Swipe up during slide cancels it and jumps. |
| Fast-fall | Swipe down in air sets vertical speed to −14 m/s, then auto-slides on landing |
| Input buffer | 0.15 s for jump/slide commands |
| Swipe detection | Min distance 48 dp, max duration 400 ms, dominant axis wins, triggered as soon as threshold passes (not on finger-up) |
| Tap | In MENU: start. In PLAYING: ignored. |

### Hit boxes (AABB, metres)
| Entity | Width (x) | Height (y) | Depth (z) | Notes |
|--------|-----------|------------|-----------|-------|
| Player standing | 0.8 | 1.7 | 0.6 | Bottom at current y |
| Player sliding | 0.8 | 0.8 | 0.6 | |
| Barricade (low) | 1.8 | 0.9 | 0.5 | Cleared if player bottom y > 0.9 |
| Overhead beam | 1.8 | beam occupies y 1.1 → 1.6 | 0.5 | Slide (0.8 tall) passes; standing/jumping hits |
| Train | 1.8 | 3.0 | 10.0 | Always hits. Cannot be jumped over or stood on |
| Coin | sphere r = 0.5 | | | Collected on overlap |

Jump check: time above 0.9 m is ≈ 0.42 s, which at 22 m/s covers 9 m > obstacle depth + player depth (1.1 m). The jump always clears a barricade at any speed.

## 4. Camera
- Position relative to player: `(x × 0.35, 3.6, +6.0)`; looks at `(x × 0.35, 1.0, −8.0)`. FOV 55° vertical.
- Smooth follow on X (lerp factor 8/s). Slight FOV widen +3° at max speed. Small shake 0.15 s on hit.
- Menu camera: closer, character fills ~45% of screen height, slowly orbits ±8° (optional).

## 5. Endless world generation
- **Track segment:** 30 m long. Pool of **7 segments** (covers 120 m ahead + 30 m behind + margin).
- Each frame: `segment.z += speed × dt`. When `segment.z > +30` (fully behind), move it to `farthest.z − 30` and re-decorate (random building/prop variants, seeded).
- Spawn horizon: **120 m ahead**. Despawn at z > +10. Optional fog from 70 m to 130 m, colour matches sky, hides pop-in.
- Scenery per segment (both sides): 2–3 buildings from 4 variants, 1–2 lamp posts, 0–2 trees, fence pieces. Never overlap the track.

## 6. Obstacles (M2)
| Type | Count in GLB | Player answer |
|------|-------------|---------------|
| `BARRICADE_LOW` | 1 lane wide | Jump (or change lane) |
| `OVERHEAD_BEAM` | 1 lane wide | Slide (or change lane) |
| `TRAIN` | 1 lane wide, 10 m long | Change lane only |

### Row-based spawning
The spawner places **rows**. A row = one decision for the player across 3 lanes, e.g. `[TRAIN, free, BARRICADE_LOW]`.
- **Row gap (distance between row starts):** `max(14 m, speed × 0.9 s + train-length if previous row had a train)`.
- A row is selected from the pattern library for the current **difficulty tier**, may be mirrored left/right using the seeded RNG.
- Rows are spawned at the spawn horizon as the world scrolls.

### Difficulty tiers (by distance travelled)
| Tier | Distance | Allowed rows |
|------|----------|--------------|
| 0 | 0–60 m | none (warm-up). In M1 builds this tier is infinite |
| 1 | 60–400 m | 1 obstacle (barricade or beam), 2 free lanes |
| 2 | 400–1000 m | 2 obstacles in a row (different or same types), 1 free lane; single train |
| 3 | 1000 m+ | mixes of train + barricade + beam, double trains, 2-row combos, jump-then-slide sequences |

### Fairness invariants (enforced by unit tests over 10 000 seeded rows)
1. Every row has **≥ 1 lane that is free or passable by jump/slide**. No row is all trains.
2. A lane that is free of trains must exist in every row that contains a train.
3. Never place a barricade immediately followed by a beam in the **same lane** closer than `speed × 0.9 s` (landing time).
4. Lane changes of 2 lanes between consecutive rows require the gap ≥ `speed × 0.9 s` (always true by the gap formula).
5. Same seed → same sequence (determinism).
6. Obstacles never spawn closer than 40 m from the player at game start (grace distance).

## 7. Coins (M3)
- Coin pickup radius 0.5 m, drawn at y = 0.9 (ground lines) or y = 1.7–2.4 (arc over a barricade).
- Patterns: line of 5–8 coins spaced 2.0 m in one lane; arc of 7 coins over a low barricade; zig-zag line across lanes (tier 2+). Coins never overlap obstacles.
- Coins spin (rotate Y at 180°/s) and bob slightly. Collected coin: scale-down pop, +1 coin, short sound.
- Spawn probability: a coin pattern between roughly every second or third obstacle row, plus in the safe lane of a row.

## 8. Scoring
- `score = floor(distanceMetres) + coinsCollectedThisRun × 10`
- HUD shows live Score and Coins. Persist `bestScore` and `totalCoins` (DataStore).
- "NEW BEST!" shown if `score > previousBest`.

## 9. Hit and game over
- Any collision with an obstacle → state HIT: world stops decelerating over 0.3 s, character plays `Hit`, camera shake, vibration (if enabled), sound.
- After 0.8 s show Game Over popup. One life only.
- Debug: `invincible` flag ignores collisions (debug builds).

## 10. Animation state mapping
| Situation | Clip | Loop | Notes |
|-----------|------|------|-------|
| Menu | `Idle` | yes | |
| Running | `Run` | yes | Playback speed scales `0.9 + 0.5 × (speed − 9) / 13` |
| Jump | `Jump` | no | duration matched to 0.62 s |
| Slide | `Slide` | no | 0.65 s |
| Lane change | `LeanLeft` / `LeanRight` | no | 0.2 s; if clips missing, tilt the model ±15° in code |
| Hit | `Hit` | no | 0.8 s |
| Resume countdown | `Run` paused on first frame | | |

## 11. Audio (Release phase)
SFX: jump, slide, lane swipe (soft), coin, hit, UI tap, countdown tick. Music: one upbeat loop 60–90 s, 70% volume. Master toggles for Sound and Vibration saved in settings.

## 12. Look and feel
Palette in `04_BLENDER_ASSET_SPEC.md`. Bright sky gradient, soft fog, warm directional light + ambient fill, **no real-time shadows** (use a round blob shadow under the character). Everything rounded, chunky, friendly.

## 13. Balance reference
An obstacle spawned 120 m ahead gives 13 s of warning at 9 m/s and 5.4 s at 22 m/s. The row gap formula keeps the time between two decisions at 0.9 s or more at every speed.
