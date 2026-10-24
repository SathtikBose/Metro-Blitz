"""
blender/scripts/build_coin.py
Metro Blitz — Collectible Coin Asset Builder
Builds chunky gold coin:
- Diameter: 0.8 m (radius 0.4 m)
- Thickness: 0.12 m (along Y in Blender, facing +-Y)
- Geometry star emboss on front and back
- Materials: M_SunnyYellow (#FFD93D), M_Orange (#FF9F43), M_Cream (#FFF4E0)
- Budget: <= 200 triangles, <= 4 materials, 0 textures
- Pivot: (0, 0, 0) (its centre)
"""
import sys
import os
import math

script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.append(script_dir)

import bpy
import bmesh
import mb_helpers as mb

SOURCE_DIR = os.path.join(script_dir, "..", "source")
EXPORT_DIR = os.path.join(script_dir, "..", "..", "app", "src", "main", "assets", "models", "collectibles")
EVIDENCE_DIR = os.path.join(script_dir, "..", "..", "docs", "evidence", "phase-7")

os.makedirs(SOURCE_DIR, exist_ok=True)
os.makedirs(EXPORT_DIR, exist_ok=True)
os.makedirs(EVIDENCE_DIR, exist_ok=True)

def reset_scene():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.context.scene.unit_settings.system = 'METRIC'
    bpy.context.scene.unit_settings.scale_length = 1.0

def build_star(name: str, y_face: float, is_front: bool, mat: bpy.types.Material) -> bpy.types.Object:
    """Builds a 5-pointed 3D star emboss facing either -Y (front) or +Y (back)."""
    mesh = bpy.data.meshes.new(name + "_mesh")
    bm = bmesh.new()

    num_points = 5
    r_outer = 0.22
    r_inner = 0.10
    star_depth = 0.015

    y_base = y_face
    y_tip = y_face - star_depth if is_front else y_face + star_depth

    # Base center and tip center
    v_base_center = bm.verts.new((0.0, y_base, 0.0))
    v_tip_center = bm.verts.new((0.0, y_tip, 0.0))

    base_perimeter = []
    tip_perimeter = []

    for i in range(num_points * 2):
        angle = math.pi / 2.0 + i * (math.pi / num_points)
        r = r_outer if (i % 2 == 0) else r_inner
        x = r * math.cos(angle)
        z = r * math.sin(angle)
        base_perimeter.append(bm.verts.new((x, y_base, z)))
        tip_perimeter.append(bm.verts.new((x * 0.9, y_tip, z * 0.9)))

    # Faces for front cap
    n = len(base_perimeter)
    for i in range(n):
        i_next = (i + 1) % n
        if is_front:
            # Front face points in -Y
            bm.faces.new([v_tip_center, tip_perimeter[i_next], tip_perimeter[i]])
            # Side bevel quads
            bm.faces.new([tip_perimeter[i], tip_perimeter[i_next], base_perimeter[i_next], base_perimeter[i]])
        else:
            # Back face points in +Y
            bm.faces.new([v_tip_center, tip_perimeter[i], tip_perimeter[i_next]])
            bm.faces.new([tip_perimeter[i_next], tip_perimeter[i], base_perimeter[i], base_perimeter[i_next]])

    bm.to_mesh(mesh)
    bm.free()

    obj = bpy.data.objects.new(name, mesh)
    bpy.context.collection.objects.link(obj)
    if mat:
        obj.data.materials.append(mat)
    return obj

def build_coin():
    reset_scene()

    # Materials
    m_yellow = mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.5)
    m_orange = mb.make_mat("M_Orange", "#FF9F43", roughness=0.6)
    m_cream = mb.make_mat("M_Cream", "#FFF4E0", roughness=0.4)

    # 1. Main Coin Body: Cylinder radius 0.40m, thickness 0.12m along Y (faces +-Y)
    # 14 vertices on perimeter gives smooth cartoon circle with 56 triangles
    coin_obj = mb.add_cylinder(
        name="CoinBody",
        radius=0.40,
        depth=0.12,
        loc=(0.0, 0.0, 0.0),
        mat=m_yellow,
        verts=14,
        rot=(math.pi / 2.0, 0.0, 0.0) # Flat faces along Y
    )
    bpy.context.view_layer.objects.active = coin_obj
    bpy.ops.object.transform_apply(location=False, rotation=True, scale=True)

    parts = [coin_obj]

    # 2. Stars: front star at Y = -0.06m, back star at Y = +0.06m
    star_front = build_star("StarFront", y_face=-0.06, is_front=True, mat=m_cream)
    star_back = build_star("StarBack", y_face=0.06, is_front=False, mat=m_cream)
    parts.extend([star_front, star_back])

    # Join into a single mesh "coin"
    bpy.ops.object.select_all(action='DESELECT')
    for p in parts:
        p.select_set(True)
    bpy.context.view_layer.objects.active = coin_obj
    bpy.ops.object.join()

    final_coin = bpy.context.active_object
    final_coin.name = "coin"

    # Ensure transforms are applied and pivot is at (0, 0, 0)
    bpy.ops.object.transform_apply(location=True, rotation=True, scale=True)

    tris = mb.tri_count([final_coin])
    mats = len(final_coin.data.materials)

    print(f"=== Built Coin ===")
    print(f"  Triangles: {tris} (Budget: <= 200)")
    print(f"  Materials: {mats} (Budget: <= 4)")

    assert tris <= 200, f"Coin triangle count {tris} exceeds budget 200!"
    assert mats <= 4, f"Coin material count {mats} exceeds budget 4!"

    # Export GLB
    glb_path = os.path.join(EXPORT_DIR, "coin.glb")
    mb.export_glb(glb_path, [final_coin], export_animations=False)
    print(f"  Exported GLB to: {glb_path}")

    # Setup rendering for evidence screenshot
    # Camera at isometric-style view looking at front-top of the coin
    cam_data = bpy.data.cameras.new("EvidenceCamera")
    cam_data.lens = 50
    cam_obj = bpy.data.objects.new("EvidenceCamera", cam_data)
    bpy.context.collection.objects.link(cam_obj)
    cam_obj.location = (0.5, -1.2, 0.6)
    # Point at coin center
    # Euler rotation approx: X = 65 deg, Y = 0, Z = 22 deg
    cam_obj.rotation_euler = (math.radians(65), math.radians(0), math.radians(22))
    bpy.context.scene.camera = cam_obj

    # Sun light
    light_data = bpy.data.lights.new(name="SunLight", type='SUN')
    light_data.energy = 3.5
    light_obj = bpy.data.objects.new(name="SunLight", object_data=light_data)
    bpy.context.collection.objects.link(light_obj)
    light_obj.rotation_euler = (math.radians(45), math.radians(30), math.radians(-30))

    # Fill light
    fill_data = bpy.data.lights.new(name="FillLight", type='POINT')
    fill_data.energy = 50.0
    fill_obj = bpy.data.objects.new(name="FillLight", object_data=fill_data)
    bpy.context.collection.objects.link(fill_obj)
    fill_obj.location = (-0.8, -1.0, 0.2)

    # Render settings
    bpy.context.scene.render.resolution_x = 512
    bpy.context.scene.render.resolution_y = 512
    bpy.context.scene.render.image_settings.file_format = 'PNG'

    screenshot_path = os.path.join(EVIDENCE_DIR, "coin.png")
    bpy.context.scene.render.filepath = screenshot_path
    bpy.ops.render.render(write_still=True)
    print(f"  Rendered screenshot to: {screenshot_path}")

    # Save .blend
    blend_path = os.path.join(SOURCE_DIR, "coin.blend")
    bpy.ops.wm.save_as_mainfile(filepath=blend_path)
    print(f"  Saved .blend to: {blend_path}")

if __name__ == "__main__":
    build_coin()
