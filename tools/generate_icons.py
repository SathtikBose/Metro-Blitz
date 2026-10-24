"""
tools/generate_icons.py
Resizes the rendered 1024x1024 Blender app icon into all standard Android mipmap densities:
- ic_launcher.png (legacy non-adaptive icon)
- ic_launcher_round.png (circular legacy icon)
- ic_launcher_foreground.png (108dp adaptive icon foreground)
"""
import os
from PIL import Image, ImageDraw

def create_circular_icon(img):
    size = img.size
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)
    result = Image.new('RGBA', size, (0, 0, 0, 0))
    result.paste(img, (0, 0), mask)
    return result

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    source_icon = os.path.join(base_dir, "..", "docs", "evidence", "phase-8", "icon_render.png")
    source_fg = os.path.join(base_dir, "..", "docs", "evidence", "phase-8", "icon_foreground.png")
    res_dir = os.path.join(base_dir, "..", "app", "src", "main", "res")

    img_icon = Image.open(source_icon).convert("RGBA")
    img_fg = Image.open(source_fg).convert("RGBA")

    # Densities for launcher icon (48dp base)
    launcher_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }

    # Densities for adaptive icon foreground (108dp base)
    fg_sizes = {
        "mipmap-mdpi": 108,
        "mipmap-hdpi": 162,
        "mipmap-xhdpi": 216,
        "mipmap-xxhdpi": 324,
        "mipmap-xxxhdpi": 432
    }

    for folder, size in launcher_sizes.items():
        out_folder = os.path.join(res_dir, folder)
        os.makedirs(out_folder, exist_ok=True)

        # Standard icon
        resized_icon = img_icon.resize((size, size), Image.Resampling.LANCZOS)
        resized_icon.save(os.path.join(out_folder, "ic_launcher.png"), "PNG")

        # Round icon
        round_icon = create_circular_icon(resized_icon)
        round_icon.save(os.path.join(out_folder, "ic_launcher_round.png"), "PNG")

    for folder, size in fg_sizes.items():
        out_folder = os.path.join(res_dir, folder)
        os.makedirs(out_folder, exist_ok=True)

        resized_fg = img_fg.resize((size, size), Image.Resampling.LANCZOS)
        resized_fg.save(os.path.join(out_folder, "ic_launcher_foreground.png"), "PNG")

    # Update ic_launcher_background.xml
    bg_xml_path = os.path.join(res_dir, "drawable", "ic_launcher_background.xml")
    with open(bg_xml_path, "w", encoding="utf-8") as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#4FC3F7"
        android:pathData="M0,0h108v108h-108z" />
</vector>
''')

    # Update mipmap-anydpi-v26/ic_launcher.xml and ic_launcher_round.xml
    anydpi_dir = os.path.join(res_dir, "mipmap-anydpi-v26")
    os.makedirs(anydpi_dir, exist_ok=True)
    with open(os.path.join(anydpi_dir, "ic_launcher.xml"), "w", encoding="utf-8") as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
''')
    with open(os.path.join(anydpi_dir, "ic_launcher_round.xml"), "w", encoding="utf-8") as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
''')

    print("All Android launcher icons and adaptive vector backgrounds generated successfully!")

if __name__ == "__main__":
    main()
