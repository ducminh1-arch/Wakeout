import os
from PIL import Image, ImageDraw

src_path = r"C:\Users\Admin\.gemini\antigravity-ide\brain\b642c148-c907-453b-8ff1-264db8882934\wakeout_icon_fullbleed_1792144900740.jpg"
base_dir = r"d:\Wakeout"
android_res = os.path.join(base_dir, "android", "app", "src", "main", "res")

src = Image.open(src_path).convert("RGBA")

def create_circular(im):
    size = im.size
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)
    result = Image.new('RGBA', size, (0, 0, 0, 0))
    result.paste(im, (0, 0), mask=mask)
    return result

# 1. Android Mipmaps
mipmap_sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

for folder, size in mipmap_sizes.items():
    folder_path = os.path.join(android_res, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Square / standard
    resized = src.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(os.path.join(folder_path, "ic_launcher.png"), format="PNG")
    
    # Round
    round_img = create_circular(resized)
    round_img.save(os.path.join(folder_path, "ic_launcher_round.png"), format="PNG")
    print(f"Generated {folder} ({size}x{size})")

# 2. Adaptive Icon Foreground (432x432 with safe margins for Android 8+)
# Safe zone is center 66% (285px out of 432px)
fg_canvas = Image.new('RGBA', (432, 432), (0, 0, 0, 0))
fg_size = int(432 * 0.72)
fg_resized = src.resize((fg_size, fg_size), Image.Resampling.LANCZOS)
offset = (432 - fg_size) // 2
# Add a rounded squircle mask to the foreground element so it sits beautifully on adaptive background
fg_mask = Image.new('L', (fg_size, fg_size), 0)
fg_draw = ImageDraw.Draw(fg_mask)
fg_draw.rounded_rectangle((0, 0, fg_size, fg_size), radius=int(fg_size * 0.22), fill=255)
fg_canvas.paste(fg_resized, (offset, offset), mask=fg_mask)

drawable_dir = os.path.join(android_res, "drawable")
os.makedirs(drawable_dir, exist_ok=True)
fg_canvas.save(os.path.join(drawable_dir, "ic_launcher_foreground.png"), format="PNG")
print("Generated adaptive foreground (432x432)")

# 3. Web & iOS assets
web_dir = os.path.join(base_dir, "web")
src.resize((64, 64), Image.Resampling.LANCZOS).save(os.path.join(web_dir, "favicon.png"), format="PNG")
src.resize((180, 180), Image.Resampling.LANCZOS).save(os.path.join(web_dir, "apple-touch-icon.png"), format="PNG")
src.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(web_dir, "logo.png"), format="PNG")
print("Generated web assets (favicon, apple-touch-icon, logo)")

# 4. Root preview
src.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(base_dir, "app_logo_512.png"), format="PNG")
print("Generated root preview app_logo_512.png")
