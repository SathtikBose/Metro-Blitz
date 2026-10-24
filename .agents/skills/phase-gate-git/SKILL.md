---
name: phase-gate-git
description: Use at the end of every Metro Blitz phase (and for every stable build) to run tests, commit with a professional message, tag, update PROGRESS.md, print the Phase Report and stop for the human's "next".
---

# Phase gate and git

Read first: `docs/07_PHASE_PLAN.md`, `docs/09_GIT_AND_QUALITY.md`.

## Procedure
1. Run all tests assigned to the phase (see phase plan). Fix failures; do not skip.
2. `git status` — only intended files. No build output, keystores or `local.properties`.
3. Commit with Conventional Commits: `type(scope): imperative summary` + body + `Refs: Phase N, Tests: ...`.
4. Tag: `git tag -a phase-N -m "<phase name>"` (and the milestone tag if the phase is 4, 6, 7 or 8).
5. Update `docs/PROGRESS.md`: checklist, decisions, versions, known issues, test results.
6. Print the Phase Report from `AGENTS.md` section 6.
7. STOP. Wait for the human to type `next`.

## Never
Commit a broken build, use messages like "update" or "wip", or start the next phase without `next`.
