# Metro Blitz — Start Here

**Metro Blitz** is a cartoon 3D endless runner for Android (portrait). You run on 3 metro tracks, swipe to change lane, jump over barricades, slide under beams, dodge trains, and collect coins. Speed increases forever. All 3D assets are built in Blender through the Blender MCP; all game code is Kotlin. Built by an AI agent, phase by phase.

- App name: **Metro Blitz** · Package: `com.buildstack.metroblitz`
- Audience: family-friendly, ages 8+. Colorful, simple, low-poly.
- Use: college project + BuildStack YouTube video.

## Reading order (agent: read ALL before writing code)
| # | File | Purpose |
|---|------|---------|
| 00 | `00_README_START_HERE.md` | Index, rules, priority of truth |
| 01 | `01_PRD.md` | What we build and why, scope per version |
| 02 | `02_GAME_DESIGN_DOCUMENT.md` | Exact gameplay rules and numbers |
| 03 | `03_TECH_ARCHITECTURE.md` | Code structure, game loop, state machine |
| 04 | `04_BLENDER_ASSET_SPEC.md` | Every 3D asset, budgets, names, animations |
| 05 | `05_BLENDER_MCP_PLAYBOOK.md` | How to drive Blender MCP safely, code snippets |
| 06 | `06_UI_UX_SPEC.md` | Screens, colors, text, flows |
| 07 | `07_PHASE_PLAN.md` | Phases 0–8, tasks, exit criteria, commits, tags |
| 08 | `08_TEST_PLAN.md` | Every test case, adb commands, performance targets |
| 09 | `09_GIT_AND_QUALITY.md` | Git flow, commit format, code standards |
| 10 | `10_SETUP_AND_TOOLING.md` | Install guide (Windows), phone setup, glossary |
| 11 | `11_RISKS_AND_FALLBACKS.md` | What can go wrong and the planned fallback |
| 12 | `12_RELEASE_CHECKLIST.md` | Final polish, APK, submission |

Root file `AGENTS.md` holds the permanent rules. `docs/PROGRESS.md` is the living log the agent creates in Phase 0 and updates after every phase.

## Priority of truth (if docs disagree)
1. `02_GAME_DESIGN_DOCUMENT.md` for gameplay numbers
2. `04_BLENDER_ASSET_SPEC.md` for asset names/budgets
3. `03_TECH_ARCHITECTURE.md` for code structure
4. `07_PHASE_PLAN.md` for order of work
5. Everything else

## Versions (milestones)
| Milestone | Gameplay | Phases | Git tag |
|-----------|----------|--------|---------|
| **M1 (v1)** | Tap to play, endless map, run, left/right, jump, slide, speed ramp, pause + 3-2-1 | 0–4 | `v0.1.0` |
| **M2 (v2)** | Barricades, overhead beams, trains, fair spawning, collision, hit animation | 5–6 | `v0.2.0` |
| **M3 (v3)** | Coins, score, best score, game over popup, play again | 7 | `v0.3.0` |
| **Release** | Sound, haptics, icon, polish, performance, release APK | 8 | `v1.0.0` |

## Things the original idea was missing (now included)
Camera rig and exact numbers · fair obstacle generation rules · input buffering · app pause on phone call/home button · persistent best score and coins · sound and vibration toggles · app icon and splash · performance budgets · object pooling · test plan · risk fallbacks · asset naming and export rules · palette · debug tools · release checklist · a progress log so the AI never loses context.

## Out of scope (do NOT build)
Multiplayer, accounts, ads, in-app purchases, shop/characters unlock, power-ups, missions, leaderboards, moving trains, jumping on top of trains, iOS, tablets layout, landscape, cloud save. These are listed as future ideas only.

## Success = the game is playable
A stranger can open the app, tap, and play for 5 minutes without a crash, at smooth frame rate, and want to try again after game over.
