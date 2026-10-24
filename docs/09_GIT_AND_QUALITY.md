# 09 — Git Workflow and Quality Standards

## 1. Branching
Solo project: work on `main` with small, stable commits. Optional: `phase/N-name` branches merged with `--no-ff` if you prefer review points. `main` must always build, launch and pass unit tests.

## 2. Commit rule
**Commit after every stable build** (builds + unit tests pass + app launches). A phase has several commits. Never commit generated build output, local config or secrets.

## 3. Commit message format (Conventional Commits)
```
<type>(<scope>): <imperative summary, ≤ 72 chars, no period>

<optional body: what and why, wrapped at 80 cols>

<optional footer: Refs: Phase N, Tests: COR-01..12 pass>
```
Types: `feat` `fix` `refactor` `perf` `test` `docs` `build` `chore` `style` `revert`.
Scopes: `core` `render` `ui` `input` `data` `audio` `blender` `assets` `app` `tools`.

**Good:** `feat(core): add lane switching with input buffering`
**Good:** `fix(render): hide pooled obstacles after recycle`
**Bad:** `update`, `fixed bug`, `changes`, `final final`

Body example:
```
feat(core): add fair spawn director

Rows are chosen from the pattern library by difficulty tier. The director
guarantees at least one survivable lane and a minimum gap of speed x 0.9 s.

Refs: Phase 5, Tests: SPN-01..10 pass
```

## 4. Tags
`phase-N` after each phase; `v0.1.0` (M1), `v0.2.0` (M2), `v0.3.0` (M3), `v1.0.0` (release). Use annotated tags: `git tag -a v0.1.0 -m "M1: playable endless runner"`.

## 5. `.gitignore` must contain
```
*.iml
.gradle/
/local.properties
/.idea/
/build/
/app/build/
*.apk
*.aab
/release/
*.blend1
*.blend2
/captures/
.DS_Store
```
Do commit: `blender/scripts/*.py`, `blender/source/*.blend`, `app/src/main/assets/models/*.glb`, `docs/evidence/` (screenshots, small), `tools/`.

## 6. Pre-commit checklist (agent runs it mentally every time)
- [ ] `tools\build_test.ps1` passes · [ ] App launches on device/emulator · [ ] No debug prints left · [ ] No unused files · [ ] `PROGRESS.md` updated at phase ends · [ ] Message follows the format

## 7. Code standards
- Kotlin official style; max line 120; names descriptive (`laneChangeDurationSeconds`, not `lcd`).
- Immutable by default (`val`), no globals except `GameConfig`.
- `core/` has **zero** Android/Compose/Filament imports (enforce with a unit test that scans imports, or a Gradle check).
- KDoc on every public class in `core/` and `render/` (1–2 lines: what and why).
- No magic numbers: constants live in `GameConfig`.
- No allocations inside `update()` loops: use preallocated arrays/ring buffers; avoid `map`/`filter` chains per frame.
- Error handling at boundaries (asset load, DataStore), not scattered try/catch.
- Logging tag `MetroBlitz`; log state transitions and spawn seeds at debug level.
- Public behaviour covered by unit tests; every fixed bug gets a regression test.

## 8. Definition of Done (per task)
Code compiles · tests written and green · documented if public · no new lint warnings · works on device · committed with a proper message.

## 9. Definition of Done (per phase)
All phase tests green · evidence screenshots saved · `PROGRESS.md` updated · tag created · Phase Report printed · stop and wait for `next`.

## 10. Rollback
If a change breaks the game and cannot be fixed in two attempts: `git revert <commit>` or `git reset --hard <last good tag>` (after telling the human), then try a smaller approach.
