"""
blender/scripts/build_track.py
Procedurally builds the 30 m 3-lane track segment for Metro Blitz.
Budget: <= 2500 triangles, <= 4 materials, 0 textures.
"""
import sys
import os

# Add scripts directory to sys.path so mb_helpers can be imported
script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.append(script_dir)

import bpy
import mb_helpers as mb

def build_track_segment():
    # Reset scene
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.context.scene.unit_settings.system = 'METRIC'
    bpy.context.scene.unit_settings.scale_length = 1.0

    # 4 Materials Palette
    mat_slate = mb.make_mat("M_Slate", "#8D99AE", roughness=0.85)
    mat_deep_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)
    mat_brown = mb.make_mat("M_Brown", "#8B5E3C", roughness=0.9)
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.7)

    parts = []

    # 1. Ballast bed: width 6.8 m, length 30 m, thickness 0.15 m
    # Extends in Y from -15.0 to +15.0
    ballast = mb.add_box(
        "Ballast",
        size=(6.8, 30.0, 0.15),
        loc=(0.0, 0.0, -0.075),
        mat=mat_slate
    )
    parts.append(ballast)

    # 2. Side platforms: Cream with Deep Navy curb
    # Left platform: x = -3.9 (x in [-4.4, -3.4]), width 1.0, length 30.0, height 0.35 (z from -0.15 to 0.20)
    plat_l = mb.add_box("Platform_L", size=(1.0, 30.0, 0.35), loc=(-3.9, 0.0, 0.025), mat=mat_cream)
    parts.append(plat_l)

    # Right platform: x = +3.9 (x in [3.4, 4.4]), width 1.0, length 30.0, height 0.35
    plat_r = mb.add_box("Platform_R", size=(1.0, 30.0, 0.35), loc=(3.9, 0.0, 0.025), mat=mat_cream)
    parts.append(plat_r)

    # Platform curbs (facing tracks at x = ±3.42)
    curb_l = mb.add_box("Curb_L", size=(0.08, 30.0, 0.04), loc=(-3.41, 0.0, 0.21), mat=mat_deep_navy)
    curb_r = mb.add_box("Curb_R", size=(0.08, 30.0, 0.04), loc=(3.41, 0.0, 0.21), mat=mat_deep_navy)
    parts.extend([curb_l, curb_r])

    # 3. Rails: 6 rails (3 pairs) across lanes x = -2.0, 0.0, 2.0
    # Spacing between rails in each pair = 1.1 m (+/- 0.55 from lane center)
    # Rail cross section: 0.12 m wide, 0.12 m high, running full 30 m length
    rail_x_offsets = [-2.55, -1.45, -0.55, 0.55, 1.45, 2.55]
    for idx, rx in enumerate(rail_x_offsets):
        rail = mb.add_box(
            f"Rail_{idx}",
            size=(0.12, 30.0, 0.12),
            loc=(rx, 0.0, 0.16),
            mat=mat_deep_navy
        )
        parts.append(rail)

    # 4. Sleepers: Brown sleepers (1.6 m x 0.3 m x 0.1 m) every 1.0 m along Y
    # In 30 m length: 30 positions at y = -14.5 + i * 1.0 for i in 0..29
    # This leaves 0.5 m margin at both ends (-15 and +15), ensuring seamless 1.0 m tiling!
    lanes = [-2.0, 0.0, 2.0]
    for lane_idx, lx in enumerate(lanes):
        for i in range(30):
            y_pos = -14.5 + (i * 1.0)
            sleeper = mb.add_box(
                f"Sleeper_{lane_idx}_{i}",
                size=(1.6, 0.3, 0.1),
                loc=(lx, y_pos, 0.05),
                mat=mat_brown
            )
            parts.append(sleeper)

    # Join all parts into one unified mesh object 'TrackSegment'
    bpy.ops.object.select_all(action='DESELECT')
    for p in parts:
        p.select_set(True)
    bpy.context.view_layer.objects.active = ballast
    bpy.ops.object.join()
    track_obj = bpy.context.active_object
    track_obj.name = "TrackSegment"

    # Ensure transforms applied
    bpy.ops.object.transform_apply(location=False, rotation=True, scale=True)

    total_tris = mb.tri_count([track_obj])
    print(f"=== Track Segment Built ===")
    print(f"  Total Triangles: {total_tris} (Budget: 2500)")
    print(f"  Materials: {len(track_obj.data.materials)} (Budget: <= 4)")

    # Save .blend
    source_dir = os.path.join(script_dir, "..", "source")
    os.makedirs(source_dir, exist_ok=True)
    blend_path = os.path.join(source_dir, "track_segment.blend")
    bpy.ops.wm.save_as_mainfile(filepath=blend_path)
    print(f"  Saved .blend: {blend_path}")

    # Export .glb
    export_dir = os.path.join(script_dir, "..", "..", "app", "src", "main", "assets", "models", "environment")
    os.makedirs(export_dir, exist_ok=True)
    glb_path = os.path.join(export_dir, "track_segment.glb")
    mb.export_glb(glb_path, [track_obj], export_animations=False)
    print(f"  Exported .glb: {glb_path}")

if __name__ == "__main__":
    build_track_segment()
