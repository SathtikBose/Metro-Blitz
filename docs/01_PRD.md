# 01 — Product Requirements Document (PRD)

## 1. Vision
A polished, family-friendly 3D endless runner that feels like Subway Surfers but is small enough to be built end-to-end by an AI agent: simple cartoon low-poly visuals, one character, one environment, three obstacle types, one collectible.

## 2. Goals and non-goals
**Goals**
1. Smooth, responsive, fair endless gameplay in portrait mode.
2. All 3D assets generated in Blender via MCP, exported as `.glb`.
3. Clean, tested Kotlin codebase with phase-wise git history.
4. APK that installs and runs on a mid-range Android phone (Android 8.0+).

**Non-goals** — see "Out of scope" in `00_README_START_HERE.md`.

## 3. Player and context
Casual player, one-handed, short sessions (1–5 min). Understands swipes without a tutorial. Plays on a phone held in portrait.

## 4. Feature list (MoSCoW)
| ID | Feature | Priority | Milestone |
|----|---------|----------|-----------|
| F01 | Home screen: character idle on track, "TAP TO PLAY" | Must | M1 |
| F02 | Tap anywhere starts the run | Must | M1 |
| F03 | 3 lanes; swipe left/right changes lane (smooth) | Must | M1 |
| F04 | Swipe up = jump; swipe down = slide (and fast-fall in air) | Must | M1 |
| F05 | Endless generated track + scenery, no visible pop-in | Must | M1 |
| F06 | Speed increases gradually up to a cap | Must | M1 |
| F07 | Pause button, pause menu, 3-2-1 get-ready after resume | Must | M1 |
| F08 | Auto-pause when app goes to background | Must | M1 |
| F09 | Character animations: idle, run, jump, slide, lean L/R, hit | Must | M1/M2 |
| F10 | Obstacles: low barricade, overhead beam, train | Must | M2 |
| F11 | Fair pattern-based obstacle spawning with difficulty tiers | Must | M2 |
| F12 | Collision → hit animation → game over | Must | M2 |
| F13 | Coins: spawn in lines/arcs, collect on touch | Must | M3 |
| F14 | Score = distance + coin bonus; HUD shows score and coins | Must | M3 |
| F15 | Best score and total coins saved on device | Must | M3 |
| F16 | Game over popup: score, best, coins, Play Again, Home | Must | M3 |
| F17 | Sound effects + music with on/off toggle | Should | Release |
| F18 | Vibration on hit with on/off toggle | Should | Release |
| F19 | App icon (rendered in Blender), splash | Should | Release |
| F20 | Debug overlay (FPS, speed, lane, state) debug builds only | Should | M1 |
| F21 | Moving trains, coin magnet, power-ups | Won't | — |

## 5. User stories and acceptance criteria
- **US1** As a player I open the app and see the character waiting. *AC:* within 3 s of launch the 3D scene is visible, character plays Idle, text "TAP TO PLAY" pulses.
- **US2** I tap and the run starts instantly. *AC:* first tap starts movement within 150 ms; no countdown on first start.
- **US3** I swipe to dodge. *AC:* swipe recognized in under 100 ms; lane change takes 0.12 s; never skips two lanes for one swipe.
- **US4** I pause and resume. *AC:* pause freezes everything; Resume shows 3-2-1 (1 s each) with the world frozen; game continues exactly where it stopped.
- **US5** I hit something and the game ends clearly. *AC:* hit animation plays, game over popup appears after about 0.8 s; Play Again restarts in under 1.5 s.
- **US6** I want to beat my best. *AC:* best score persists after killing the app; "NEW BEST!" shown when beaten.
- **US7** The game never feels unfair. *AC:* automated test proves every generated row has at least one survivable option.

## 6. Quality requirements (non-functional)
| Area | Target |
|------|--------|
| Frame rate | 60 fps on mid-range phone (Snapdragon 7-class), ≥ 30 fps on low-end |
| Cold start to interactive | < 3 s on mid-range |
| APK size | < 40 MB (debug < 60 MB) |
| RAM | < 350 MB peak after 10 min |
| Crashes | 0 in a 10-minute soak test |
| Permissions | Only `VIBRATE`. No internet. |
| Min Android | 8.0 (API 26). Needs OpenGL ES 3.0+ |
| Accessibility | Text ≥ 16 sp, high contrast, sound/vibration toggles, no flashing > 3 Hz |

## 7. Content and safety
Family-friendly: no violence, blood, scary elements. The "hit" is a cartoon stumble with stars, not injury. No real brands, no copyrighted characters. Sounds and fonts must be CC0/OFL and listed in `CREDITS.md`.

## 8. Success metrics (for the college demo)
- 10-minute soak without crash. · 60 fps on test phone. · All must-have tests pass. · 3+ friends can play without explanation.

## 9. Deliverables
Source repo with phase tags, release APK, README with screenshots, docs folder, short demo video.
