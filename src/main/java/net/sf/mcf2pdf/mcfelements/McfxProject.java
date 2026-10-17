/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sqlite.SQLiteConfig;

/**
 * Extracts a native CEWE SQLite project into a temporary directory for the
 * existing file-based MCF parser and resource resolver.
 */
public final class McfxProject implements AutoCloseable {

	private static final byte[] FOTOBOOK_END = "</fotobook>".getBytes(java.nio.charset.StandardCharsets.UTF_8);

	private final Path workspace;
	private final File mcfFile;

	private McfxProject(Path workspace, File mcfFile) {
		this.workspace = workspace;
		this.mcfFile = mcfFile;
	}

	public static McfxProject extract(File databaseFile, String selectedMcf) throws IOException {
		return extract(databaseFile, selectedMcf, Path.of(System.getProperty("java.io.tmpdir")));
	}

	static McfxProject extract(File databaseFile, String selectedMcf, Path temporaryParent) throws IOException {
		if (!databaseFile.isFile() || !databaseFile.canRead()) {
			throw new IOException("Cannot read .mcfx database: " + databaseFile.getAbsolutePath());
		}

		Path workspace = Files.createTempDirectory(temporaryParent, "mcf2pdf-mcfx-");
		try {
			File mcfFile = extractFiles(databaseFile, selectedMcf, workspace);
			return new McfxProject(workspace, mcfFile);
		} catch (IOException e) {
			deleteWorkspace(workspace, e);
			throw e;
		}
	}

	public File getMcfFile() {
		return mcfFile;
	}

	@Override
	public void close() throws IOException {
		deleteWorkspace(workspace, null);
	}

	private static File extractFiles(File databaseFile, String selectedMcf, Path workspace) throws IOException {
		SQLiteConfig config = new SQLiteConfig();
		config.setReadOnly(true);
		String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

		try (Connection connection = DriverManager.getConnection(url, config.toProperties())) {
			validateSchema(connection);
			List<Path> mcfFiles = new ArrayList<>();
			Set<String> extractedPaths = new HashSet<>();
			try (Statement statement = connection.createStatement();
				 ResultSet rows = statement.executeQuery(
						 "SELECT Filename, Data, LastModified FROM Files ORDER BY Filename")) {
				while (rows.next()) {
					String fileName = rows.getString("Filename");
					Path target = resolveTarget(workspace, fileName);
					String collisionKey = target.toString().toLowerCase(Locale.ROOT);
					if (!extractedPaths.add(collisionKey)) {
						throw new IOException("Duplicate or case-colliding file path in .mcfx: " + fileName);
					}

					if (isMcfFile(fileName)) {
						mcfFiles.add(target);
					}

					Files.createDirectories(target.getParent());
					try (InputStream data = rows.getBinaryStream("Data")) {
						if (data == null) {
							throw new IOException("Missing file data in .mcfx entry: " + fileName);
						}
						if (isMcfFile(fileName)) {
							writeMcf(data.readAllBytes(), target, fileName);
						} else {
							Files.copy(data, target);
						}
					}
				}
			}

			Path selected = selectMcf(mcfFiles, workspace, selectedMcf);
			return selected.toFile();
		} catch (SQLException e) {
			throw new IOException("Could not read native CEWE .mcfx database. Expected a Files table with Filename, Data, and LastModified columns.", e);
		}
	}

	private static void validateSchema(Connection connection) throws SQLException, IOException {
		Set<String> columns = new HashSet<>();
		try (Statement statement = connection.createStatement();
			 ResultSet result = statement.executeQuery("PRAGMA table_info('Files')")) {
			while (result.next()) {
				columns.add(result.getString("name").toLowerCase(Locale.ROOT));
			}
		}
		if (!columns.contains("filename") || !columns.contains("data") || !columns.contains("lastmodified")) {
			throw new IOException("Not a native CEWE .mcfx database: expected Files(Filename, Data, LastModified).");
		}
	}

	private static Path resolveTarget(Path workspace, String fileName) throws IOException {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IOException("The .mcfx database contains an empty filename.");
		}
		String normalizedName = fileName.replace('\\', '/');
		if (normalizedName.startsWith("/") || normalizedName.contains(":") || normalizedName.endsWith("/")) {
			throw new IOException("Unsafe path in .mcfx database: " + fileName);
		}
		for (String part : normalizedName.split("/")) {
			if (part.isEmpty() || ".".equals(part) || "..".equals(part)) {
				throw new IOException("Unsafe relative path in .mcfx database: " + fileName);
			}
		}

		Path relative;
		try {
			relative = Path.of(normalizedName);
		} catch (InvalidPathException e) {
			throw new IOException("Invalid path in .mcfx database: " + fileName, e);
		}
		Path target = workspace.resolve(relative).normalize();
		if (!target.startsWith(workspace)) {
			throw new IOException("Unsafe path in .mcfx database: " + fileName);
		}
		return target;
	}

	private static Path selectMcf(List<Path> mcfFiles, Path workspace, String selectedMcf) throws IOException {
		if (selectedMcf != null) {
			Path selectedPath = resolveTarget(workspace, selectedMcf);
			if (!mcfFiles.contains(selectedPath)) {
				throw new IOException("MCF entry not found in .mcfx database: " + selectedMcf);
			}
			return selectedPath;
		}
		if (mcfFiles.size() != 1) {
			throw new IOException(mcfFiles.isEmpty()
					? "No .mcf entry found in .mcfx database."
					: "Multiple .mcf entries found in .mcfx database; specify one with -f.");
		}
		return mcfFiles.get(0);
	}

	private static boolean isMcfFile(String fileName) {
		return fileName.toLowerCase(Locale.ROOT).endsWith(".mcf");
	}

	private static void writeMcf(byte[] data, Path target, String fileName) throws IOException {
		int end = indexOf(data, FOTOBOOK_END);
		if (end < 0) {
			throw new IOException("MCF entry does not contain a closing </fotobook> element: " + fileName);
		}
		try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			output.write(data, 0, end + FOTOBOOK_END.length);
		}
	}

	private static int indexOf(byte[] data, byte[] value) {
		for (int i = 0; i <= data.length - value.length; i++) {
			int j = 0;
			while (j < value.length && data[i + j] == value[j]) {
				j++;
			}
			if (j == value.length) {
				return i;
			}
		}
		return -1;
	}

	private static void deleteWorkspace(Path workspace, IOException originalFailure) throws IOException {
		IOException cleanupFailure = null;
		if (Files.exists(workspace)) {
			try (java.util.stream.Stream<Path> paths = Files.walk(workspace)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
					try {
						Files.deleteIfExists(path);
					} catch (IOException e) {
						if (cleanupFailure == null) {
							cleanupFailure = e;
						} else {
							cleanupFailure.addSuppressed(e);
						}
					}
				}
			}
		}
		if (cleanupFailure != null) {
			if (originalFailure != null) {
				originalFailure.addSuppressed(cleanupFailure);
			} else {
				throw cleanupFailure;
			}
		}
	}
}
