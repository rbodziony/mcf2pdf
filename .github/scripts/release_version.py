"""Only publish non-SNAPSHOT Maven versions whose tag matches the project version."""

import os
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET


def release_version(pom: Path, tag: str) -> str | None:
    root = ET.parse(pom).getroot()
    version = root.findtext("{http://maven.apache.org/POM/4.0.0}version", "").strip()
    if not version:
        raise ValueError("Missing project version in pom.xml")
    if "SNAPSHOT" in version.upper():
        return None
    if not re.fullmatch(r"[0-9][0-9A-Za-z.+-]*", version):
        raise ValueError(f"Not a concrete release version: {version!r}")
    if tag not in (version, f"v{version}", f"mcf2pdf-{version}"):
        raise ValueError(f"Tag {tag!r} does not match pom.xml version {version!r}")
    return version


def main() -> int:
    try:
        version = release_version(Path("pom.xml"), os.environ["RELEASE_TAG"])
    except (ValueError, KeyError, ET.ParseError, OSError) as error:
        print(f"Release version check failed: {error}", file=sys.stderr)
        return 1
    with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
        output.write(f"publish={'true' if version else 'false'}\n")
        if version:
            output.write(f"version={version}\n")
    if version is None:
        print("SNAPSHOT version: skipping GitHub Release")
    return 0


if __name__ == "__main__":
    sys.exit(main())
