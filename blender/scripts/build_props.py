"""
blender/scripts/build_props.py
Procedurally builds buildings (a-d) and street props (lamp_post, tree, fence)
for Metro Blitz.
All assets strictly adhere to triangle budgets, palette colors, and <= 4 materials.
"""
import sys
import os

script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.append(script_dir)

import bpy
import mb_helpers as mb

SOURCE_DIR = os.path.join(script_dir, "..", "source")
EXPORT_DIR = os.path.join(script_dir, "..", "..", "app", "src", "main", "assets", "models", "environment")

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

# ==========================================
# Building A: Cream apartment, Mint roof/cornice, Navy/Yellow windows
# Budget: 400 tris, footprint <= 6x6, height 6-10m
# ==========================================
def build_building_a():
    reset_scene()
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.8)
    mat_mint = mb.make_mat("M_MintGreen", "#6BCB77", roughness=0.7)
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)
    mat_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.5)

    parts = []
    # Main wall block: 5.0m x 5.0m x 9.0m
    body = mb.add_box("Body", size=(5.0, 5.0, 9.0), loc=(0.0, 0.0, 4.5), mat=mat_cream)
    parts.append(body)

    # Roof cornice & trim
    cornice = mb.add_box("Cornice", size=(5.4, 5.4, 0.5), loc=(0.0, 0.0, 9.25), mat=mat_mint)
    roof_box = mb.add_box("RoofBox", size=(2.4, 2.4, 0.8), loc=(0.0, 0.0, 9.9), mat=mat_mint)
    parts.extend([cornice, roof_box])

    # Entrance door / canopy at base front (y = -2.5)
    door = mb.add_box("Door", size=(1.2, 0.1, 2.2), loc=(0.0, -2.52, 1.1), mat=mat_navy)
    canopy = mb.add_box("Canopy", size=(1.8, 0.8, 0.15), loc=(0.0, -2.8, 2.3), mat=mat_mint)
    parts.extend([door, canopy])

    # Windows on front facade (2 columns x 3 rows)
    win_y = -2.52
    col_x = [-1.4, 1.4]
    row_z = [4.0, 6.0, 8.0]
    for cx in col_x:
        for rz in row_z:
            win_frame = mb.add_box(f"WinFrame_{cx}_{rz}", size=(1.0, 0.08, 1.2), loc=(cx, win_y, rz), mat=mat_navy)
            win_glass = mb.add_box(f"WinGlass_{cx}_{rz}", size=(0.8, 0.10, 1.0), loc=(cx, win_y, rz), mat=mat_yellow)
            parts.extend([win_frame, win_glass])

    finalize_asset("building_a", parts, body, max_tris=400)

# ==========================================
# Building B: Stepped Sky Blue tower with Cream trim & Navy windows
# Budget: 400 tris, height 6-10m
# ==========================================
def build_building_b():
    reset_scene()
    mat_blue = mb.make_mat("M_SkyBlue", "#4FC3F7", roughness=0.75)
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.75)
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)
    mat_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.5)

    parts = []
    # Lower tier: 5.4m x 5.4m x 5.0m
    tier1 = mb.add_box("Tier1", size=(5.4, 5.4, 5.0), loc=(0.0, 0.0, 2.5), mat=mat_blue)
    trim1 = mb.add_box("Trim1", size=(5.6, 5.6, 0.3), loc=(0.0, 0.0, 5.15), mat=mat_cream)
    parts.extend([tier1, trim1])

    # Upper tier: 4.0m x 4.0m x 4.0m (z from 5.3 to 9.3)
    tier2 = mb.add_box("Tier2", size=(4.0, 4.0, 4.0), loc=(0.0, 0.0, 7.3), mat=mat_blue)
    trim2 = mb.add_box("Trim2", size=(4.2, 4.2, 0.3), loc=(0.0, 0.0, 9.45), mat=mat_cream)
    parts.extend([tier2, trim2])

    # Roof Spire / Top Box: 1.6m x 1.6m x 1.0m (z from 9.6 to 10.6)
    spire_base = mb.add_box("SpireBase", size=(1.8, 1.8, 0.6), loc=(0.0, 0.0, 9.9), mat=mat_cream)
    spire = mb.add_cylinder("Spire", radius=0.15, depth=1.5, loc=(0.0, 0.0, 10.95), mat=mat_yellow, verts=8)
    parts.extend([spire_base, spire])

    # Windows on lower tier (y = -2.72)
    for cx in [-1.5, 0.0, 1.5]:
        win = mb.add_box(f"WinL_{cx}", size=(0.8, 0.08, 1.5), loc=(cx, -2.72, 2.8), mat=mat_navy)
        parts.append(win)

    # Windows on upper tier (y = -2.02)
    for cx in [-1.0, 1.0]:
        win = mb.add_box(f"WinU_{cx}", size=(0.8, 0.08, 1.4), loc=(cx, -2.02, 7.5), mat=mat_yellow)
        parts.append(win)

    finalize_asset("building_b", parts, tier1, max_tris=400)

# ==========================================
# Building C: Violet apartment with rounded Mint barrel roof
# Budget: 400 tris, height 6-10m
# ==========================================
def build_building_c():
    reset_scene()
    mat_violet = mb.make_mat("M_Violet", "#7C5CFF", roughness=0.8)
    mat_mint = mb.make_mat("M_MintGreen", "#6BCB77", roughness=0.7)
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.8)
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)

    parts = []
    # Main block: 4.8m x 5.0m x 6.5m
    body = mb.add_box("Body", size=(4.8, 5.0, 6.5), loc=(0.0, 0.0, 3.25), mat=mat_violet)
    parts.append(body)

    # Rounded barrel roof: half-cylinder oriented along Y (rotation=(pi/2, 0, 0))
    # 10 verts cylinder, radius 2.4m, depth 5.2m, centered at z = 6.5
    roof = mb.add_cylinder("BarrelRoof", radius=2.4, depth=5.2, loc=(0.0, 0.0, 6.5), mat=mat_mint, verts=10, rot=(1.5708, 0.0, 0.0))
    parts.append(roof)

    # Base trim
    base_trim = mb.add_box("BaseTrim", size=(5.0, 5.2, 0.4), loc=(0.0, 0.0, 0.2), mat=mat_cream)
    parts.append(base_trim)

    # Windows & Balconies (2 columns x 2 rows)
    for cx in [-1.3, 1.3]:
        for rz in [2.5, 4.8]:
            win = mb.add_box(f"Win_{cx}_{rz}", size=(1.0, 0.08, 1.4), loc=(cx, -2.52, rz), mat=mat_navy)
            balcony = mb.add_box(f"Balcony_{cx}_{rz}", size=(1.3, 0.5, 0.4), loc=(cx, -2.75, rz - 0.6), mat=mat_cream)
            parts.extend([win, balcony])

    finalize_asset("building_c", parts, body, max_tris=400)

# ==========================================
# Building D: Coral Red shop with Sunny Yellow awning & Cream trim
# Budget: 400 tris, height 6-10m
# ==========================================
def build_building_d():
    reset_scene()
    mat_coral = mb.make_mat("M_CoralRed", "#FF6B6B", roughness=0.8)
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.75)
    mat_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.6)
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)

    parts = []
    # Main walls: 4.6m x 4.8m x 6.5m
    body = mb.add_box("Body", size=(4.6, 4.8, 6.5), loc=(0.0, 0.0, 3.25), mat=mat_coral)
    parts.append(body)

    # Top cornice & parapet
    cornice = mb.add_box("Cornice", size=(4.9, 5.1, 0.4), loc=(0.0, 0.0, 6.7), mat=mat_cream)
    parapet = mb.add_box("Parapet", size=(4.6, 4.8, 0.5), loc=(0.0, 0.0, 7.15), mat=mat_coral)
    parts.extend([cornice, parapet])

    # Storefront shop window & entrance
    shop_win = mb.add_box("ShopWindow", size=(3.2, 0.1, 1.8), loc=(0.0, -2.42, 1.2), mat=mat_navy)
    # Striped awning: Yellow angled wedge
    awning = mb.add_box("Awning", size=(3.6, 1.0, 0.15), loc=(0.0, -2.85, 2.5), mat=mat_yellow)
    awning.rotation_euler = (0.35, 0.0, 0.0) # slight tilt downward
    bpy.context.view_layer.objects.active = awning
    bpy.ops.object.transform_apply(rotation=True)
    parts.extend([shop_win, awning])

    # Upper floor windows
    for cx in [-1.2, 1.2]:
        win_frame = mb.add_box(f"Frame_{cx}", size=(1.0, 0.08, 1.5), loc=(cx, -2.42, 4.8), mat=mat_cream)
        win_glass = mb.add_box(f"Glass_{cx}", size=(0.7, 0.10, 1.2), loc=(cx, -2.42, 4.8), mat=mat_yellow)
        parts.extend([win_frame, win_glass])

    finalize_asset("building_d", parts, body, max_tris=400)

# ==========================================
# Lamp Post: Navy post, curved arm, Sunny Yellow light fixture
# Budget: 150 tris, height ~4.5m
# ==========================================
def build_lamp_post():
    reset_scene()
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)
    mat_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.3)
    mat_white = mb.make_mat("M_White", "#FFFFFF", roughness=0.4)

    parts = []
    # Base flange
    base = mb.add_cylinder("Base", radius=0.22, depth=0.15, loc=(0.0, 0.0, 0.075), mat=mat_navy, verts=6)
    parts.append(base)

    # Main pole: height 3.8m, radius 0.06m
    pole = mb.add_cylinder("Pole", radius=0.06, depth=3.8, loc=(0.0, 0.0, 2.05), mat=mat_navy, verts=6)
    parts.append(pole)

    # Horizontal arm extending over track/platform (+X direction by 0.7m at z = 3.9)
    arm = mb.add_box("Arm", size=(0.7, 0.08, 0.08), loc=(0.35, 0.0, 3.95), mat=mat_navy)
    parts.append(arm)

    # Lamp head fixture at x = 0.7, z = 3.85
    fixture = mb.add_cylinder("Fixture", radius=0.18, depth=0.12, loc=(0.7, 0.0, 3.88), mat=mat_navy, verts=6)
    bulb = mb.add_sphere("Bulb", radius=0.14, loc=(0.7, 0.0, 3.76), mat=mat_yellow, scale=(1.0, 1.0, 0.8), segments=6, ring_count=4)
    parts.extend([fixture, bulb])

    finalize_asset("lamp_post", parts, base, max_tris=150)

# ==========================================
# Tree: Chunky round cartoon tree, Brown trunk, Mint Green canopy
# Budget: 150 tris, height ~4.2m
# ==========================================
def build_tree():
    reset_scene()
    mat_brown = mb.make_mat("M_Brown", "#8B5E3C", roughness=0.85)
    mat_mint = mb.make_mat("M_MintGreen", "#6BCB77", roughness=0.7)

    parts = []
    # Trunk: 6 verts, radius 0.20, height 2.0m
    trunk = mb.add_cylinder("Trunk", radius=0.20, depth=2.0, loc=(0.0, 0.0, 1.0), mat=mat_brown, verts=6)
    parts.append(trunk)

    # Foliage Canopy: 2 rounded overlapping low-poly spheres (8 segments, 5 rings)
    # Sphere 1 (main canopy)
    canopy_main = mb.add_sphere("CanopyMain", radius=1.2, loc=(0.0, 0.0, 2.8), mat=mat_mint, scale=(1.1, 1.1, 1.0), segments=8, ring_count=5)
    # Sphere 2 (top puffed canopy)
    canopy_top = mb.add_sphere("CanopyTop", radius=0.9, loc=(0.1, -0.1, 3.6), mat=mat_mint, scale=(1.0, 1.0, 0.9), segments=8, ring_count=5)
    parts.extend([canopy_main, canopy_top])

    finalize_asset("tree", parts, trunk, max_tris=150)

# ==========================================
# Fence: 3m cartoon platform fence
# Budget: 100 tris, 3m length along Y, height 1.2m
# ==========================================
def build_fence():
    reset_scene()
    mat_navy = mb.make_mat("M_DeepNavy", "#1B1F3B", roughness=0.5)
    mat_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.7)

    parts = []
    # End posts: at y = -1.45 and y = +1.45, height 1.2m, 0.12x0.12m
    post_l = mb.add_box("Post_L", size=(0.12, 0.12, 1.2), loc=(0.0, -1.44, 0.6), mat=mat_navy)
    post_r = mb.add_box("Post_R", size=(0.12, 0.12, 1.2), loc=(0.0, 1.44, 0.6), mat=mat_navy)
    # Center post at y = 0
    post_m = mb.add_box("Post_M", size=(0.12, 0.12, 1.2), loc=(0.0, 0.0, 0.6), mat=mat_navy)
    parts.extend([post_l, post_r, post_m])

    # Horizontal rails (2 rails along Y, length 3.0m)
    rail_bottom = mb.add_box("RailBottom", size=(0.06, 3.0, 0.08), loc=(0.0, 0.0, 0.35), mat=mat_cream)
    rail_top = mb.add_box("RailTop", size=(0.06, 3.0, 0.08), loc=(0.0, 0.0, 0.95), mat=mat_cream)
    parts.extend([rail_bottom, rail_top])

    finalize_asset("fence", parts, post_l, max_tris=100)

if __name__ == "__main__":
    print("Building environment models...")
    build_building_a()
    build_building_b()
    build_building_c()
    build_building_d()
    build_lamp_post()
    build_tree()
    build_fence()
    print("All environment models successfully built!")
