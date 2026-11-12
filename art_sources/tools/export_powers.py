"""Export power icon art to the two sizes PowerIconHelper loads.

For every job in art_sources/powers/api_20261111/manifest.json (or the names given):
  <name>84.png  84x84, symbol scaled to fit 72 px, centred (power tooltips and flashes)
  <name>32.png  32x32, symbol scaled to fit 28 px, centred (icon row under the health bar)
Requires Python 3 and Pillow.
"""
import argparse
import json
import os

from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SOURCE = os.path.join(ROOT, "art_sources", "powers", "api_20261111")
TARGET = os.path.join(ROOT, "src", "main", "resources", "roxymod", "images", "powers")


def fit(image, canvas, box):
    scale = box / max(image.width, image.height)
    size = (max(1, round(image.width * scale)), max(1, round(image.height * scale)))
    resized = image.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
    out = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    out.paste(resized, ((canvas - size[0]) // 2, (canvas - size[1]) // 2))
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("names", nargs="*")
    args = parser.parse_args()
    with open(os.path.join(SOURCE, "manifest.json"), encoding="utf-8") as handle:
        names = args.names or [job["name"] for job in json.load(handle)["jobs"]]
    for name in names:
        source = Image.open(os.path.join(SOURCE, name + ".png")).convert("RGBA")
        trimmed = source.crop(source.split()[3].point(lambda a: 255 if a > 8 else 0).getbbox())
        fit(trimmed, 84, 72).save(os.path.join(TARGET, name + "84.png"))
        fit(trimmed, 32, 28).save(os.path.join(TARGET, name + "32.png"))
        print("exported", name)


if __name__ == "__main__":
    main()
