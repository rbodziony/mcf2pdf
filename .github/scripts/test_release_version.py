from pathlib import Path
import tempfile
import unittest

from release_version import release_version


class ReleaseVersionTest(unittest.TestCase):
    def check(self, version, tag):
        with tempfile.TemporaryDirectory() as directory:
            pom = Path(directory) / "pom.xml"
            pom.write_text(
                '<project xmlns="http://maven.apache.org/POM/4.0.0">'
                '<parent><version>99.0.0</version></parent>'
                f'<version>{version}</version></project>',
                encoding="utf-8",
            )
            return release_version(pom, tag)

    def test_accepts_existing_and_maven_release_tag_formats(self):
        for tag in ("0.7.6", "v0.7.6", "mcf2pdf-0.7.6"):
            with self.subTest(tag=tag):
                self.assertEqual("0.7.6", self.check("0.7.6", tag))

    def test_snapshots_never_publish(self):
        for version in ("0.7.6-SNAPSHOT", "0.7.6-snapshot"):
            with self.subTest(version=version):
                self.assertIsNone(self.check(version, "0.7.6"))

    def test_tag_version_mismatch_fails(self):
        for tag in ("0.7.5", "v0.7.7", "unrelated", ""):
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                self.check("0.7.6", tag)

    def test_missing_or_unresolved_project_version_fails(self):
        for version in ("", "${revision}"):
            with self.subTest(version=version), self.assertRaises(ValueError):
                self.check(version, "0.7.6")


if __name__ == "__main__":
    unittest.main()
