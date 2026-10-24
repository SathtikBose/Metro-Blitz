"""
Metro Blitz — Blender Python Helper Library
Used by all procedural asset generation scripts.
"""
import bpy
import math
import bmesh

def srgb_to_lin(c: float) -> float:
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4

def make_mat(name: str, hex_color: str, roughness: float = 0.8) -> bpy.types.Material:
    mat = bpy.data.materials.get(name) or bpy.data.materials.new(name)
    mat.use_nodes = True
    # Look up Principled BSDF by type to handle localized Blender versions
    bsdf = next((n for n in mat.node_tree.nodes if n.type == "BSDF_PRINCIPLED"), None)
    if not bsdf:
        bsdf = mat.node_tree.nodes.new("ShaderNodeBsdfPrincipled")

    r, g, b = (int(hex_color[i:i+2], 16) / 255.0 for i in (1, 3, 5))
    bsdf.inputs["Base Color"].default_value = (srgb_to_lin(r), srgb_to_lin(g), srgb_to_lin(b), 1.0)
    bsdf.inputs["Roughness"].default_value = roughness
    bsdf.inputs["Metallic"].default_value = 0.0
    return mat

def clear_asset(prefix: str):
    """Safely removes objects created with a given prefix without affecting scene cameras/lights/addons."""
    to_remove = [o for o in bpy.data.objects if o.name.startswith(prefix)]
    for o in to_remove:
        bpy.data.objects.remove(o, do_unlink=True)

def add_box(name: str, size: tuple, loc: tuple, mat: bpy.types.Material, bevel: float = 0.0) -> bpy.types.Object:
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=loc)
    o = bpy.context.active_object
    o.name = name
    o.scale = size
    bpy.ops.object.transform_apply(scale=True)
    if bevel > 0:
        m = o.modifiers.new("Bevel", 'BEVEL')
        m.width = bevel
        m.segments = 2
    if mat:
        o.data.materials.append(mat)
    bpy.ops.object.shade_smooth()
    return o

def add_cylinder(name: str, radius: float, depth: float, loc: tuple, mat: bpy.types.Material, verts: int = 12, rot: tuple = (0, 0, 0)) -> bpy.types.Object:
    bpy.ops.mesh.primitive_cylinder_add(vertices=verts, radius=radius, depth=depth, location=loc, rotation=rot)
    o = bpy.context.active_object
    o.name = name
    if mat:
        o.data.materials.append(mat)
    return o

def add_sphere(name: str, radius: float, loc: tuple, mat: bpy.types.Material, scale: tuple = (1, 1, 1)) -> bpy.types.Object:
    bpy.ops.mesh.primitive_uv_sphere_add(segments=12, ring_count=8, radius=radius, location=loc)
    o = bpy.context.active_object
    o.name = name
    o.scale = scale
    bpy.ops.object.transform_apply(scale=True)
    if mat:
        o.data.materials.append(mat)
    bpy.ops.object.shade_smooth()
    return o

def tri_count(objs: list) -> int:
    total = 0
    depsgraph = bpy.context.evaluated_depsgraph_get()
    for o in objs:
        if o.type == 'MESH':
            me = o.evaluated_get(depsgraph).to_mesh()
            me.calc_loop_triangles()
            total += len(me.loop_triangles)
    return total

def export_glb(path: str, objects: list, export_animations: bool = True):
    bpy.ops.object.select_all(action='DESELECT')
    for o in objects:
        o.select_set(True)
    
    # Ensure parent armature is also selected if exporting skinned mesh
    for o in objects:
        if o.parent and o.parent.type == 'ARMATURE':
            o.parent.select_set(True)

    bpy.ops.export_scene.gltf(
        filepath=path,
        export_format='GLB',
        use_selection=True,
        export_apply=True,
        export_yup=True,
        export_materials='EXPORT',
        export_cameras=False,
        export_lights=False,
        export_animations=export_animations,
        export_animation_mode='ACTIONS' if export_animations else 'SCENE',
        export_draco_mesh_compression_enable=False
    )
