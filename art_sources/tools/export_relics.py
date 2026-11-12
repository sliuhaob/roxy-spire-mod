"""Export relic art into the three images the game loads for each relic.

For every job in art_sources/relics/api_20261111/manifest.json (or the names given):
  <name>.png          128x128, object scaled to fit a 72 px box, centred
  <name>_large.png    256x256, the same at twice the size (relic popup)
  <name>_outline.png  128x128 white silhouette, the icon's shape grown by 3 px,
                      which the game tints for the dark rim and the hover/flash glow
--outline-only rebuilds just the outline from an existing <name>.png, for relics whose
art is kept. Requires Python 3 and Pillow.
"""
import argparse
import json
import os

from PIL import Image, ImageFilter

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SOURCE = os.path.join(ROOT, "art_sources", "relics", "api_20261111")
TARGET = os.path.join(ROOT, "src", "main", "resources", "roxymod", "images", "relics")


def fit(image, canvas, box):
    """Scale a trimmed RGBA image to fit box x box and centre it on a transparent canvas."""
    scale = box / max(image.width, image.height)
    size = (max(1, round(image.width * scale)), max(1, round(image.height * scale)))
    # Resize premultiplied so transparent pixels do not bleed dark fringes into the edges.
    resized = image.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
    out = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    out.paste(resized, ((canvas - size[0]) // 2, (canvas - size[1]) // 2))
    return out


def outline(icon):
    mask = icon.split()[3].point(lambda a: 255 if a > 16 else 0)
    grown = mask.filter(ImageFilter.MaxFilter(7)).filter(ImageFilter.GaussianBlur(0.6))
    white = Image.new("RGBA", icon.size, (255, 255, 255, 0))
    white.putalpha(grown)
    return white


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("names", nargs="*")
    parser.add_argument("--outline-only", nargs="*", default=[])
    args = parser.parse_args()
    with open(os.path.join(SOURCE, "manifest.json"), encoding="utf-8") as handle:
        names = args.names or [job["name"] for job in json.load(handle)["jobs"]]
    if args.outline_only and not args.names:
        names = []
    for name in names:
        source = Image.open(os.path.join(SOURCE, name + ".png")).convert("RGBA")
        bbox = source.split()[3].point(lambda a: 255 if a > 8 else 0).getbbox()
        trimmed = source.crop(bbox)
        icon = fit(trimmed, 128, 72)
        icon.save(os.path.join(TARGET, name + ".png"))
        fit(trimmed, 256, 144).save(os.path.join(TARGET, name + "_large.png"))
        outline(icon).save(os.path.join(TARGET, name + "_outline.png"))
        print("exported", name)
    for name in args.outline_only:
        icon = Image.open(os.path.join(TARGET, name + ".png")).convert("RGBA")
        outline(icon).save(os.path.join(TARGET, name + "_outline.png"))
        print("outline", name)


if __name__ == "__main__":
    main()
