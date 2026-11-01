# Native CEWE `.mcfx` Support Plan

## Goal

Convert a native CEWE `.mcfx` project to PDF by reading its embedded `.mcf` and
all associated files, while preserving the existing `.mcf` conversion path.
The target is the SQLite container format used by CEWE, not a new application-
specific database format.

## Findings before native extraction was implemented

The present uncommitted implementation does **not** yet read native CEWE
`.mcfx` files:

- `McfDatabaseReader` expects a custom `mcf_files(filename, xml_content)`
  table and reads the MCF as text. The supplied CEWE sample instead has
  `Files(Filename TEXT PRIMARY KEY, Data BLOB, LastModified INTEGER)`.
- The supplied sample contains one MCF and associated image/resource BLOBs.
  Its MCF BLOB has bytes after `</fotobook>`, so passing the entire BLOB
  directly to the current XML parser is not sufficient.
- The current reader only returns the MCF XML; it does not materialize the
  assets. `Mcf2FoConverter` and `McfFileUtil` resolve resources from filesystem
  paths relative to the MCF's file location, so rendering needs a temporary
  filesystem representation of the full project.
- The original `testdb/*.mcfdb` files exercise only the custom `mcf_files` schema.
  There are no tests for the native `Files` schema or its BLOB entries.
- `McfDatabaseManager` and `McfDatabaseTool` create and manage the custom
  schema, which is a separate feature from reading existing CEWE projects.
  Its parent-directory creation logic is also incorrect and should not be
  carried into the native reader.
- The current command line requires `-f` for every `.mcfx`, although native
  CEWE projects are expected to contain one `.mcf` entry. File selection
  should follow the actual container contract and provide clear errors for
  unexpected ambiguity.
- `SQLITE_DATABASE_SUPPORT.md` documents the custom schema as if it were the
  `.mcfx` format and must be corrected if that management feature remains.

The inspection of the user-provided sample was read-only. Its contents are
personal project data and should not be committed as a test fixture.

## Reference behavior

The `cewe2pdf` README describes `.mcfx` as a SQLite database containing one
`.mcf` plus related image files. Its `mcfx.py` reads `Filename`, `Data`, and
`LastModified` from `Files`, writes the entries into a temporary project
directory, uses the extracted `.mcf` with the existing conversion pipeline,
and removes the temporary directory when conversion completes. It also trims
trailing bytes after the MCF's closing `</fotobook>` element.

Reference sources:

- [cewe2pdf album-file documentation](https://github.com/bash0/cewe2pdf#album-files)
- [cewe2pdf `mcfx.py`](https://github.com/bash0/cewe2pdf/blob/master/mcfx.py)
- [cewe2pdf extraction tests](https://github.com/bash0/cewe2pdf/blob/master/tests/testMcfxExtraction/test_McfxExtraction.py)

Use these as behavioral references only; implement independently and do not
copy source code.

The reference extraction suite compares the extracted MCF and associated
assets with originals, checks workspace behavior, and creates a small SQLite
database to verify nonnumeric `LastModified` handling. The Java tests follow
those behavioral goals with temporary native-schema databases generated during
the test run, rather than checking in a large or personal `.mcfx` file.

## Implemented

- Added `McfxProject` to open native SQLite databases read-only, validate the
  `Files(Filename, Data, LastModified)` schema, extract embedded files safely
  into a temporary workspace, trim MCF bytes after `</fotobook>`, and clean the
  workspace on close or extraction failure.
- The converter now keeps this workspace alive for the full parse/render pass,
  so the existing image/resource path resolution can access extracted assets.
- `-f` is optional when one MCF exists and selects an entry only when needed.
- The prior XML-only database manager is clearly separated as custom `.mcfdb`
  tooling, not native CEWE `.mcfx`; the support document now explains the
  distinction.
- Added `McfxProjectTest` coverage for MCF/assets extraction, binary data,
  cleanup, trailing payload, nonnumeric timestamps, selection/ambiguity,
  traversal, malformed MCF, and invalid schema.

## Remaining validation

- A full conversion against a representative project should still be performed
  to validate PDF output and resource lookup beyond extraction.
- Perform a manual conversion of the supplied native sample without checking
  the sample or extracted album data into Git.

## Current worktree review and validation

- The current branch is `feature/addingmcxsupport`; feature work is uncommitted.
- The pre-implementation test suite passed on JDK 25: 222 tests, 0 failures,
  0 errors, 19 skipped.
- Focused JUnit extraction tests pass: 10 tests, 0 failures/errors.
- The full JDK 25 Maven suite passes: 232 tests, 0 failures/errors, 19 skipped.
- `mvn package -DskipTests` succeeds, and the Windows archive contains both
  `sqlite-jdbc-3.45.1.0.jar` and the compiled `McfxProject` class.
- The worktree also contains IDE/classpath files, custom-schema tooling and
  fixtures, a local batch file, and personal sample data. Review and stage
  these independently; do not treat them all as required native-format
  support.
