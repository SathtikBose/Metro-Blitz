"""
tools/validate_glb.py
Validates binary glTF (.glb) files against Metro Blitz asset specifications.
Checks:
- File size < 1 MB
- Triangle budget
- Material count <= 4
- Texture count == 0
- Required animation clips for character models
"""
import sys
import os
import struct
import json

BUDGETS = {
    "blitz.glb": {
        "max_triangles": 5000,
        "max_materials": 4,
        "max_textures": 0,
        "required_clips": ["Idle", "Run", "Jump", "Slide", "LeanLeft", "LeanRight", "Hit"]
    },
    "track_segment.glb": {
        "max_triangles": 2500,
        "max_materials": 4,
        "max_textures": 0
    },
    "building_a.glb": {"max_triangles": 400, "max_materials": 4, "max_textures": 0},
    "building_b.glb": {"max_triangles": 400, "max_materials": 4, "max_textures": 0},
    "building_c.glb": {"max_triangles": 400, "max_materials": 4, "max_textures": 0},
    "building_d.glb": {"max_triangles": 400, "max_materials": 4, "max_textures": 0},
    "lamp_post.glb": {"max_triangles": 150, "max_materials": 4, "max_textures": 0},
    "tree.glb": {"max_triangles": 150, "max_materials": 4, "max_textures": 0},
    "fence.glb": {"max_triangles": 100, "max_materials": 4, "max_textures": 0},
    "barricade_low.glb": {"max_triangles": 300, "max_materials": 4, "max_textures": 0},
    "overhead_beam.glb": {"max_triangles": 400, "max_materials": 4, "max_textures": 0},
    "train_blue.glb": {"max_triangles": 1500, "max_materials": 4, "max_textures": 0},
    "train_orange.glb": {"max_triangles": 1500, "max_materials": 4, "max_textures": 0},
    "coin.glb": {"max_triangles": 200, "max_materials": 4, "max_textures": 0}
}

def parse_glb_json(filepath):
    with open(filepath, "rb") as f:
        magic, version, length = struct.unpack("<4sII", f.read(12))
        if magic != b"glTF":
            raise ValueError(f"Invalid GLB magic: {magic}")
        
        chunk_len, chunk_type = struct.unpack("<II", f.read(8))
        if chunk_type != 0x4E4F534A: # "JSON"
            raise ValueError("Expected JSON chunk")
        
        json_bytes = f.read(chunk_len)
        return json.loads(json_bytes.decode("utf-8"))

def validate_glb(filepath):
    filename = os.path.basename(filepath)
    file_size_kb = os.path.getsize(filepath) / 1024.0
    print(f"\n--- Validating: {filename} ({file_size_kb:.2f} KB) ---")

    gltf = parse_glb_json(filepath)
    accessors = gltf.get("accessors", [])
    meshes = gltf.get("meshes", [])
    materials = gltf.get("materials", [])
    textures = gltf.get("textures", [])
    animations = gltf.get("animations", [])

    # Calculate total triangle count
    total_triangles = 0
    for mesh in meshes:
        for prim in mesh.get("primitives", []):
            if "indices" in prim:
                idx_accessor = accessors[prim["indices"]]
                total_triangles += idx_accessor.get("count", 0) // 3
            elif "attributes" in prim and "POSITION" in prim["attributes"]:
                pos_accessor = accessors[prim["attributes"]["POSITION"]]
                total_triangles += pos_accessor.get("count", 0) // 3

    clip_names = [a.get("name", "unnamed") for a in animations]

    print(f"  Meshes: {len(meshes)}")
    print(f"  Triangles: {total_triangles}")
    print(f"  Materials: {len(materials)}")
    print(f"  Textures: {len(textures)}")
    print(f"  Animations ({len(clip_names)}): {clip_names}")

    errors = []
    if file_size_kb > 1024.0:
        errors.append(f"File size {file_size_kb:.2f} KB exceeds 1024 KB budget")

    if len(textures) > 0:
        errors.append(f"Found {len(textures)} textures; spec requires 0 textures (flat colors only)")

    if len(materials) > 4:
        errors.append(f"Found {len(materials)} materials; exceeds budget of 4")

    spec = BUDGETS.get(filename)
    if spec:
        max_tri = spec.get("max_triangles", 5000)
        if total_triangles > max_tri:
            errors.append(f"Triangles ({total_triangles}) exceeds budget of {max_tri}")

        required_clips = spec.get("required_clips", [])
        for clip in required_clips:
            if clip not in clip_names:
                errors.append(f"Missing required animation clip: '{clip}'")

    if errors:
        print("  FAILURES:")
        for err in errors:
            print(f"    - {err}")
        return False

    print("  RESULT: PASSED")
    return True

def main():
    if len(sys.argv) < 2:
        print("Usage: python validate_glb.py <path_to_glb> [more_paths...]")
        sys.exit(1)

    all_passed = True
    for path in sys.argv[1:]:
        if os.path.exists(path):
            if not validate_glb(path):
                all_passed = False
        else:
            print(f"File not found: {path}")
            all_passed = False

    sys.exit(0 if all_passed else 1)

if __name__ == "__main__":
    main()
