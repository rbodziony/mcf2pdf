# Native CEWE `.mcfx` Support

Native CEWE `.mcfx` projects are SQLite databases containing the MCF document
and the files referenced by that document. The reader supports the CEWE
`Files(Filename, Data, LastModified)` table and does not modify the source
database.

## Converting a project

The MCF entry is selected automatically when the database contains one `.mcf`
file:

```text
mcf2pdf -i "C:\path\to\CEWE" "photobook.mcfx" "photobook.pdf"
```

If a database contains more than one MCF document, select one explicitly with
`-f`:

```text
mcf2pdf -i "C:\path\to\CEWE" -f "data.mcf" "photobook.mcfx" "photobook.pdf"
```

The database entries are extracted into a temporary workspace for the
conversion. The MCF and its assets remain available throughout rendering, and
the workspace is removed when conversion succeeds or fails. Bytes after the
MCF's closing `</fotobook>` element are ignored.

The existing `.mcf` conversion path is unchanged. Native `.mcfx` input requires
a readable SQLite database with a `Files` table containing `Filename`, `Data`,
and `LastModified` columns. The reader rejects unsafe embedded paths, missing
file data, missing MCF documents, and ambiguous MCF documents without `-f`.

## Tests

`McfxProjectTest` creates small native-format SQLite fixtures during the test
run. It covers extraction of the MCF and binary assets, removal of the
temporary workspace, trailing MCF payload, non-numeric timestamps, explicit
selection of multiple MCF entries, missing data/MCF entries, unsafe paths,
malformed MCF content, and invalid database schemas. No personal album files
are required.

## Custom XML-only archive utility

`mcfdbtool` creates and manages an application-specific XML-only SQLite
archive, named with a `.mcfdb` extension. Its `mcf_files(filename, xml_content)`
schema is **not** the native CEWE `.mcfx` format and is not accepted as input to
the PDF converter. Do not rename a `.mcfdb` file to `.mcfx`.

Examples:

```text
mcfdbtool -c collection.mcfdb
mcfdbtool -a photobook.mcf collection.mcfdb
mcfdbtool -l collection.mcfdb
```
