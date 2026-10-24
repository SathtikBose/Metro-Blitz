# 11 — Risks, Fallbacks and Scope Control

## 1. Risk register
| # | Risk | Likelihood | Impact | Prevention | Fallback |
|---|------|-----------|--------|------------|----------|
| R1 | Agent guesses a wrong SceneView/Compose API | High | High | Context7 lookup before use; all 3D calls isolated in `render/`; small compile-run loops | Read the library sample app in the repo; use `sceneview-mcp`; as last resort switch the renderer (R9) |
| R2 | Animation clips missing/misnamed in `.glb` | Medium | High | Fake-user actions, ACTIONS export, `validate_glb.py` lists clips | Export each clip as its own `.glb` and load per clip; or build one NLA track per clip |
| R3 | Character looks ugly or proportions wrong | Medium | Medium | Screenshot review loop, palette, reference proportions in `04` | Simplify design: bean-shaped body + round head + cap; accept "cute blocky" |
| R4 | Rigging/weights fail through script | Medium | High | Rigid per-part weights (no weight paint) | Animate parts as separate objects parented to bones, export node animation |
| R5 | Blender MCP disconnects or times out | High | Low | Scripts saved on disk, small calls, save `.blend` often | Human presses Start server; rerun script file |
| R6 | Emulator renders black or slow | Medium | Medium | Use real phone | Emulator graphics = Hardware; update GPU drivers |
| R7 | Performance below target | Medium | Medium | Budgets, pools, no shadows, flat materials | Dynamic resolution, fewer buildings, lower fog distance, limit 30 fps on low-end |
| R8 | Agent context lost mid-project | High | Medium | `docs/PROGRESS.md`, phase gate, AGENTS.md | New chat + resume prompt |
| R9 | SceneView cannot do what we need | Low | High | ADR-001 and engine-free `core/` | Replace `render/` with libGDX + glTF loader; keep `core/`, `input/`, `data/`, UI overlays rebuilt in Android Views/Compose |
| R10 | Quota / rate limit hit | Medium | Medium | Phase gating, small context, no huge file rewrites | Wait/reset; switch model; continue from PROGRESS.md |
| R11 | Unfair or impossible obstacle patterns | Medium | High | Fairness invariants + 10 000-row tests + optimal bot test | Reduce pattern library to the safe subset |
| R12 | Gesture feels laggy | Medium | High | Trigger on threshold, not finger-up; input buffer | Lower threshold to 36 dp |
| R13 | Float drift / precision on long runs | Low | Medium | World moves, player fixed; 1 h simulation test | Reset origin periodically |
| R14 | Release (R8) build crashes | Medium | Medium | Test release APK in Phase 8 | Add keep rules for SceneView/Filament or disable shrinking for the college build |
| R15 | Scope creep | High | Medium | Out-of-scope list; "Ideas (not built)" section | Use the cut list below |
| R16 | Licensing issues with audio/fonts | Low | Medium | CC0/OFL only, `CREDITS.md` | Remove the asset, ship silent |

## 2. Cut list (if time or quota runs short, cut in this order, never cut tests)
1. Speed lines, landing dust, fog polish 2. Music (keep SFX) 3. Zig-zag coin patterns 4. `LeanLeft/LeanRight` clips (use code tilt) 5. Second train colour 6. Fast-fall 7. Building variants c and d 8. Menu camera orbit.
**Never cut:** pause + countdown, fairness tests, game over, best score, hit animation, portrait lock.

## 3. Decision triggers
- If a phase exceeds 3 failed attempts on the same problem → log in PROGRESS.md, apply fallback, tell the human.
- If frame rate < 30 on the test phone after the performance pass → apply R7 fallbacks in order.
- If the Blender character is not acceptable after 6 iterations → apply R3, move on.

## 4. Known platform facts to remember
Android Studio emulator ≠ real GPU. Portrait only. Min API 26. Blender glTF export is Y-up. Rule files in Antigravity are limited to 12 000 characters each.
