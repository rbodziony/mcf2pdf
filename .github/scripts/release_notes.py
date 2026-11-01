"""Combine GitHub's generated notes with every commit since the previous release."""

import json
import os
from pathlib import Path
import re
import subprocess
from urllib.parse import quote


def git(directory, *args):
    return subprocess.check_output(
        ["git", *args], cwd=directory, encoding="utf-8", errors="replace"
    ).strip("\n")


def github(*args):
    return json.loads(subprocess.check_output(["gh", "api", *args], encoding="utf-8"))


def previous_release(directory, tag, releases):
    current = git(directory, "rev-parse", f"refs/tags/{tag}^{{commit}}")
    candidates = []
    for release in releases:
        name = release["tag_name"]
        if release["draft"] or name == tag:
            continue
        ref = f"refs/tags/{name}"
        commit = git(directory, "rev-parse", f"{ref}^{{commit}}")
        if commit == current:
            continue
        ancestor = subprocess.run(
            ["git", "merge-base", "--is-ancestor", commit, current], cwd=directory
        )
        if ancestor.returncode == 1:
            continue
        ancestor.check_returncode()
        distance = int(git(directory, "rev-list", "--count", f"{commit}..{current}"))
        candidates.append((distance, name))
    return min(candidates)[1] if candidates else None


def markdown(text):
    return re.sub(r"([\\`*_{}\[\]<>()#!|])", r"\\\1", text)


def commit_notes(directory, repository, tag, previous):
    current = f"refs/tags/{tag}"
    revision = f"refs/tags/{previous}..{current}" if previous else current
    output = git(directory, "log", "--reverse", "-z", "--format=%H%x00%s%x00%an", revision)
    fields = output.rstrip("\0").split("\0") if output else []
    base = f"https://github.com/{repository}"
    heading = f"All commits since {markdown(previous)}" if previous else "All commits (first release)"
    lines = [f"## {heading}", ""]
    for index in range(0, len(fields), 3):
        sha, subject, author = fields[index:index + 3]
        lines.append(f"- [{sha[:7]}]({base}/commit/{sha}) {markdown(subject)} — {markdown(author)}")
    if previous:
        lines += ["", f"[Full comparison]({base}/compare/{quote(previous, safe='')}...{quote(tag, safe='')})"]
    return "\n".join(lines)


def generate_notes(directory, repository, tag):
    pages = github("--paginate", "--slurp", f"repos/{repository}/releases?per_page=100")
    releases = [release for page in pages for release in page]
    previous = previous_release(directory, tag, releases)
    args = [f"repos/{repository}/releases/generate-notes", "-f", f"tag_name={tag}"]
    if previous:
        args += ["-f", f"previous_tag_name={previous}"]
    generated = github(*args)["body"].strip()
    return generated + "\n\n" + commit_notes(directory, repository, tag, previous) + "\n"


if __name__ == "__main__":
    notes = generate_notes(Path.cwd(), os.environ["GITHUB_REPOSITORY"], os.environ["RELEASE_TAG"])
    Path("release-notes.md").write_text(notes, encoding="utf-8")
