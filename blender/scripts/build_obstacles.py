"""
blender/scripts/build_obstacles.py
Procedurally builds obstacles for Metro Blitz:
- barricade_low.glb (Jump over or switch lanes, 1.8x0.5x0.9m, <=300 tris, <=4 mats)
- overhead_beam.glb (Slide under or switch lanes, 1.8x0.5x2.4m, clearance 1.1m, <=400 tris, <=4 mats)
- train_blue.glb (Switch lanes only, 1.8x10.0x3.0m, front faces -Y, <=1500 tris, <=4 mats)
- train_orange.glb (Switch lanes only, orange variant of train, <=1500 tris, <=4 mats)

All assets adhere strictly to triangle budgets, palette colors, and <= 4 materials.
"""
import sys
import os
import math

script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.append(script_dir)

import bpy
import mb_helpers as mb

SOURCE_DIR = os.path.join(script_dir, "..", "source")
EXPORT_DIR = os.path.join(script_dir, "..", "..", "app", "src", "main", "assets", "models", "obstacles")

os.makedirs(SOURCE_DIR, exist_ok=True)
os.makedirs(EXPORT_DIR, exist_ok=True)

def reset_scene():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.context.scene.unit_settings.system = 'METRIC'
    bpy.context.scene.unit_settings.scale_length = 1.0

def finalize_asset(name: str, parts: list, primary_obj: bpy.types.Object, max_tris: int):
    bpy.ops.object.select_all(action='DESELECT')
    for p in parts:
        p.select_set(True)
    bpy.context.view_layer.objects.active = primary_obj
    bpy.ops.object.join()
    obj = bpy.context.active_object
    obj.name = name

    # Pivot at base center (0, 0, 0)
    bpy.ops.object.transform_apply(location=False, rotation=True, scale=True)

    tris = mb.tri_count([obj])
    mats = len(obj.data.materials)
    print(f"=== Built {name} ===")
    print(f"  Triangles: {tris} (Budget: {max_tris})")
    print(f"  Materials: {mats} (Budget: <= 4)")

    blend_path = os.path.join(SOURCE_DIR, f"{name}.blend")
    bpy.ops.wm.save_as_mainfile(filepath=blend_path)

    glb_path = os.path.join(EXPORT_DIR, f"{name}.glb")
    mb.export_glb(glb_path, [obj], export_animations=False)
    print(f"  Exported: {glb_path}\n")


# ==============================================================================
# 1. Barricade Low
# Dimensions: X=1.8m, Y=0.5m, Z=0.9m
# Budget: <= 300 tris, <= 4 materials
# Materials: M_SunnyYellow (#FFD93D), M_CoralRed (#FF6B6B), M_DeepNavy (#1B1F3B), M_White (#FFFFFF)
# ==============================================================================
def build_barricade_low():
    reset_scene()
    mat_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.7)
    mat_red = mb.make_mat("M_CoralRed", "#FF6B6B", roughness=0.7)
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.8)
    mat_white = mb.make_mat("M_White", "#FFFFFF", roughness=0.6)

    parts = []

    # 1. Base feet (T-shaped feet for ground stability, Y depth 0.48m)
    # Left foot
    parts.append(mb.add_box("Foot_L", (0.12, 0.48, 0.08), (-0.68, 0, 0.04), mat_navy))
    # Right foot
    parts.append(mb.add_box("Foot_R", (0.12, 0.48, 0.08), (0.68, 0, 0.04), mat_navy))

    # 2. Upright support posts (Z from 0.08 to 0.88m, height 0.80m)
    parts.append(mb.add_box("Post_L", (0.08, 0.08, 0.80), (-0.68, 0, 0.48), mat_navy))
    parts.append(mb.add_box("Post_R", (0.08, 0.08, 0.80), (0.68, 0, 0.48), mat_navy))

    # 3. Feet angle braces
    parts.append(mb.add_box("Brace_LF", (0.06, 0.16, 0.12), (-0.68, 0.12, 0.12), mat_navy))
    parts.append(mb.add_box("Brace_LB", (0.06, 0.16, 0.12), (-0.68, -0.12, 0.12), mat_navy))
    parts.append(mb.add_box("Brace_RF", (0.06, 0.16, 0.12), (0.68, 0.12, 0.12), mat_navy))
    parts.append(mb.add_box("Brace_RB", (0.06, 0.16, 0.12), (0.68, -0.12, 0.12), mat_navy))

    # 4. Top Board: Z from 0.58 to 0.86m (height 0.28m, depth 0.06m, total width 1.80m)
    # Divided into 5 alternating yellow and red stripe blocks across X (-0.90 to +0.90)
    num_stripes = 5
    total_w = 1.80
    stripe_w = total_w / num_stripes # 0.36m
    board_z_top = 0.72
    board_h_top = 0.26
    board_d = 0.06

    for i in range(num_stripes):
        x = -0.90 + stripe_w * (i + 0.5)
        mat = mat_yellow if i % 2 == 0 else mat_red
        parts.append(mb.add_box(f"TopStripe_{i}", (stripe_w - 0.002, board_d, board_h_top), (x, 0, board_z_top), mat))

    # 5. Bottom Board: Z from 0.20 to 0.42m (height 0.22m, depth 0.06m, total width 1.80m)
    board_z_bot = 0.31
    board_h_bot = 0.20
    for i in range(num_stripes):
        x = -0.90 + stripe_w * (i + 0.5)
        mat = mat_red if i % 2 == 0 else mat_yellow  # Invert order for nice chevron contrast
        parts.append(mb.add_box(f"BotStripe_{i}", (stripe_w - 0.002, board_d, board_h_bot), (x, 0, board_z_bot), mat))

    # 6. White end caps / reflectors on both ends
    parts.append(mb.add_box("Reflector_LT", (0.04, 0.08, 0.16), (-0.89, 0, board_z_top), mat_white))
    parts.append(mb.add_box("Reflector_RT", (0.04, 0.08, 0.16), (0.89, 0, board_z_top), mat_white))
    parts.append(mb.add_box("Reflector_LB", (0.04, 0.08, 0.12), (-0.89, 0, board_z_bot), mat_white))
    parts.append(mb.add_box("Reflector_RB", (0.04, 0.08, 0.12), (0.89, 0, board_z_bot), mat_white))

    primary = parts[0]
    finalize_asset("barricade_low", parts, primary, max_tris=300)


# ==============================================================================
# 2. Overhead Beam (Gantry)
# Dimensions: X=1.8m, Y=0.5m, Z=2.4m
# Clearance: Z from 0.0 to 1.1m is open air. Beam spans Z 1.1 to 1.6m.
# Budget: <= 400 tris, <= 4 materials
# Materials: M_DeepNavy (#1B1F3B), M_SunnyYellow (#FFD93D), M_CoralRed (#FF6B6B), M_White (#FFFFFF)
# ==============================================================================
def build_overhead_beam():
    reset_scene()
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.8)
    mat_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.7)
    mat_red = mb.make_mat("M_CoralRed", "#FF6B6B", roughness=0.7)
    mat_white = mb.make_mat("M_White", "#FFFFFF", roughness=0.6)

    parts = []

    # 1. Base anchor plates at ground
    parts.append(mb.add_box("BasePlate_L", (0.24, 0.44, 0.08), (-0.80, 0, 0.04), mat_navy))
    parts.append(mb.add_box("BasePlate_R", (0.24, 0.44, 0.08), (0.80, 0, 0.04), mat_navy))

    # 2. Vertical Gantry Posts: X at -0.80 and +0.80, extending up to Z=2.38m
    # Height 2.30m, thickness 0.14m
    parts.append(mb.add_box("Post_L", (0.14, 0.14, 2.30), (-0.80, 0, 1.23), mat_navy))
    parts.append(mb.add_box("Post_R", (0.14, 0.14, 2.30), (0.80, 0, 1.23), mat_navy))

    # 3. Top Cross-beam (Header truss at top)
    parts.append(mb.add_box("TopHeader", (1.78, 0.14, 0.14), (0, 0, 2.32), mat_navy))

    # 4. Diagonal corner gussets connecting posts to top beam
    parts.append(mb.add_box("Gusset_L", (0.18, 0.08, 0.18), (-0.68, 0, 2.18), mat_navy))
    parts.append(mb.add_box("Gusset_R", (0.18, 0.08, 0.18), (0.68, 0, 2.18), mat_navy))

    # 5. Clearance Warning Beam: spans Z = 1.10m to 1.58m (center Z=1.34m, height 0.48m, depth 0.16m, width 1.76m)
    # Duck-under-able: clear height under this beam is 1.10m! (Blitz sliding height is 0.8m)
    num_stripes = 6
    total_w = 1.68
    stripe_w = total_w / num_stripes # 0.28m
    beam_z = 1.34
    beam_h = 0.46
    beam_d = 0.16

    for i in range(num_stripes):
        x = -total_w / 2.0 + stripe_w * (i + 0.5)
        mat = mat_yellow if i % 2 == 0 else mat_navy
        parts.append(mb.add_box(f"BeamStripe_{i}", (stripe_w - 0.002, beam_d, beam_h), (x, 0, beam_z), mat))

    # 6. Overhead Clearance Warning Sign & Downward Arrows
    # Caution sign plate above the warning beam (Z from 1.62 to 1.96m)
    parts.append(mb.add_box("SignPlate", (0.80, 0.06, 0.32), (0, 0.08, 1.80), mat_red))
    # White caution symbol / arrow indicator
    parts.append(mb.add_box("SignDecal", (0.60, 0.08, 0.18), (0, 0.09, 1.80), mat_white))
    # Downward clearance warning pointers
    parts.append(mb.add_box("Arrow_L", (0.10, 0.08, 0.16), (-0.45, 0.09, 1.60), mat_yellow))
    parts.append(mb.add_box("Arrow_R", (0.10, 0.08, 0.16), (0.45, 0.09, 1.60), mat_yellow))

    primary = parts[0]
    finalize_asset("overhead_beam", parts, primary, max_tris=400)


# ==============================================================================
# 3. Train Blue & Train Orange
# Dimensions: X=1.8m, Y=10.0m, Z=3.0m
# Front faces -Y in Blender! Pivot at base centre (0, 0, 0).
# Length: Y from -5.0 to +5.0. Front nose is at Y = -5.0m.
# Budget: <= 1500 tris, <= 4 materials
# ==============================================================================
def build_train(variant: str):
    reset_scene()
    is_blue = (variant == "blue")
    name = "train_blue" if is_blue else "train_orange"

    if is_blue:
        mat_primary = mb.make_mat("M_SkyBlue", "#4FC3F7", roughness=0.6)
    else:
        mat_primary = mb.make_mat("M_Orange", "#FF9F43", roughness=0.6)

    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.8)
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.6)
    mat_white = mb.make_mat("M_White", "#FFFFFF", roughness=0.5)

    parts = []

    # 1. Undercarriage / Chassis base
    # Low chassis from Z=0.10 to 0.48m (height 0.38m, width 1.64m, length 9.60m, centered Y=0.08)
    parts.append(mb.add_box("Chassis", (1.64, 9.60, 0.34), (0, 0.10, 0.28), mat_navy))

    # Wheel bogies (4 pairs of wheels under the chassis)
    bogie_y_positions = [-3.8, -2.4, 2.4, 3.8]
    for i, by in enumerate(bogie_y_positions):
        # Left wheel
        parts.append(mb.add_box(f"Wheel_L_{i}", (0.14, 0.50, 0.28), (-0.72, by, 0.18), mat_navy))
        # Right wheel
        parts.append(mb.add_box(f"Wheel_R_{i}", (0.14, 0.50, 0.28), (0.72, by, 0.18), mat_navy))

    # Front cowcatcher / plow (slanted wedge at front nose base, Y = -4.92, Z=0.22)
    parts.append(mb.add_box("Plow", (1.70, 0.24, 0.26), (0, -4.86, 0.22), mat_navy))

    # 2. Main Train Body Cabin
    # Width 1.76m, height 2.20m (Z from 0.46 to 2.66m), length 8.80m (Y from -4.0 to +4.8)
    parts.append(mb.add_box("MainCabin", (1.76, 8.80, 2.18), (0, 0.40, 1.55), mat_primary))

    # 3. Aerodynamic Rounded Front Nose
    # Front section spans from Y = -4.0 to Y = -5.0m
    # Lower front wedge (Z 0.46 to 1.50)
    parts.append(mb.add_box("FrontLowerNose", (1.74, 0.98, 1.02), (0, -4.48, 0.98), mat_primary))
    # Slanted front nose bumper
    parts.append(mb.add_box("FrontBumper", (1.76, 0.20, 0.40), (0, -4.92, 0.65), mat_navy))

    # Upper front cab / cockpit (Z 1.50 to 2.66, slightly tapered/set back)
    parts.append(mb.add_box("FrontUpperCab", (1.72, 0.88, 1.14), (0, -4.42, 2.06), mat_primary))

    # 4. Front Windshield (cockpit glass at front)
    # Large cartoon wrap-around windshield facing -Y
    parts.append(mb.add_box("Windshield_Front", (1.42, 0.08, 0.68), (0, -4.86, 2.08), mat_navy))
    # Windshield glare/reflection line
    parts.append(mb.add_box("Windshield_Reflection", (0.35, 0.09, 0.42), (-0.32, -4.87, 2.12), mat_white))

    # 5. Front Headlights (Chunky round/square headlights in M_Cream)
    parts.append(mb.add_box("Headlight_L", (0.28, 0.10, 0.28), (-0.56, -4.94, 1.05), mat_cream))
    parts.append(mb.add_box("Headlight_R", (0.28, 0.10, 0.28), (0.56, -4.94, 1.05), mat_cream))
    # Headlight inner bright glow
    parts.append(mb.add_box("Glow_L", (0.16, 0.12, 0.16), (-0.56, -4.96, 1.05), mat_white))
    parts.append(mb.add_box("Glow_R", (0.16, 0.12, 0.16), (0.56, -4.96, 1.05), mat_white))

    # Center front badge / logo
    parts.append(mb.add_box("FrontBadge", (0.34, 0.06, 0.34), (0, -4.92, 1.35), mat_cream))

    # 6. Roof Design: Z from 2.64 to 2.98m (stays strictly <= 3.0m)
    # Curved roof cap
    parts.append(mb.add_box("RoofCap", (1.64, 9.40, 0.24), (0, 0.10, 2.76), mat_primary))
    # Roof AC Units (3 pods on roof)
    for i, ry in enumerate([-2.2, 0.5, 3.2]):
        parts.append(mb.add_box(f"RoofAC_{i}", (1.10, 1.40, 0.18), (0, ry, 2.92), mat_navy))
        parts.append(mb.add_box(f"ACVent_{i}", (0.80, 1.10, 0.06), (0, ry, 2.98), mat_white))

    # 7. Side Exterior Stripe (Signature metro stripe along sides)
    # Left stripe
    parts.append(mb.add_box("Stripe_L", (0.04, 8.60, 0.22), (-0.89, 0.45, 1.25), mat_white))
    # Right stripe
    parts.append(mb.add_box("Stripe_R", (0.04, 8.60, 0.22), (0.89, 0.45, 1.25), mat_white))

    # 8. Side Windows & Doors
    # 5 large passenger windows on each side in M_Navy
    window_y_positions = [-2.8, -1.4, 0.0, 1.4, 2.8]
    for i, wy in enumerate(window_y_positions):
        # Left windows
        parts.append(mb.add_box(f"Win_L_{i}", (0.05, 0.90, 0.60), (-0.89, wy, 1.90), mat_navy))
        parts.append(mb.add_box(f"WinGlow_L_{i}", (0.06, 0.25, 0.40), (-0.89, wy - 0.20, 1.92), mat_cream))
        # Right windows
        parts.append(mb.add_box(f"Win_R_{i}", (0.05, 0.90, 0.60), (0.89, wy, 1.90), mat_navy))
        parts.append(mb.add_box(f"WinGlow_R_{i}", (0.06, 0.25, 0.40), (0.89, wy - 0.20, 1.92), mat_cream))

    # Rear door / back panel
    parts.append(mb.add_box("RearWindow", (1.00, 0.06, 0.60), (0, 4.81, 1.90), mat_navy))
    parts.append(mb.add_box("RearLight_L", (0.20, 0.08, 0.18), (-0.60, 4.81, 0.85), mat_navy))
    parts.append(mb.add_box("RearLight_R", (0.20, 0.08, 0.18), (0.60, 4.81, 0.85), mat_navy))

    primary = parts[0]
    finalize_asset(name, parts, primary, max_tris=1500)


def main():
    print("=== BUILDING METRO BLITZ OBSTACLES ===")
    build_barricade_low()
    build_overhead_beam()
    build_train("blue")
    build_train("orange")
    print("=== ALL OBSTACLES BUILT SUCCESSFULLY ===")

if __name__ == "__main__":
    main()
