"""Generate every image listed in a manifest.json with the OpenAI Images API.

manifest.json sits next to its outputs and looks like:
  {"model": "...", "quality": "...", "style": "...", "references": ["../x.png"],
   "jobs": [{"name": "teaching_pointer", "subject": "...", "references": [...]}]}
Each job's prompt is the shared style text followed by the job's subject. Images that
already exist are skipped unless named with --only. Output: <name>.png plus <name>.json
recording the exact request. Needs OPENAI_API_KEY in the environment.

    python art_sources/tools/generate_batch.py art_sources/relics/api_20261111/manifest.json
"""
import argparse
import json
import os
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor

HERE = os.path.dirname(os.path.abspath(__file__))


def run(job, folder, manifest):
    out = os.path.join(folder, job["name"] + ".png")
    prompt = (manifest.get("style", "") + "\n\nSubject: " + job["subject"]).strip()
    prompt_file = os.path.join(folder, job["name"] + ".prompt.txt")
    with open(prompt_file, "w", encoding="utf-8") as handle:
        handle.write(prompt + "\n")
    command = [sys.executable, os.path.join(HERE, "openai_image.py"), "--prompt-file", prompt_file, "--out", out,
               "--model", job.get("model", manifest.get("model", "gpt-image-1.5")),
               "--quality", job.get("quality", manifest.get("quality", "medium")),
               "--background", job.get("background", manifest.get("background", "transparent"))]
    for ref in job.get("references", manifest.get("references", [])):
        command += ["--ref", os.path.normpath(os.path.join(folder, ref))]
    result = subprocess.run(command, capture_output=True, text=True, encoding="utf-8")
    os.remove(prompt_file)
    return job["name"], result.returncode, (result.stdout + result.stderr).strip().splitlines()[-1:]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest")
    parser.add_argument("--only", nargs="*", help="regenerate these names even if they exist")
    parser.add_argument("--workers", type=int, default=3)
    args = parser.parse_args()
    folder = os.path.dirname(os.path.abspath(args.manifest))
    with open(args.manifest, encoding="utf-8") as handle:
        manifest = json.load(handle)
    jobs = [j for j in manifest["jobs"]
            if (args.only and j["name"] in args.only)
            or (not args.only and not os.path.exists(os.path.join(folder, j["name"] + ".png")))]
    print("%d of %d images to generate" % (len(jobs), len(manifest["jobs"])))
    failed = 0
    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        for name, code, tail in pool.map(lambda j: run(j, folder, manifest), jobs):
            failed += code != 0
            print("%-22s %s %s" % (name, "ok  " if code == 0 else "FAIL", " ".join(tail)))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
