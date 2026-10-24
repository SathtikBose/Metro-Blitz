"""
tools/render_blitz_views.py
Renders front, side, and 3/4 perspective views of Blitz to docs/evidence/phase-1/
"""
import bpy
import os
import math

def render_views():
    evidence_dir = "docs/evidence/phase-1"
    os.makedirs(evidence_dir, exist_ok=True)

    blend_path = "blender/source/blitz.blend"
    bpy.ops.wm.open_mainfile(filepath=blend_path)

    # Setup render settings
    scene = bpy.context.scene
    scene.render.engine = 'BLENDER_EEVEE_NEXT' if hasattr(bpy.types, 'RenderEngine') and 'BLENDER_EEVEE_NEXT' in [e.identifier for e in bpy.types.RenderSettings.bl_rna.properties['engine'].enum_items] else 'BLENDER_EEVEE'
    scene.render.resolution_x = 512
    scene.render.resolution_y = 512
    scene.render.film_transparent = True

    # Setup camera
    cam_data = bpy.data.cameras.new("EvidenceCam")
    cam_data.lens = 50
    cam_obj = bpy.data.objects.new("EvidenceCam", cam_data)
    scene.collection.objects.link(cam_obj)
    scene.camera = cam_obj

    # Setup lighting
    light_data = bpy.data.lights.new("Sun", 'SUN')
    light_data.energy = 3.0
    light_obj = bpy.data.objects.new("Sun", light_data)
    light_obj.rotation_euler = (math.radians(45), math.radians(15), math.radians(-30))
    scene.collection.objects.link(light_obj)

    # 1. Front View (faces -Y)
    cam_obj.location = (0, -3.2, 0.9)
    cam_obj.rotation_euler = (math.radians(90), 0, 0)
    scene.render.filepath = os.path.join(evidence_dir, "blitz_front.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # 2. Side View
    cam_obj.location = (-3.2, 0, 0.9)
    cam_obj.rotation_euler = (math.radians(90), 0, math.radians(-90))
    scene.render.filepath = os.path.join(evidence_dir, "blitz_side.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

    # 3. 3/4 Perspective View
    cam_obj.location = (-2.2, -2.2, 1.4)
    cam_obj.rotation_euler = (math.radians(75), 0, math.radians(-45))
    scene.render.filepath = os.path.join(evidence_dir, "blitz_3_4.png")
    bpy.ops.render.render(write_still=True)
    print(f"Rendered: {scene.render.filepath}")

if __name__ == "__main__":
    render_views()
