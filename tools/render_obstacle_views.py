"""
tools/render_obstacle_views.py
Renders evidence screenshots for Phase 5:
- Obstacles line-up on the tracks (barricade_low, overhead_beam, train_blue, train_orange)
- Perspective close-ups for barricade, overhead beam clearance, and trains
Saves images to docs/evidence/phase-5/ and generates asset_report.txt.
"""
import bpy
import os
import math
import subprocess

def render_obstacles():
    evidence_dir = os.path.abspath("docs/evidence/phase-5")
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
    cam_data = bpy.data.cameras.new("ObsCam")
    cam_data.lens = 35
    cam_obj = bpy.data.objects.new("ObsCam", cam_data)
    scene.collection.objects.link(cam_obj)
    scene.camera = cam_obj

    # Sun Light
    sun_data = bpy.data.lights.new("Sun", 'SUN')
    sun_data.energy = 3.5
    sun_obj = bpy.data.objects.new("Sun", sun_data)
    sun_obj.rotation_euler = (math.radians(50), math.radians(20), math.radians(-40))
    scene.collection.objects.link(sun_obj)

    # Track segment for grounded context
    track_glb = os.path.abspath("app/src/main/assets/models/environment/track_segment.glb")
    if os.path.exists(track_glb):
        bpy.ops.import_scene.gltf(filepath=track_glb)

    # Import obstacles
    barricade_glb = os.path.abspath("app/src/main/assets/models/obstacles/barricade_low.glb")
    beam_glb = os.path.abspath("app/src/main/assets/models/obstacles/overhead_beam.glb")
    train_blue_glb = os.path.abspath("app/src/main/assets/models/obstacles/train_blue.glb")
    train_orange_glb = os.path.abspath("app/src/main/assets/models/obstacles/train_orange.glb")

    # 1. Lineup on the 3 lanes:
    # Lane -2: Train Blue (at Y = -2m)
    # Lane 0: Barricade Low (at Y = -6m) and Overhead Beam (at Y = 0m)
    # Lane +2: Train Orange (at Y = 2m)
    obs_objs = {}
    
    if os.path.exists(train_blue_glb):
        bpy.ops.import_scene.gltf(filepath=train_blue_glb)
        obs_objs["train_blue"] = bpy.context.selected_objects[0]
        obs_objs["train_blue"].location = (-2.0, -2.0, 0.0)

    if os.path.exists(barricade_glb):
        bpy.ops.import_scene.gltf(filepath=barricade_glb)
        obs_objs["barricade"] = bpy.context.selected_objects[0]
        obs_objs["barricade"].location = (0.0, -6.0, 0.0)

    if os.path.exists(beam_glb):
        bpy.ops.import_scene.gltf(filepath=beam_glb)
        obs_objs["beam"] = bpy.context.selected_objects[0]
        obs_objs["beam"].location = (0.0, 0.0, 0.0)

    if os.path.exists(train_orange_glb):
        bpy.ops.import_scene.gltf(filepath=train_orange_glb)
        obs_objs["train_orange"] = bpy.context.selected_objects[0]
        obs_objs["train_orange"].location = (2.0, 2.0, 0.0)

    # Screenshot 1: Overview track lineup showing all 4 obstacles in perspective
    cam_obj.location = (0.0, -14.0, 5.5)
    cam_obj.rotation_euler = (math.radians(72), 0, 0)
    scene.render.filepath = os.path.join(evidence_dir, "obstacles_track_lineup.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # Screenshot 2: Front/under view of Overhead Beam showing 1.1m duck-under clearance
    cam_obj.location = (0.0, -4.5, 1.8)
    cam_obj.rotation_euler = (math.radians(82), 0, 0)
    scene.render.filepath = os.path.join(evidence_dir, "overhead_beam_clearance.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # Screenshot 3: Close-up perspective of Barricade Low
    cam_obj.location = (1.5, -9.5, 2.2)
    cam_obj.rotation_euler = (math.radians(72), 0, math.radians(20))
    scene.render.filepath = os.path.join(evidence_dir, "barricade_perspective.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # Screenshot 4: Close-up front of Train Blue and Train Orange
    cam_obj.location = (-1.2, -8.0, 2.5)
    cam_obj.rotation_euler = (math.radians(78), 0, math.radians(-12))
    scene.render.filepath = os.path.join(evidence_dir, "trains_front_perspective.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # Generate asset_report.txt
    print("Generating asset report...")
    report_path = os.path.join(evidence_dir, "asset_report.txt")
    cmd = [
        "python",
        os.path.abspath("tools/validate_glb.py"),
        barricade_glb,
        beam_glb,
        train_blue_glb,
        train_orange_glb
    ]
    res = subprocess.run(cmd, capture_output=True, text=True)
    with open(report_path, "w") as f:
        f.write("=== PHASE 5 OBSTACLE ASSET VALIDATION REPORT ===\n")
        f.write(res.stdout)
        if res.stderr:
            f.write("\nSTDERR:\n" + res.stderr)
    print(f"Wrote asset report to: {report_path}")

if __name__ == "__main__":
    render_obstacles()
