"""Generate one image with the OpenAI Images API and record how it was made.

The API key comes from the OPENAI_API_KEY environment variable and is never written
anywhere. Next to each image the script writes <name>.json with the model, prompt,
settings, reference images and token usage, so the art can be traced and remade.

    python art_sources/tools/openai_image.py --prompt-file p.txt --out out.png \
        [--ref style.png ...] [--model gpt-image-2] [--quality medium]

With --ref the request goes to /images/edits and the references guide style;
without it, to /images/generations. Requires Python 3 and requests.
"""
import argparse
import base64
import json
import os
import sys
import time

import requests


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--prompt")
    parser.add_argument("--prompt-file")
    parser.add_argument("--out", required=True)
    parser.add_argument("--ref", action="append", default=[])
    parser.add_argument("--model", default="gpt-image-2")
    parser.add_argument("--size", default="1024x1024")
    parser.add_argument("--quality", default="medium")
    parser.add_argument("--background", default="transparent")
    args = parser.parse_args()

    prompt = args.prompt
    if args.prompt_file:
        with open(args.prompt_file, encoding="utf-8") as handle:
            prompt = handle.read().strip()
    if not prompt:
        sys.exit("No prompt given.")
    key = os.environ.get("OPENAI_API_KEY", "").strip()
    if not key:
        sys.exit("Set OPENAI_API_KEY first.")
    base = os.environ.get("OPENAI_BASE_URL", "https://api.openai.com/v1").rstrip("/")
    headers = {"Authorization": "Bearer " + key}
    settings = {
        "model": args.model,
        "prompt": prompt,
        "size": args.size,
        "quality": args.quality,
        "background": args.background,
        "output_format": "png",
        "n": 1,
    }

    started = time.time()
    if args.ref:
        files = [("image[]", (os.path.basename(p), open(p, "rb"), "image/png")) for p in args.ref]
        data = {k: str(v) for k, v in settings.items()}
        response = requests.post(base + "/images/edits", headers=headers, data=data, files=files, timeout=600)
    else:
        response = requests.post(base + "/images/generations", headers=headers, json=settings, timeout=600)
    if response.status_code != 200:
        try:
            error = response.json().get("error", {})
            message = "%s: %s" % (error.get("code") or error.get("type"), error.get("message"))
        except ValueError:
            message = response.text[:300]
        sys.exit("HTTP %d %s" % (response.status_code, message))

    body = response.json()
    image = base64.b64decode(body["data"][0]["b64_json"])
    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with open(args.out, "wb") as handle:
        handle.write(image)
    record = dict(settings)
    record.update({
        "references": [os.path.basename(p) for p in args.ref],
        "endpoint": "images/edits" if args.ref else "images/generations",
        "usage": body.get("usage"),
        "created": body.get("created"),
        "seconds": round(time.time() - started, 1),
    })
    with open(os.path.splitext(args.out)[0] + ".json", "w", encoding="utf-8") as handle:
        json.dump(record, handle, ensure_ascii=False, indent=2)
    print("wrote %s (%d bytes, %.0fs, usage %s)" % (args.out, len(image), record["seconds"], body.get("usage")))


if __name__ == "__main__":
    main()
