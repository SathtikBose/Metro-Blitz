# 05 — Blender MCP Playbook

How the agent builds assets reliably. The MCP gives the agent tools to inspect the scene, take viewport screenshots, and **execute Python (`bpy`) inside the running Blender**. Everything is done by Python.

## 1. Before the first asset
1. Blender is open with the MCP addon enabled and **server started** (sidebar → Blender MCP tab). Default socket port is 9876.
2. Agent calls the scene-info tool. If it fails: ask the human to press "Start server" and wait.
3. Agent saves a new file `blender/source/metro_blitz_work.blend` and sets units (metric, scale 1.0), FPS 30.
4. Agent checks what is installed: `bpy.app.version_string`. The glTF exporter parameter names below are for Blender 4.x; if an argument errors, print the valid ones with `bpy.ops.export_scene.gltf.get_rna_type().properties.keys()` and adapt.

## 2. Working loop (for every asset)
1. **Write the script to a file** `blender/scripts/<asset>.py` (idempotent: it removes its own old objects first, then rebuilds).
2. **Run it** through the MCP's execute-code tool by reading the file and executing it (send in chunks if large).
3. **Look**: take a viewport screenshot from front, side and 3/4 view. Judge: proportions, floating parts, colours, direction.
4. **Fix** and rerun. Maximum 6 iterations per asset, then accept and log what is imperfect.
5. **Report numbers** with code: dimensions, triangle count, materials.
6. **Export** `.glb`, **validate**, **save** `.blend`, **commit**.
Never build assets by sending one giant unstructured script; build in small named functions (`make_head()`, `make_body()`).

## 3. Rules
- Delete only objects you created (tag them with a custom property `mb_asset = "<name>"` or a name prefix). Never reset factory settings or purge everything: that can disconnect the addon.
- Use helper functions below, so colours and units are identical across assets.
- Keep primitives low-poly: cubes with Bevel modifier (segments 2), UV spheres 12×8, cylinders 12 verts. Apply modifiers before export (`export_apply=True`).
- Do not use external asset downloads, AI mesh generators or textures for this project (keeps style consistent and free of licensing questions). Hyper3D/Hunyuan/PolyHaven/Sketchfab integrations stay OFF.
- Save the `.blend` after every asset.

## 4. Helper snippets (put in `blender/scripts/mb_helpers.py`, import via `exec(open(path).read())`)
```python
import bpy, math, bmesh

def srgb_to_lin(c):
    return c/12.92 if c <= 0.04045 else ((c+0.055)/1.055) ** 2.4

def make_mat(name, hex_color, roughness=0.8):
    mat = bpy.data.materials.get(name) or bpy.data.materials.new(name)
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes.get("Principled BSDF")
    r, g, b = (int(hex_color[i:i+2], 16)/255 for i in (1, 3, 5))
    bsdf.inputs["Base Color"].default_value = (srgb_to_lin(r), srgb_to_lin(g), srgb_to_lin(b), 1)
    bsdf.inputs["Roughness"].default_value = roughness
    bsdf.inputs["Metallic"].default_value = 0.0
    return mat

def clear_asset(prefix):
    for o in [o for o in bpy.data.objects if o.name.startswith(prefix)]:
        bpy.data.objects.remove(o, do_unlink=True)

def add_box(name, size, loc, mat, bevel=0.0):
    bpy.ops.mesh.primitive_cube_add(size=1, location=loc)
    o = bpy.context.active_object
    o.name = name
    o.scale = size
    bpy.ops.object.transform_apply(scale=True)
    if bevel > 0:
        m = o.modifiers.new("Bevel", 'BEVEL'); m.width = bevel; m.segments = 2
    o.data.materials.append(mat)
    bpy.ops.object.shade_smooth()
    return o

def add_cylinder(name, radius, depth, loc, mat, verts=12, rot=(0, 0, 0)):
    bpy.ops.mesh.primitive_cylinder_add(vertices=verts, radius=radius, depth=depth, location=loc, rotation=rot)
    o = bpy.context.active_object; o.name = name; o.data.materials.append(mat)
    return o

def add_sphere(name, radius, loc, mat, scale=(1, 1, 1)):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=12, ring_count=8, radius=radius, location=loc)
    o = bpy.context.active_object; o.name = name; o.scale = scale
    bpy.ops.object.transform_apply(scale=True); o.data.materials.append(mat)
    bpy.ops.object.shade_smooth()
    return o

def tri_count(objs):
    total = 0
    for o in objs:
        me = o.evaluated_get(bpy.context.evaluated_depsgraph_get()).to_mesh()
        me.calc_loop_triangles(); total += len(me.loop_triangles)
    return total
```

## 5. Rigging the character (rigid skinning)
```python
# 1) Create armature in edit mode with exact bone names from 04_BLENDER_ASSET_SPEC.md
bpy.ops.object.armature_add(enter_editmode=True, location=(0, 0, 0))
arm = bpy.context.active_object; arm.name = "Armature"
eb = arm.data.edit_bones
# rename the default bone to "root", then eb.new("hips") etc., set .head / .tail / .parent explicitly
bpy.ops.object.mode_set(mode='OBJECT')

# 2) Rigid weights: each part gets 100% of one bone
def bind_part(obj, bone_name):
    vg = obj.vertex_groups.new(name=bone_name)
    vg.add(list(range(len(obj.data.vertices))), 1.0, 'REPLACE')

# 3) Join all parts into one mesh named "Body", add Armature modifier
bpy.ops.object.select_all(action='DESELECT')
for o in parts: o.select_set(True)
bpy.context.view_layer.objects.active = parts[0]
bpy.ops.object.join(); body = bpy.context.active_object; body.name = "Body"
mod = body.modifiers.new("Armature", 'ARMATURE'); mod.object = arm
body.parent = arm
```
Check: rotate a bone in Pose mode through the MCP, screenshot, confirm only that part moves.

## 6. Animating (keyframes through Python)
```python
def new_action(arm, name):
    act = bpy.data.actions.new(name); act.use_fake_user = True
    arm.animation_data_create(); arm.animation_data.action = act
    return act

def key_pose(arm, frame, pose):   # pose = {"thigh.L": (rx, ry, rz) in degrees, ...}
    for bone, rot in pose.items():
        pb = arm.pose.bones[bone]; pb.rotation_mode = 'XYZ'
        pb.rotation_euler = tuple(math.radians(a) for a in rot)
        pb.keyframe_insert("rotation_euler", frame=frame)
    # root/hips location bounce:  pb.location = (...); pb.keyframe_insert("location", frame=frame)
```
- Reset all bones to rest pose before each clip's first key.
- Looping clips: last frame pose = first frame pose; set interpolation Bezier for natural motion; for `Run` use 4 key poses (contact, down, passing, up) mirrored.
- After creating a clip, push each action into the NLA or keep fake user so export includes it; verify with `[a.name for a in bpy.data.actions]`.
- Scrub the clip by setting `bpy.context.scene.frame_set(n)` and taking screenshots at 3–4 frames to check the motion.

## 7. Export
```python
def export_glb(path, objects):
    bpy.ops.object.select_all(action='DESELECT')
    for o in objects: o.select_set(True)
    bpy.ops.export_scene.gltf(
        filepath=path, export_format='GLB', use_selection=True,
        export_apply=True, export_yup=True, export_materials='EXPORT',
        export_cameras=False, export_lights=False,
        export_animations=True, export_animation_mode='ACTIONS',
        export_draco_mesh_compression_enable=False)
```
For static assets, pass `export_animations=False`.

## 8. Quality screenshots
Take and save: front, side, top, 3/4 view to `docs/evidence/phase-N/<asset>_*.png`. Add one image of the whole character posed in each clip's key frame.

## 9. Common problems
| Problem | Fix |
|---------|-----|
| Executed code does nothing / MCP timeout | Split script, keep each call < 30 s |
| Parts look black | Normals flipped → `bpy.ops.mesh.normals_make_consistent` |
| Character faces the wrong way in the app | Re-check it faces −Y in Blender; fix in Blender, not with magic numbers |
| Animations missing in `.glb` | Action not exported: enable fake user, export mode ACTIONS, re-validate |
| Model huge/tiny in app | Unapplied scale; apply transforms; check dimensions in script output |
| Seam gaps on track segment | Cross-section differs at ends; snap ends to ±15 m exactly |
| MCP disconnects | Ask human to press Start server; scripts are on disk so nothing is lost |
