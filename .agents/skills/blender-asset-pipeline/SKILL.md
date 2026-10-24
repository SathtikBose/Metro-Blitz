---
name: blender-asset-pipeline
description: Use when creating, rigging, animating, exporting or validating any 3D asset for Metro Blitz through the Blender MCP (character, track, buildings, obstacles, coin, app icon).
---

# Blender asset pipeline (Metro Blitz)

Read first: `docs/04_BLENDER_ASSET_SPEC.md` and `docs/05_BLENDER_MCP_PLAYBOOK.md`.

## Steps
1. Confirm the Blender MCP responds (scene info). If not, ask the human to press "Start server" in Blender.
2. Write or update the asset script in `blender/scripts/<asset>.py`. Reuse `mb_helpers.py`. Scripts must be re-runnable (remove own old objects first).
3. Run the script through the MCP in small chunks. Never reset factory settings or delete objects you did not create.
4. Screenshot front, side, 3/4. Judge proportions, floating parts, direction (faces -Y in Blender), palette colours. Fix and rerun (max 6 iterations).
5. Print dimensions, triangle count, material count. Compare with the budget table.
6. Export `.glb` (no Draco, no textures, animations in ACTIONS mode for the character) into `app/src/main/assets/models/<folder>/`.
7. Run `python tools/validate_glb.py`. Save the report in `docs/evidence/phase-N/`.
8. Save `blender/source/<asset>.blend`. Commit script, blend and glb together: `feat(blender): ...` / `feat(assets): ...`.

## Never
Use image textures, AI mesh generators, downloaded assets, more than 4 materials per file, exact clip names other than the spec.
