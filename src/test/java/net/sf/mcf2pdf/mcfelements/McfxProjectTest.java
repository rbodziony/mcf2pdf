package net.sf.mcf2pdf.mcfelements;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class McfxProjectTest {

	private static final byte[] MCF_XML =
			"<?xml version=\"1.0\"?><fotobook></fotobook>".getBytes(StandardCharsets.UTF_8);

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void extractsMcfAndBinaryAssetsAndRemovesWorkspaceOnClose() throws Exception {
		File database = createNativeDatabase("INTEGER");
		byte[] mcfWithTrailingData = append(MCF_XML, new byte[] { 0, 1, 2, 3 });
		byte[] image = new byte[] { 0, (byte) 0xff, 23, 42 };
		insert(database, "data.mcf", mcfWithTrailingData, 10L);
		insert(database, "data_mcf-Dateien/nested/photo.jpg", image, 10L);

		Path workspace;
		try (McfxProject project = McfxProject.extract(database, null)) {
			Path mcfPath = project.getMcfFile().toPath();
			workspace = mcfPath.getParent();
			assertEquals("data.mcf", mcfPath.getFileName().toString());
			assertArrayEquals(MCF_XML, Files.readAllBytes(mcfPath));
			assertArrayEquals(image, Files.readAllBytes(
					workspace.resolve("data_mcf-Dateien/nested/photo.jpg")));
		}
		assertFalse(Files.exists(workspace));
	}

	@Test
	public void extractsWhenLastModifiedIsNotNumeric() throws Exception {
		File database = createNativeDatabase("BLOB");
		byte[] timestamp = "not-a-timestamp".getBytes(StandardCharsets.UTF_8);
		insert(database, "data.mcf", MCF_XML, timestamp);

		try (McfxProject project = McfxProject.extract(database, null)) {
			assertArrayEquals(MCF_XML, Files.readAllBytes(project.getMcfFile().toPath()));
		}
	}

	@Test
	public void explicitEntrySelectsMcfWhenDatabaseContainsMoreThanOne() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "first.mcf", MCF_XML, 1L);
		insert(database, "second.mcf", MCF_XML, 1L);

		try (McfxProject project = McfxProject.extract(database, "second.mcf")) {
			assertEquals("second.mcf", project.getMcfFile().getName());
		}
	}

	@Test
	public void rejectsMultipleMcfEntriesWithoutSelection() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "first.mcf", MCF_XML, 1L);
		insert(database, "second.mcf", MCF_XML, 1L);

		assertExtractionFails(database, null, "Multiple .mcf entries");
	}

	@Test
	public void rejectsPathTraversalEntries() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "../outside.mcf", MCF_XML, 1L);

		assertExtractionFails(database, null, "Unsafe relative path");
	}

	@Test
	public void rejectsWindowsAlternateDataStreamPaths() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "assets/photo.jpg:stream", new byte[] { 1 }, 1L);
		insert(database, "data.mcf", MCF_XML, 1L);

		assertExtractionFails(database, null, "Unsafe path");
	}

	@Test
	public void rejectsMissingMcfEntry() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "assets/photo.jpg", new byte[] { 1 }, 1L);

		assertExtractionFails(database, null, "No .mcf entry");
	}

	@Test
	public void rejectsEntryWithMissingData() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "data.mcf", null, 1L);

		assertExtractionFails(database, null, "Missing file data");
	}

	@Test
	public void rejectsMcfWithoutClosingFotobookElement() throws Exception {
		File database = createNativeDatabase("INTEGER");
		insert(database, "data.mcf", "<fotobook>".getBytes(StandardCharsets.UTF_8), 1L);

		assertExtractionFails(database, null, "closing </fotobook>");
	}

	@Test
	public void rejectsDatabaseWithoutNativeFilesTable() throws Exception {
		File database = temporaryFolder.newFile("invalid.mcfx");
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
			 Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE mcf_files (filename TEXT, xml_content TEXT)");
		}

		assertExtractionFails(database, null, "Files(Filename, Data, LastModified)");
	}

	private File createNativeDatabase(String lastModifiedType) throws Exception {
		File database = temporaryFolder.newFile("sample-" + System.nanoTime() + ".mcfx");
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
			 Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE Files (Filename TEXT PRIMARY KEY, Data BLOB, LastModified "
					+ lastModifiedType + ")");
		}
		return database;
	}

	private void insert(File database, String fileName, byte[] data, Object lastModified) throws Exception {
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
			 PreparedStatement statement = connection.prepareStatement("INSERT INTO Files VALUES (?, ?, ?)")) {
			statement.setString(1, fileName);
			statement.setBytes(2, data);
			statement.setObject(3, lastModified);
			statement.executeUpdate();
		}
	}

	private void assertExtractionFails(File database, String selectedMcf, String expectedMessage) throws Exception {
		Path workspaceParent = Files.createDirectory(
				temporaryFolder.getRoot().toPath().resolve("workspace-" + System.nanoTime()));
		try (McfxProject ignored = McfxProject.extract(database, selectedMcf, workspaceParent)) {
			fail("Expected extraction to fail with: " + expectedMessage);
		} catch (IOException e) {
			assertTrue("Unexpected error: " + e.getMessage(), e.getMessage().contains(expectedMessage));
		}
		try (Stream<Path> remaining = Files.list(workspaceParent)) {
			assertFalse("Failed extraction must clean up its temporary workspace", remaining.findAny().isPresent());
		}
	}

	private static byte[] append(byte[] first, byte[] second) {
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
}
