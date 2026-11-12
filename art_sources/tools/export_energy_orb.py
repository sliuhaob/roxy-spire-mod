"""Cut the generated energy orb art into the 128 px layers RoxyEnergyOrb draws.

Inputs (art_sources/ui/energy_orb_20261111): frame.png, a ring with an empty centre, and
vortex.png, a swirl disc. Outputs (images/ui/energyorb):
  frame.png              static ring, centred on its hole, about 118 px across (drawn on top)
  vortex.png             swirl cut to a circle just larger than the hole, centred on the
                         swirl's bright core so it spins in place
  glints.png             the swirl's brightest streaks, spun the other way for shimmer
  vortex_d.png, glints_d.png   greyed, darker versions shown when energy is 0
Requires Python 3 and Pillow.
"""
import os

from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFilter, ImageOps

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SOURCE = os.path.join(ROOT, "art_sources", "ui", "energy_orb_20261111")
TARGET = os.path.join(ROOT, "src", "main", "resources", "roxymod", "images", "ui", "energyorb")
CANVAS = 128
FRAME_SPAN = 118  # outer size of the ring in the 128 px layer, like the vanilla base layer


def hole_of(frame):
    """Centre and radius of the transparent hole, found by flooding from the image centre."""
    alpha = frame.split()[3]
    opaque = alpha.point(lambda a: 255 if a > 40 else 0)
    w, h = frame.size
    seed = (w // 2, h // 2)
    if opaque.getpixel(seed):
        raise SystemExit("frame.png has no transparent centre")
    marker = opaque.copy()
    ImageDraw.floodfill(marker, seed, 128)
    hole = marker.point(lambda v: 255 if v == 128 else 0)
    x0, y0, x1, y1 = hole.getbbox()
    return ((x0 + x1) / 2.0, (y0 + y1) / 2.0), min(x1 - x0, y1 - y0) / 2.0


def core_of(vortex):
    """Centre of the swirl: the centroid of its brightest pixels."""
    gray = vortex.convert("L")
    gray = Image.composite(gray, Image.new("L", gray.size, 0), vortex.split()[3])
    cutoff = sorted(gray.getdata())[int(gray.width * gray.height * 0.995)]
    bright = gray.point(lambda v: 255 if v >= cutoff else 0)
    xs = ys = n = 0
    for index, value in enumerate(bright.getdata()):
        if value:
            xs += index % gray.width
            ys += index // gray.width
            n += 1
    return xs / n, ys / n


def crop_square(image, centre, half):
    cx, cy = centre
    box = (round(cx - half), round(cy - half), round(cx + half), round(cy + half))
    canvas = Image.new("RGBA", (box[2] - box[0], box[3] - box[1]), (0, 0, 0, 0))
    canvas.paste(image.crop(box), (0, 0))
    return canvas


def shrink(image, size):
    return image.convert("RGBa").resize((size, size), Image.LANCZOS).convert("RGBA")


def circle_mask(size, radius, feather=0.8):
    scale = 4
    mask = Image.new("L", (size * scale, size * scale), 0)
    centre = size * scale / 2.0
    r = radius * scale
    ImageDraw.Draw(mask).ellipse((centre - r, centre - r, centre + r, centre + r), fill=255)
    return mask.resize((size, size), Image.LANCZOS).filter(ImageFilter.GaussianBlur(feather))


def disabled(layer, keep=0.25, dim=0.6):
    """Mostly grey and darker, like the vanilla no-energy layers."""
    rgb = layer.convert("RGB")
    grey = ImageOps.grayscale(rgb).convert("RGB")
    mixed = Image.blend(grey, rgb, keep)
    mixed = ImageEnhance.Brightness(mixed).enhance(dim)
    out = mixed.convert("RGBA")
    out.putalpha(layer.split()[3])
    return out


def main():
    os.makedirs(TARGET, exist_ok=True)
    frame = Image.open(os.path.join(SOURCE, "frame.png")).convert("RGBA")
    vortex = Image.open(os.path.join(SOURCE, "vortex.png")).convert("RGBA")

    (hx, hy), hole_r = hole_of(frame)
    x0, y0, x1, y1 = frame.split()[3].point(lambda a: 255 if a > 8 else 0).getbbox()
    reach = max(hx - x0, x1 - hx, hy - y0, y1 - hy)
    scale = (FRAME_SPAN / 2.0) / reach
    frame_layer = shrink(crop_square(frame, (hx, hy), CANVAS / 2.0 / scale), CANVAS)
    hole_px = hole_r * scale
    frame_layer.save(os.path.join(TARGET, "frame.png"))

    # The swirl disc sits under the ring, slightly larger than the hole so no gap shows.
    disc_r = hole_px + 2.0
    core = core_of(vortex)
    vx0, vy0, vx1, vy1 = vortex.split()[3].point(lambda a: 255 if a > 200 else 0).getbbox()
    disc_reach = min(core[0] - vx0, vx1 - core[0], core[1] - vy0, vy1 - core[1])
    # Use the inner 85% of the disc so its dark painted rim stays hidden under the ring.
    vscale = disc_r / (disc_reach * 0.85)
    swirl = shrink(crop_square(vortex, core, CANVAS / 2.0 / vscale), CANVAS)
    mask = circle_mask(CANVAS, disc_r)
    swirl.putalpha(ImageChops.multiply(swirl.split()[3], mask))
    swirl.save(os.path.join(TARGET, "vortex.png"))

    # Glints: the brightest streaks only, soft-edged, at partial strength.
    lum = swirl.convert("L")
    glint_alpha = lum.point(lambda v: max(0, min(255, (v - 150) * 3))).filter(ImageFilter.GaussianBlur(0.6))
    glint_alpha = ImageChops.multiply(glint_alpha, mask).point(lambda v: int(v * 0.55))
    glints = Image.new("RGBA", swirl.size, (210, 250, 255, 0))
    glints.putalpha(glint_alpha)
    glints.save(os.path.join(TARGET, "glints.png"))

    disabled(swirl).save(os.path.join(TARGET, "vortex_d.png"))
    disabled(glints, keep=0.2, dim=0.5).save(os.path.join(TARGET, "glints_d.png"))
    print("hole centre (%.0f, %.0f) radius %.0f px -> %.1f px in the layer; swirl core (%.0f, %.0f)"
          % (hx, hy, hole_r, hole_px, core[0], core[1]))


if __name__ == "__main__":
    main()
