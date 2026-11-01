from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

from release_notes import commit_notes, generate_notes, previous_release


class ReleaseNotesTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repo = Path(self.temp.name)
        self.git("init", "-b", "main")
        self.git("config", "user.name", "Test Author")
        self.git("config", "user.email", "test@example.com")
        # Ignore signing and hooks from the developer's machine.
        self.git("config", "commit.gpgsign", "false")
        self.git("config", "tag.gpgsign", "false")
        self.git("config", "core.hooksPath", str(self.repo / "no-hooks"))
        self.first = self.commit("First commit")
        self.git("tag", "v1.0.0")
        self.second = self.commit("Direct push without PR")
        self.git("tag", "unreleased-tag")
        self.third = self.commit("Fix [crop] & *border*")
        self.git("tag", "v1.1.0")

    def git(self, *args):
        return subprocess.check_output(["git", *args], cwd=self.repo, text=True, encoding="utf-8").strip()

    def commit(self, subject):
        self.git("commit", "--allow-empty", "-m", subject)
        return self.git("rev-parse", "HEAD")

    def release(self, tag, draft=False):
        return {"tag_name": tag, "draft": draft}

    def test_all_direct_commits_included_and_previous_history_excluded(self):
        notes = commit_notes(self.repo, "owner/repo", "v1.1.0", "v1.0.0")
        self.assertIn(self.second, notes)
        self.assertIn(self.third, notes)
        self.assertNotIn(self.first, notes)
        self.assertIn("Direct push without PR", notes)
        self.assertIn(r"Fix \[crop\] & \*border\*", notes)
        self.assertIn("Test Author", notes)
        self.assertIn("/compare/v1.0.0...v1.1.0", notes)

    def test_first_release_includes_entire_history(self):
        notes = commit_notes(self.repo, "owner/repo", "v1.1.0", None)
        for sha in (self.first, self.second, self.third):
            self.assertIn(sha, notes)
        self.assertIn("first release", notes)

    def test_only_published_releases_determine_previous_version(self):
        releases = [self.release("unreleased-tag", draft=True), self.release("v1.0.0")]
        self.assertEqual("v1.0.0", previous_release(self.repo, "v1.1.0", releases))

    def test_rerun_ignores_current_and_future_release(self):
        self.commit("Later change")
        self.git("tag", "v1.2.0")
        releases = [self.release(tag) for tag in ("v1.2.0", "v1.1.0", "v1.0.0")]
        self.assertEqual("v1.0.0", previous_release(self.repo, "v1.1.0", releases))

    def test_no_releases_and_same_commit_alias_mean_first_release(self):
        self.git("tag", "alias")
        self.assertIsNone(previous_release(self.repo, "v1.1.0", []))
        self.assertIsNone(previous_release(self.repo, "v1.1.0", [self.release("alias")]))

    def test_merge_includes_commits_from_both_branches(self):
        self.git("checkout", "-b", "feature", "v1.0.0")
        feature = self.commit("Feature branch change")
        self.git("checkout", "main")
        self.git("merge", "--no-ff", "feature", "-m", "Merge feature")
        self.git("tag", "v1.2.0")
        notes = commit_notes(self.repo, "owner/repo", "v1.2.0", "v1.0.0")
        for sha in (feature, self.second, self.third):
            self.assertIn(sha, notes)
        self.assertIn("Merge feature", notes)

    @patch("release_notes.github")
    def test_generated_pr_notes_and_commit_list_use_same_previous_release(self, api):
        api.side_effect = [[[self.release("v1.0.0")]], {"body": "## What's Changed\n- PR #123"}]
        notes = generate_notes(self.repo, "owner/repo", "v1.1.0")
        self.assertIn("PR #123", notes)
        self.assertIn(self.second, notes)
        self.assertNotIn(self.first, notes)
        self.assertIn("previous_tag_name=v1.0.0", api.call_args.args)


if __name__ == "__main__":
    unittest.main()
