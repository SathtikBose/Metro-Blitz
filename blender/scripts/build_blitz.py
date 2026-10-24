"""
blender/scripts/build_blitz.py
Procedurally models, rigs, and animates Blitz for Metro Blitz.
Can be executed via Blender MCP or headlessly via Blender CLI.
"""
import bpy
import math
import os
import sys

# Ensure script directory is in sys.path to import mb_helpers
script_dir = os.path.dirname(os.path.abspath(__file__)) if "__file__" in locals() else os.getcwd()
if script_dir not in sys.path:
    sys.path.append(script_dir)

try:
    import mb_helpers as mb
except ImportError:
    # If executed inside Blender text editor or -P
    helpers_path = os.path.join(script_dir, "mb_helpers.py")
    if os.path.exists(helpers_path):
        exec(open(helpers_path).read())
    else:
        # Fallback to repo root relative path
        exec(open("blender/scripts/mb_helpers.py").read())

def setup_scene():
    bpy.context.scene.unit_settings.system = 'METRIC'
    bpy.context.scene.unit_settings.scale_length = 1.0
    bpy.context.scene.render.fps = 30

    # Clean existing Blitz objects and armature
    mb.clear_asset("blitz_")
    if "Armature" in bpy.data.objects:
        bpy.data.objects.remove(bpy.data.objects["Armature"], do_unlink=True)
    if "Body" in bpy.data.objects:
        bpy.data.objects.remove(bpy.data.objects["Body"], do_unlink=True)

def create_materials():
    mats = {
        "coral_red": mb.make_mat("M_CoralRed", "#FF6B6B", roughness=0.8),
        "sunny_yellow": mb.make_mat("M_SunnyYellow", "#FFD93D", roughness=0.8),
        "jeans_blue": mb.make_mat("M_JeansBlue", "#2F6FED", roughness=0.8),
        "skin": mb.make_mat("M_Skin", "#F2C29B", roughness=0.8)
    }
    return mats

def build_character_meshes(mats):
    parts = []

    # 1. Head (Center at z=1.35, size ~0.45m cube beveled)
    head = mb.add_box("blitz_head", size=(0.42, 0.40, 0.44), loc=(0, 0, 1.36), mat=mats["skin"], bevel=0.08)
    parts.append((head, "head"))

    # Cap (Yellow, on top of head)
    cap_dome = mb.add_sphere("blitz_cap_dome", radius=0.23, loc=(0, -0.02, 1.55), mat=mats["sunny_yellow"], scale=(1.02, 1.02, 0.6))
    parts.append((cap_dome, "head"))
    cap_visor = mb.add_box("blitz_cap_visor", size=(0.32, 0.20, 0.04), loc=(0, -0.25, 1.50), mat=mats["sunny_yellow"], bevel=0.01)
    parts.append((cap_visor, "head"))

    # Eyes (Using yellow or skin accents, facing -Y)
    eye_l = mb.add_box("blitz_eye_l", size=(0.06, 0.02, 0.08), loc=(-0.11, -0.21, 1.36), mat=mats["coral_red"], bevel=0.01)
    eye_r = mb.add_box("blitz_eye_r", size=(0.06, 0.02, 0.08), loc=(0.11, -0.21, 1.36), mat=mats["coral_red"], bevel=0.01)
    parts.append((eye_l, "head"))
    parts.append((eye_r, "head"))

    # 2. Torso (Hoodie, Coral Red, z=0.88 to 1.15)
    torso = mb.add_box("blitz_torso", size=(0.38, 0.28, 0.32), loc=(0, 0, 1.02), mat=mats["coral_red"], bevel=0.05)
    parts.append((torso, "spine"))

    # 3. Hips (Jeans Blue, z=0.72 to 0.88)
    hips = mb.add_box("blitz_hips", size=(0.34, 0.26, 0.16), loc=(0, 0, 0.80), mat=mats["jeans_blue"], bevel=0.03)
    parts.append((hips, "hips"))

    # 4. Arms (Upper and Lower with mitten hands)
    # Left Arm (-X side)
    u_arm_l = mb.add_box("blitz_upper_arm_l", size=(0.12, 0.14, 0.20), loc=(-0.25, 0, 1.00), mat=mats["coral_red"], bevel=0.02)
    parts.append((u_arm_l, "upper_arm.L"))
    l_arm_l = mb.add_box("blitz_lower_arm_l", size=(0.11, 0.13, 0.20), loc=(-0.25, -0.02, 0.82), mat=mats["skin"], bevel=0.02)
    parts.append((l_arm_l, "lower_arm.L"))

    # Right Arm (+X side)
    u_arm_r = mb.add_box("blitz_upper_arm_r", size=(0.12, 0.14, 0.20), loc=(0.25, 0, 1.00), mat=mats["coral_red"], bevel=0.02)
    parts.append((u_arm_r, "upper_arm.R"))
    l_arm_r = mb.add_box("blitz_lower_arm_r", size=(0.11, 0.13, 0.20), loc=(0.25, -0.02, 0.82), mat=mats["skin"], bevel=0.02)
    parts.append((l_arm_r, "lower_arm.R"))

    # 5. Legs (Jeans Blue)
    # Left Leg (-X side)
    thigh_l = mb.add_box("blitz_thigh_l", size=(0.14, 0.16, 0.24), loc=(-0.11, 0, 0.60), mat=mats["jeans_blue"], bevel=0.02)
    parts.append((thigh_l, "thigh.L"))
    shin_l = mb.add_box("blitz_shin_l", size=(0.13, 0.15, 0.24), loc=(-0.11, 0, 0.36), mat=mats["jeans_blue"], bevel=0.02)
    parts.append((shin_l, "shin.L"))
    foot_l = mb.add_box("blitz_foot_l", size=(0.15, 0.28, 0.14), loc=(-0.11, -0.06, 0.08), mat=mats["sunny_yellow"], bevel=0.03)
    parts.append((foot_l, "foot.L"))

    # Right Leg (+X side)
    thigh_r = mb.add_box("blitz_thigh_r", size=(0.14, 0.16, 0.24), loc=(0.11, 0, 0.60), mat=mats["jeans_blue"], bevel=0.02)
    parts.append((thigh_r, "thigh.R"))
    shin_r = mb.add_box("blitz_shin_r", size=(0.13, 0.15, 0.24), loc=(0.11, 0, 0.36), mat=mats["jeans_blue"], bevel=0.02)
    parts.append((shin_r, "shin.R"))
    foot_r = mb.add_box("blitz_foot_r", size=(0.15, 0.28, 0.14), loc=(0.11, -0.06, 0.08), mat=mats["sunny_yellow"], bevel=0.03)
    parts.append((foot_r, "foot.R"))

    return parts

def create_armature():
    bpy.ops.object.armature_add(enter_editmode=True, location=(0, 0, 0))
    arm_obj = bpy.context.active_object
    arm_obj.name = "Armature"
    eb = arm_obj.data.edit_bones

    # Rename default bone to root
    root = eb[0]
    root.name = "root"
    root.head = (0, 0, 0)
    root.tail = (0, 0, 0.2)

    # Hips
    hips = eb.new("hips")
    hips.parent = root
    hips.head = (0, 0, 0.72)
    hips.tail = (0, 0, 0.88)

    # Spine
    spine = eb.new("spine")
    spine.parent = hips
    spine.head = (0, 0, 0.88)
    spine.tail = (0, 0, 1.15)

    # Head
    head = eb.new("head")
    head.parent = spine
    head.head = (0, 0, 1.15)
    head.tail = (0, 0, 1.65)

    # Left Arm
    u_arm_l = eb.new("upper_arm.L")
    u_arm_l.parent = spine
    u_arm_l.head = (-0.20, 0, 1.10)
    u_arm_l.tail = (-0.25, 0, 0.92)

    l_arm_l = eb.new("lower_arm.L")
    l_arm_l.parent = u_arm_l
    l_arm_l.head = (-0.25, 0, 0.92)
    l_arm_l.tail = (-0.25, 0, 0.72)

    # Right Arm
    u_arm_r = eb.new("upper_arm.R")
    u_arm_r.parent = spine
    u_arm_r.head = (0.20, 0, 1.10)
    u_arm_r.tail = (0.25, 0, 0.92)

    l_arm_r = eb.new("lower_arm.R")
    l_arm_r.parent = u_arm_r
    l_arm_r.head = (0.25, 0, 0.92)
    l_arm_r.tail = (0.25, 0, 0.72)

    # Left Leg
    thigh_l = eb.new("thigh.L")
    thigh_l.parent = hips
    thigh_l.head = (-0.11, 0, 0.72)
    thigh_l.tail = (-0.11, 0, 0.48)

    shin_l = eb.new("shin.L")
    shin_l.parent = thigh_l
    shin_l.head = (-0.11, 0, 0.48)
    shin_l.tail = (-0.11, 0, 0.16)

    foot_l = eb.new("foot.L")
    foot_l.parent = shin_l
    foot_l.head = (-0.11, 0, 0.16)
    foot_l.tail = (-0.11, -0.20, 0.04)

    # Right Leg
    thigh_r = eb.new("thigh.R")
    thigh_r.parent = hips
    thigh_r.head = (0.11, 0, 0.72)
    thigh_r.tail = (0.11, 0, 0.48)

    shin_r = eb.new("shin.R")
    shin_r.parent = thigh_r
    shin_r.head = (0.11, 0, 0.48)
    shin_r.tail = (0.11, 0, 0.16)

    foot_r = eb.new("foot.R")
    foot_r.parent = shin_r
    foot_r.head = (0.11, 0, 0.16)
    foot_r.tail = (0.11, -0.20, 0.04)

    bpy.ops.object.mode_set(mode='OBJECT')
    return arm_obj

def bind_and_join(parts, arm_obj):
    # Rigid skinning: assign 100% weight of each mesh to its assigned bone
    for obj, bone_name in parts:
        vg = obj.vertex_groups.new(name=bone_name)
        vg.add(list(range(len(obj.data.vertices))), 1.0, 'REPLACE')

    # Select all parts and join into single 'Body' mesh
    bpy.ops.object.select_all(action='DESELECT')
    for obj, _ in parts:
        obj.select_set(True)
    bpy.context.view_layer.objects.active = parts[0][0]
    bpy.ops.object.join()

    body = bpy.context.active_object
    body.name = "Body"

    # Add Armature modifier
    mod = body.modifiers.new("Armature", 'ARMATURE')
    mod.object = arm_obj
    body.parent = arm_obj

    return body

# Keyframe animation helpers
def key_pose(arm_obj, frame, pose, loc_offsets=None):
    for bone_name, rot_euler_deg in pose.items():
        if bone_name in arm_obj.pose.bones:
            pb = arm_obj.pose.bones[bone_name]
            pb.rotation_mode = 'XYZ'
            pb.rotation_euler = tuple(math.radians(a) for a in rot_euler_deg)
            pb.keyframe_insert("rotation_euler", frame=frame)

    if loc_offsets:
        for bone_name, loc in loc_offsets.items():
            if bone_name in arm_obj.pose.bones:
                pb = arm_obj.pose.bones[bone_name]
                pb.location = loc
                pb.keyframe_insert("location", frame=frame)

def reset_pose(arm_obj):
    for pb in arm_obj.pose.bones:
        pb.rotation_mode = 'XYZ'
        pb.rotation_euler = (0, 0, 0)
        pb.location = (0, 0, 0)

def create_animation_clips(arm_obj):
    clips = {}

    # --- 1. Idle (frames 1..60, loop) ---
    act_idle = bpy.data.actions.new("Idle")
    act_idle.use_fake_user = True
    arm_obj.animation_data_create()
    arm_obj.animation_data.action = act_idle

    reset_pose(arm_obj)
    # Rest pose at f1 and f60
    key_pose(arm_obj, 1, {"spine": (0, 0, 0), "head": (0, 0, 0)})
    # Gentle breath / bob at f30
    key_pose(arm_obj, 30, {"spine": (-3, 0, 0), "head": (3, 0, 0)}, {"hips": (0, 0, -0.02)})
    key_pose(arm_obj, 60, {"spine": (0, 0, 0), "head": (0, 0, 0)}, {"hips": (0, 0, 0)})
    clips["Idle"] = act_idle

    # --- 2. Run (frames 1..18, loop, 0.6s) ---
    act_run = bpy.data.actions.new("Run")
    act_run.use_fake_user = True
    arm_obj.animation_data.action = act_run

    reset_pose(arm_obj)
    # Pose 1: Left leg forward, right arm forward (contact)
    p1 = {
        "spine": (10, 0, 0),
        "thigh.L": (-35, 0, 0), "shin.L": (10, 0, 0),
        "thigh.R": (30, 0, 0), "shin.R": (25, 0, 0),
        "upper_arm.L": (30, 0, 0), "lower_arm.L": (-20, 0, 0),
        "upper_arm.R": (-35, 0, 0), "lower_arm.R": (-30, 0, 0)
    }
    # Pose 2: Passing pose
    p2 = {
        "spine": (8, 0, 0),
        "thigh.L": (0, 0, 0), "shin.L": (0, 0, 0),
        "thigh.R": (10, 0, 0), "shin.R": (45, 0, 0),
        "upper_arm.L": (0, 0, 0), "lower_arm.L": (-10, 0, 0),
        "upper_arm.R": (0, 0, 0), "lower_arm.R": (-10, 0, 0)
    }
    # Pose 3: Right leg forward, left arm forward
    p3 = {
        "spine": (10, 0, 0),
        "thigh.L": (30, 0, 0), "shin.L": (25, 0, 0),
        "thigh.R": (-35, 0, 0), "shin.R": (10, 0, 0),
        "upper_arm.L": (-35, 0, 0), "lower_arm.L": (-30, 0, 0),
        "upper_arm.R": (30, 0, 0), "lower_arm.R": (-20, 0, 0)
    }

    key_pose(arm_obj, 1, p1, {"hips": (0, 0, 0.02)})
    key_pose(arm_obj, 5, p2, {"hips": (0, 0, -0.02)})
    key_pose(arm_obj, 9, p3, {"hips": (0, 0, 0.02)})
    key_pose(arm_obj, 14, p2, {"hips": (0, 0, -0.02)})
    key_pose(arm_obj, 18, p1, {"hips": (0, 0, 0.02)})
    clips["Run"] = act_run

    # --- 3. Jump (frames 1..19, 0.62s) ---
    act_jump = bpy.data.actions.new("Jump")
    act_jump.use_fake_user = True
    arm_obj.animation_data.action = act_jump
    reset_pose(arm_obj)

    # Crouch (f1-f3)
    key_pose(arm_obj, 1, {"spine": (0, 0, 0)})
    key_pose(arm_obj, 3, {"spine": (15, 0, 0), "thigh.L": (25, 0, 0), "thigh.R": (25, 0, 0), "shin.L": (30, 0, 0), "shin.R": (30, 0, 0)}, {"hips": (0, 0, -0.10)})
    # Apex tuck (f9)
    key_pose(arm_obj, 9, {
        "spine": (-5, 0, 0),
        "thigh.L": (-60, 0, 0), "shin.L": (70, 0, 0),
        "thigh.R": (-60, 0, 0), "shin.R": (70, 0, 0),
        "upper_arm.L": (-70, 0, 0), "upper_arm.R": (-70, 0, 0)
    }, {"hips": (0, 0, 0.15)})
    # Extend legs for landing (f15-f19)
    key_pose(arm_obj, 15, {"thigh.L": (-10, 0, 0), "shin.L": (10, 0, 0), "thigh.R": (-10, 0, 0), "shin.R": (10, 0, 0)})
    key_pose(arm_obj, 19, {"spine": (0, 0, 0), "thigh.L": (0, 0, 0), "shin.L": (0, 0, 0), "thigh.R": (0, 0, 0), "shin.R": (0, 0, 0)}, {"hips": (0, 0, 0)})
    clips["Jump"] = act_jump

    # --- 4. Slide (frames 1..20, 0.65s) ---
    act_slide = bpy.data.actions.new("Slide")
    act_slide.use_fake_user = True
    arm_obj.animation_data.action = act_slide
    reset_pose(arm_obj)

    # Drop low (f1..f4) -> hold slide (f4..f16) -> rise (f17..f20)
    key_pose(arm_obj, 1, {"spine": (0, 0, 0)})
    slide_pose = {
        "spine": (-50, 0, 0),
        "thigh.L": (-80, 0, 0), "shin.L": (15, 0, 0), "foot.L": (20, 0, 0),
        "thigh.R": (-70, 0, 0), "shin.R": (70, 0, 0),
        "upper_arm.L": (40, 0, 0), "upper_arm.R": (40, 0, 0)
    }
    key_pose(arm_obj, 4, slide_pose, {"hips": (0, 0, -0.45)})
    key_pose(arm_obj, 16, slide_pose, {"hips": (0, 0, -0.45)})
    key_pose(arm_obj, 20, {"spine": (0, 0, 0), "thigh.L": (0, 0, 0), "shin.L": (0, 0, 0), "thigh.R": (0, 0, 0), "shin.R": (0, 0, 0)}, {"hips": (0, 0, 0)})
    clips["Slide"] = act_slide

    # --- 5. LeanLeft (frames 1..6, 0.2s) ---
    act_lean_l = bpy.data.actions.new("LeanLeft")
    act_lean_l.use_fake_user = True
    arm_obj.animation_data.action = act_lean_l
    reset_pose(arm_obj)
    key_pose(arm_obj, 1, {"root": (0, 0, 0)})
    key_pose(arm_obj, 3, {"root": (0, -15, 0), "spine": (0, -10, 0)})
    key_pose(arm_obj, 6, {"root": (0, 0, 0), "spine": (0, 0, 0)})
    clips["LeanLeft"] = act_lean_l

    # --- 6. LeanRight (frames 1..6, 0.2s) ---
    act_lean_r = bpy.data.actions.new("LeanRight")
    act_lean_r.use_fake_user = True
    arm_obj.animation_data.action = act_lean_r
    reset_pose(arm_obj)
    key_pose(arm_obj, 1, {"root": (0, 0, 0)})
    key_pose(arm_obj, 3, {"root": (0, 15, 0), "spine": (0, 10, 0)})
    key_pose(arm_obj, 6, {"root": (0, 0, 0), "spine": (0, 0, 0)})
    clips["LeanRight"] = act_lean_r

    # --- 7. Hit (frames 1..24, 0.8s) ---
    act_hit = bpy.data.actions.new("Hit")
    act_hit.use_fake_user = True
    arm_obj.animation_data.action = act_hit
    reset_pose(arm_obj)
    key_pose(arm_obj, 1, {"spine": (0, 0, 0)})
    # Stumble backwards
    key_pose(arm_obj, 6, {
        "spine": (-30, 0, 0),
        "head": (-25, 0, 0),
        "upper_arm.L": (-80, 0, 40),
        "upper_arm.R": (-80, 0, -40),
        "thigh.L": (-40, 0, 0), "thigh.R": (20, 0, 0)
    }, {"hips": (0, 0.1, 0.05)})
    # Fall down on hips
    key_pose(arm_obj, 18, {
        "spine": (-55, 0, 0),
        "head": (20, 0, 0),
        "thigh.L": (-85, 0, -15), "shin.L": (40, 0, 0),
        "thigh.R": (-85, 0, 15), "shin.R": (40, 0, 0)
    }, {"hips": (0, 0.35, -0.40)})
    # Rest on ground
    key_pose(arm_obj, 24, {
        "spine": (-55, 0, 0),
        "head": (20, 0, 0),
        "thigh.L": (-85, 0, -15), "shin.L": (40, 0, 0),
        "thigh.R": (-85, 0, 15), "shin.R": (40, 0, 0)
    }, {"hips": (0, 0.35, -0.40)})
    clips["Hit"] = act_hit

    # Reset default action to Idle
    arm_obj.animation_data.action = act_idle
    return clips

def main():
    print("==> Initializing Blitz model generation...")
    setup_scene()
    mats = create_materials()
    parts = build_character_meshes(mats)
    arm_obj = create_armature()
    body = bind_and_join(parts, arm_obj)
    clips = create_animation_clips(arm_obj)

    total_tris = mb.tri_count([body])
    print(f"==> Character built successfully!")
    print(f"    Total Triangles: {total_tris} (Budget: 5000)")
    print(f"    Materials: {len(body.data.materials)} (Budget: 4)")
    print(f"    Clips generated: {list(clips.keys())}")

    # Output paths
    output_dir = "app/src/main/assets/models/characters"
    os.makedirs(output_dir, exist_ok=True)
    glb_path = os.path.join(output_dir, "blitz.glb")
    blend_dir = "blender/source"
    os.makedirs(blend_dir, exist_ok=True)
    blend_path = os.path.join(blend_dir, "blitz.blend")

    # Save .blend
    bpy.ops.wm.save_as_mainfile(filepath=blend_path)
    print(f"==> Saved blend file: {blend_path}")

    # Export .glb
    mb.export_glb(glb_path, [body], export_animations=True)
    print(f"==> Exported GLB to: {glb_path}")

if __name__ == "__main__":
    main()
