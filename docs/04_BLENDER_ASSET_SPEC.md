# 04 — Blender Asset Specification

Every 3D asset is built in **Blender via the Blender MCP**, from Python scripts saved in `blender/scripts/`, and exported as **binary glTF (`.glb`)** to `app/src/main/assets/models/`.

## 1. Art direction
Cartoonish, colorful, family-friendly, chunky low-poly (Mario-like simplicity, original designs). Rounded shapes, bevels, no sharp realistic detail. **Flat colours only — no image textures.** Soft, bright, sunny city-metro mood.

### Palette (use only these; add shades only if essential)
| Name | Hex | Use |
|------|-----|-----|
| Sky Blue | `#4FC3F7` | Sky, blue train |
| Sunny Yellow | `#FFD93D` | Coins, cap, lamp light, barricade stripes |
| Coral Red | `#FF6B6B` | Character hoodie, barricade |
| Mint Green | `#6BCB77` | Trees, buildings accent |
| Orange | `#FF9F43` | Orange train, signs |
| Violet | `#7C5CFF` | Building accent, UI accent |
| Cream | `#FFF4E0` | Building walls, highlights |
| Deep Navy | `#1B1F3B` | Outlines of UI, rails, windows |
| Slate | `#8D99AE` | Ballast/ground, platform |
| Brown | `#8B5E3C` | Rail sleepers, wood |
| Skin | `#F2C29B` | Character skin |
| Jeans Blue | `#2F6FED` | Character trousers |
| White | `#FFFFFF` | Eyes, shoes soles, details |

## 2. Global technical rules
| Rule | Value |
|------|-------|
| Units | 1 Blender unit = 1 m (Scene unit scale 1.0, metric) |
| Orientation | Up = +Z in Blender (exports to +Y up). **Characters and vehicles face −Y in Blender** (front view) |
| Origin / pivot | Characters: between feet on the ground (0,0,0). Obstacles: centre of the base on the ground. Track segment: centre of the segment on the ground. Coin: its centre |
| Transforms | Apply scale and rotation (Ctrl+A) before export; scale = 1 |
| Normals | Outward, shade smooth where rounded, flat where blocky |
| Materials | Principled BSDF, base colour only, roughness 0.8, metallic 0, **max 4 materials per file**, named `M_<Color>` (e.g., `M_CoralRed`) |
| Textures | None. No UV maps required |
| Geometry | Triangulate on export. No loose vertices, no hidden or inside faces |
| Compression | **No Draco, no meshopt** |
| Cameras/lights | Not exported |
| File size | Each `.glb` < 1 MB |
| Naming | `snake_case.glb`; objects `Body`, `Wheel_L`; armature `Armature`; bones listed below |

## 3. Asset list and budgets
| File (in `assets/models/`) | Contents | Max triangles | Notes |
|----------------------------|----------|---------------|-------|
| `characters/blitz.glb` | Hero "Blitz" + armature + all clips | 5 000 | Big head, short body, cap, hoodie, sneakers |
| `environment/track_segment.glb` | 30 m ground: ballast bed, 6 rails (3 pairs), sleepers every 1 m, side platform edges | 2 500 | Lane centres x = −2, 0, +2 |
| `environment/building_a.glb` … `building_d.glb` | 4 city buildings, 6–10 m tall, rounded roofs, colourful windows | 400 each | Pivot at base centre; footprint ≤ 6 × 6 m |
| `environment/lamp_post.glb` | Street lamp | 150 | |
| `environment/tree.glb` | Round low-poly tree | 150 | |
| `environment/fence.glb` | 3 m fence piece | 100 | |
| `obstacles/barricade_low.glb` | Striped yellow/red barricade 1.8 × 0.9 × 0.5 m | 300 | Legs + plank |
| `obstacles/overhead_beam.glb` | Gantry: 2 posts + beam; beam spans y 1.1–1.6; 1.8 m wide, 0.5 deep, posts to 2.4 m | 400 | Must look duck-under-able |
| `obstacles/train_blue.glb`, `train_orange.glb` | Cartoon metro train car 1.8 × 3.0 × 10.0 m, rounded nose, windows, stripe | 1 500 each | Pivot at base centre, front faces −Y (Blender) |
| `collectibles/coin.glb` | Chunky gold coin, thickness 0.12, diameter 0.8 with star emboss as geometry | 200 | Faces ±Y for spinning around vertical axis |
| `ui/icon_render.png` | Rendered app icon 1024×1024 (Phase 8) | — | Not a glb |

Scene budget on screen: **≤ 60 000 triangles**, ≤ 25 draw-call materials combined.

## 4. Character "Blitz"
Height 1.7 m (head ≈ 40% of body for cartoon proportion). Parts as separate meshes then joined: head, cap, hair tuft, eyes (2 white + 2 navy pupils), hoodie torso, arms, hands, trousers legs, shoes. Smile via a navy curved piece. No hands detail beyond mitten shape.

### Armature (exact bone names)
```
root
 └ hips
    ├ spine
    │  ├ head
    │  ├ upper_arm.L → lower_arm.L (hand is part of lower_arm)
    │  └ upper_arm.R → lower_arm.R
    ├ thigh.L → shin.L → foot.L
    └ thigh.R → shin.R → foot.R
```
**Skinning method: rigid weights.** Each body part is assigned 100% to one bone (vertex group named after the bone, weight 1.0). Best for blocky characters: no weight painting, no stretching. Join all parts into ONE mesh `Body` with an Armature modifier.

### Required animation clips (exact names, case-sensitive)
| Clip | Frames @30 fps | Loop | Description |
|------|----------------|------|-------------|
| `Idle` | 1–60 | yes | Gentle breathing, head bob, one foot tap |
| `Run` | 1–18 (0.6 s cycle) | yes | Arms pump, knees lift, slight torso lean forward, first = last pose |
| `Jump` | 1–19 (0.62 s) | no | Crouch (3 f) → stretch up → tuck knees at apex → extend legs for landing |
| `Slide` | 1–20 (0.65 s) | no | Quick drop to low slide pose, hold, then rise (last 4 f) |
| `LeanLeft` | 1–6 (0.2 s) | no | Body lean to character's left and back |
| `LeanRight` | 1–6 | no | Mirror |
| `Hit` | 1–24 (0.8 s) | no | Stumble back, stars optional as separate small meshes, end on lying/sitting pose |
Each clip is a separate Blender Action with the **fake user** enabled so it is exported.
`LeanLeft/Right` are optional; if skipped the app tilts the model in code.

## 5. Track segment layout
Length 30 m along Y in Blender (travel direction), width 6.8 m. Ballast Slate base 0.15 m thick. Three lanes centred at x = −2, 0, 2 each with two rails 1.1 m apart (Navy, 0.12 m wide, 0.12 m high) and Brown sleepers (1.6 m × 0.3 m × 0.1 m) every 1.0 m. Side platforms Cream with Yellow edge stripe at x = ±3.4 to ±4.4. Segment ends must tile seamlessly (identical cross-section at both ends).

## 6. Obstacle and coin dimensions (must match GDD hit boxes)
Barricade 1.8×0.5×0.9 · Beam gap bottom 1.1 m · Train 1.8×10×3.0 · Coin Ø 0.8. If a model differs, fix the model, not the hit box.

## 7. Export procedure (every asset)
1. Select only the objects of the asset (and armature for character).
2. Export glTF binary with: format GLB, +Y up, apply modifiers, materials export, animations ON in **Actions** mode (character), cameras/lights OFF, compression OFF.
3. Save to `app/src/main/assets/models/<folder>/<name>.glb`. Save the `.blend` to `blender/source/<name>.blend`.
4. Run the validation script (below).

## 8. Validation (script `tools/validate_glb.py`, written in Phase 1)
For each `.glb`: print file size, mesh count, triangle count (sum of index counts ÷ 3), material count, texture count (must be 0), animation names (character must contain all required clips), and fail if over budget. Output saved to `docs/evidence/phase-N/asset_report.txt`.

## 9. Asset acceptance checklist
- [ ] Meets triangle and material budgets · [ ] Correct size in metres · [ ] Pivot correct · [ ] Faces correct direction · [ ] Palette colours only · [ ] Clip names exact · [ ] Loops are seamless · [ ] Screenshot taken and looks friendly · [ ] `.blend` and script committed · [ ] Loads in the in-app model viewer without crash
