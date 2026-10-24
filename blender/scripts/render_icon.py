"""
blender/scripts/render_icon.py
Renders the official 1024x1024 app icon for Metro Blitz from Blitz's character model:
- Blitz's face with yellow cap, big friendly eyes, smile, and coral hoodie
- Framed on a vibrant Sky Blue circle (#4FC3F7)
- Exports icon_render.png (1024x1024) to docs/evidence/phase-8/ and app/src/main/assets/ui/
- Exports transparent icon_foreground.png for Android adaptive launcher icon
"""
import os
import sys
import math
import shutil

script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.append(script_dir)

import bpy
import mb_helpers as mb

SOURCE_BLEND = os.path.join(script_dir, "..", "source", "blitz.blend")
EVIDENCE_DIR = os.path.join(script_dir, "..", "..", "docs", "evidence", "phase-8")
ASSET_DIR = os.path.join(script_dir, "..", "..", "app", "src", "main", "assets", "ui")

os.makedirs(EVIDENCE_DIR, exist_ok=True)
os.makedirs(ASSET_DIR, exist_ok=True)

def render_icon():
    bpy.ops.wm.open_mainfile(filepath=SOURCE_BLEND)

    # 1. Background Sky Blue circular disc behind Blitz
    m_sky = mb.make_mat("M_SkyBlue", "#4FC3F7", roughness=0.5)
    disc = mb.add_cylinder(
        name="BgCircle",
        radius=0.90,
        depth=0.04,
        loc=(0.0, 0.40, 1.40),
        mat=m_sky,
        verts=36,
        rot=(math.pi / 2.0, 0.0, 0.0)
    )

    # 2. Camera looking closely at Blitz's face (centered at Z = 1.42m)
    cam_data = bpy.data.cameras.new("IconCamera")
    cam_data.lens = 68 # 68mm lens for flattering portrait framing
    cam_obj = bpy.data.objects.new("IconCamera", cam_data)
    bpy.context.collection.objects.link(cam_obj)
    cam_obj.location = (0.0, -1.30, 1.42)
    cam_obj.rotation_euler = (math.radians(90.0), 0.0, 0.0)
    bpy.context.scene.camera = cam_obj

    # 3. Sun and fill lighting
    sun_data = bpy.data.lights.new(name="SunLight", type='SUN')
    sun_data.energy = 3.6
    sun_obj = bpy.data.objects.new(name="SunLight", object_data=sun_data)
    bpy.context.collection.objects.link(sun_obj)
    sun_obj.rotation_euler = (math.radians(45.0), math.radians(15.0), math.radians(-30.0))

    fill_data = bpy.data.lights.new(name="FillLight", type='POINT')
    fill_data.energy = 45.0
    fill_obj = bpy.data.objects.new(name="FillLight", object_data=fill_data)
    bpy.context.collection.objects.link(fill_obj)
    fill_obj.location = (-0.6, -1.0, 1.3)

    # 4. Render configuration
    bpy.context.scene.render.resolution_x = 1024
    bpy.context.scene.render.resolution_y = 1024
    bpy.context.scene.render.image_settings.file_format = 'PNG'
    bpy.context.scene.render.film_transparent = True

    # Render full icon with sky blue circle
    evidence_icon = os.path.join(EVIDENCE_DIR, "icon_render.png")
    bpy.context.scene.render.filepath = evidence_icon
    bpy.ops.render.render(write_still=True)

    asset_icon = os.path.join(ASSET_DIR, "icon_render.png")
    shutil.copyfile(evidence_icon, asset_icon)
    print(f"Rendered full icon to: {evidence_icon} and {asset_icon}")

    # Render transparent foreground only (hide background disc)
    disc.hide_render = True
    fg_icon = os.path.join(EVIDENCE_DIR, "icon_foreground.png")
    bpy.context.scene.render.filepath = fg_icon
    bpy.ops.render.render(write_still=True)
    print(f"Rendered transparent foreground to: {fg_icon}")

if __name__ == "__main__":
    render_icon()
