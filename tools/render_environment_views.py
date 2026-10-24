"""
tools/render_environment_views.py
Renders evidence screenshots for Phase 2:
- Three track segments tiling seamlessly (ENV-01)
- Street view with buildings, lamp posts, trees, fences (ENV-02, ENV-03)
- Individual environment props overview
Saves images to docs/evidence/phase-2/ and generates asset_report.txt.
"""
import bpy
import os
import math
import subprocess

def render_env():
    evidence_dir = os.path.abspath("docs/evidence/phase-2")
    os.makedirs(evidence_dir, exist_ok=True)

    # Reset scene
    bpy.ops.wm.read_factory_settings(use_empty=True)
    scene = bpy.context.scene
    scene.render.engine = 'BLENDER_EEVEE_NEXT' if hasattr(bpy.types, 'RenderEngine') and 'BLENDER_EEVEE_NEXT' in [e.identifier for e in bpy.types.RenderSettings.bl_rna.properties['engine'].enum_items] else 'BLENDER_EEVEE'
    scene.render.resolution_x = 720
    scene.render.resolution_y = 1280
    scene.render.film_transparent = False

    # Sky background color (#4FC3F7)
    world = bpy.data.worlds.new("SkyWorld")
    world.use_nodes = True
    bg_node = world.node_tree.nodes.get("Background")
    if bg_node:
        bg_node.inputs["Color"].default_value = (0.07, 0.54, 0.94, 1.0)
    scene.world = world

    # Camera
    cam_data = bpy.data.cameras.new("EnvCam")
    cam_data.lens = 35
    cam_obj = bpy.data.objects.new("EnvCam", cam_data)
    scene.collection.objects.link(cam_obj)
    scene.camera = cam_obj

    # Sun Light
    sun_data = bpy.data.lights.new("Sun", 'SUN')
    sun_data.energy = 3.5
    sun_obj = bpy.data.objects.new("Sun", sun_data)
    sun_obj.rotation_euler = (math.radians(50), math.radians(20), math.radians(-40))
    scene.collection.objects.link(sun_obj)

    # Import track segments: 3 consecutive segments at y = -30, y = 0, y = +30
    track_glb = os.path.abspath("app/src/main/assets/models/environment/track_segment.glb")
    for y_offset in [-30.0, 0.0, 30.0]:
        bpy.ops.import_scene.gltf(filepath=track_glb)
        imported = bpy.context.selected_objects
        for obj in imported:
            obj.location = (0, y_offset, 0)

    # 1. Screenshot: Three segments tiling check (high overhead perspective looking along the seams)
    cam_obj.location = (0, -20, 18)
    cam_obj.rotation_euler = (math.radians(55), 0, 0)
    scene.render.filepath = os.path.join(evidence_dir, "env_three_segments_tiling.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # Now add buildings and props along left (x = -7.5) and right (x = 7.5) sides (no intrusion on |x| < 4)
    prop_files = {
        "building_a": ("app/src/main/assets/models/environment/building_a.glb", [(-7.2, -15, 0), (7.2, 15, 0)]),
        "building_b": ("app/src/main/assets/models/environment/building_b.glb", [(7.2, -15, 0), (-7.2, 15, 0)]),
        "building_c": ("app/src/main/assets/models/environment/building_c.glb", [(-7.2, 0, 0)]),
        "building_d": ("app/src/main/assets/models/environment/building_d.glb", [(7.2, 0, 0)]),
        "lamp_post":  ("app/src/main/assets/models/environment/lamp_post.glb",  [(-4.0, -10, 0.2), (-4.0, 10, 0.2), (4.0, -5, 0.2), (4.0, 15, 0.2)]),
        "tree":       ("app/src/main/assets/models/environment/tree.glb",       [(-4.5, -20, 0.2), (-4.5, 0, 0.2), (4.5, -25, 0.2), (4.5, 5, 0.2)]),
        "fence":      ("app/src/main/assets/models/environment/fence.glb",      [(-3.95, -2, 0.2), (-3.95, 1, 0.2), (3.95, -2, 0.2), (3.95, 1, 0.2)])
    }

    for name, (path, positions) in prop_files.items():
        abs_path = os.path.abspath(path)
        for pos in positions:
            bpy.ops.import_scene.gltf(filepath=abs_path)
            for o in bpy.context.selected_objects:
                o.location = pos
                # Face toward street
                if pos[0] > 0 and "building" in name:
                    o.rotation_euler = (0, 0, math.radians(180))

    # 2. Screenshot: Street runner perspective (player height looking down the tracks)
    cam_obj.location = (0, -25, 3.2)
    cam_obj.rotation_euler = (math.radians(82), 0, 0)
    scene.render.filepath = os.path.join(evidence_dir, "env_street_perspective.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # Generate asset_report.txt
    print("Generating asset_report.txt...")
    report_path = os.path.join(evidence_dir, "asset_report.txt")
    result = subprocess.run([
        "python", "tools/validate_glb.py",
        "app/src/main/assets/models/environment/track_segment.glb",
        "app/src/main/assets/models/environment/building_a.glb",
        "app/src/main/assets/models/environment/building_b.glb",
        "app/src/main/assets/models/environment/building_c.glb",
        "app/src/main/assets/models/environment/building_d.glb",
        "app/src/main/assets/models/environment/lamp_post.glb",
        "app/src/main/assets/models/environment/tree.glb",
        "app/src/main/assets/models/environment/fence.glb"
    ], capture_output=True, text=True)

    with open(report_path, "w", encoding="utf-8") as f:
        f.write(result.stdout)
    print(f"Asset report written to: {report_path}")

if __name__ == "__main__":
    render_env()
