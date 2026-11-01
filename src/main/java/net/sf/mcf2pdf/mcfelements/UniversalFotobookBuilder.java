/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;

import org.xml.sax.SAXException;

/**
 * MCF DOM builder for disk files and the application's custom XML-only
 * SQLite archive. Native CEWE .mcfx projects should be extracted with
 * {@link McfxProject} and parsed through {@link FotobookBuilder}.
 */
public final class UniversalFotobookBuilder {
	
	private DigesterConfigurator configurator;
	private McfDatabaseReader databaseReader;
	
	/**
	 * Creates a new universal MCF DOM builder with default settings.
	 */
	public UniversalFotobookBuilder() {
		this(new DigesterConfiguratorImpl());
	}
	
	/**
	 * Creates a new universal MCF DOM builder which uses the given configurator to configure
	 * the XML Digester to use. This way, you can use your own API or add some
	 * preprocessing to the parsing process.
	 * 
	 * @param configurator Configurator to use to configure the internal Digester
	 * object.
	 */
	public UniversalFotobookBuilder(DigesterConfigurator configurator) {
		this.configurator = configurator;
	}
	
	/**
	 * Reads the given MCF input and creates an MCF DOM reflecting the
	 * relevant parts of the content. Automatically detects if input is a file
	 * (.mcf) or custom SQLite archive and handles accordingly.
	 *  
	 * @param input MCF input file (.mcf) or custom archive (.mcfdb)
	 * 
	 * @return A Fotobook object reflecting the contents of the input.
	 * 
	 * @throws IOException If any I/O related problem occurs reading the input.
	 * @throws SAXException If any XML related problem occurs reading the content.
	 * @throws SQLException If any database related problem occurs.
	 */
	public McfFotobook readFotobook(File input) throws IOException, SAXException, SQLException {
		return readFotobook(input, input.getName());
	}
	
	/**
	 * Reads the given MCF input and creates an MCF DOM reflecting the
	 * relevant parts of the content. Automatically detects if input is a file
	 * (.mcf) or custom SQLite archive and handles accordingly.
	 *  
	 * @param input MCF input file (.mcf) or custom SQLite archive
	 * @param filename The specific filename to read from the custom archive
	 * 
	 * @return A Fotobook object reflecting the contents of the input.
	 * 
	 * @throws IOException If any I/O related problem occurs reading the input.
	 * @throws SAXException If any XML related problem occurs reading the content.
	 * @throws SQLException If any database related problem occurs.
	 */
	public McfFotobook readFotobook(File input, String filename) throws IOException, SAXException, SQLException {
		if (input.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".mcfdb")) {
			return readFromDatabase(input, filename);
		} else {
			return readFromFile(input);
		}
	}
	
	/**
	 * Reads MCF content from a disk file.
	 */
	private McfFotobook readFromFile(File file) throws IOException, SAXException {
		FotobookBuilder fileBuilder = new FotobookBuilder(configurator);
		return fileBuilder.readFotobook(file);
	}
	
	/**
	 * Reads MCF content from a SQLite database.
	 */
	private McfFotobook readFromDatabase(File databaseFile, String filename) throws IOException, SAXException, SQLException {
		if (databaseReader == null || !databaseFile.equals(databaseReader.getCurrentDatabaseFile())) {
			if (databaseReader != null) {
				databaseReader.close();
			}
			databaseReader = new McfDatabaseReader(databaseFile);
		}
		
		return databaseReader.readFotobook(filename, configurator);
	}
	
	/**
	 * Lists all available MCF files in the custom archive.
	 * 
	 * @param databaseFile The custom .mcfdb file
	 * @return Array of filenames available in the database
	 * @throws SQLException If database access fails
	 * @throws IOException If the file is not a custom archive
	 */
	public String[] listAvailableFiles(File databaseFile) throws SQLException, IOException {
		if (!databaseFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".mcfdb")) {
			throw new IOException("Not a custom archive. Expected .mcfdb extension: " + databaseFile.getName());
		}
		
		if (databaseReader == null || !databaseFile.equals(databaseReader.getCurrentDatabaseFile())) {
			if (databaseReader != null) {
				databaseReader.close();
			}
			databaseReader = new McfDatabaseReader(databaseFile);
		}
		
		return databaseReader.listAvailableFiles();
	}
	
	/**
	 * Checks if a specific MCF file exists in the database.
	 * 
	 * @param databaseFile The custom .mcfdb file
	 * @param filename The filename to check
	 * @return True if the file exists in the database
	 * @throws SQLException If database access fails
	 * @throws IOException If the file is not a custom archive
	 */
	public boolean fileExists(File databaseFile, String filename) throws SQLException, IOException {
		if (!databaseFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".mcfdb")) {
			throw new IOException("Not a custom archive. Expected .mcfdb extension: " + databaseFile.getName());
		}
		
		if (databaseReader == null || !databaseFile.equals(databaseReader.getCurrentDatabaseFile())) {
			if (databaseReader != null) {
				databaseReader.close();
			}
			databaseReader = new McfDatabaseReader(databaseFile);
		}
		
		return databaseReader.fileExists(filename);
	}
	
	/**
	 * Gets the XML content of an MCF file from the database.
	 * 
	 * @param databaseFile The custom .mcfdb file
	 * @param filename The filename to retrieve
	 * @return The XML content as a string
	 * @throws SQLException If database access fails
	 * @throws IOException If the file is not a custom archive or doesn't exist
	 */
	public String getMcfXmlContent(File databaseFile, String filename) throws SQLException, IOException {
		if (!databaseFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".mcfdb")) {
			throw new IOException("Not a custom archive. Expected .mcfdb extension: " + databaseFile.getName());
		}
		
		if (databaseReader == null || !databaseFile.equals(databaseReader.getCurrentDatabaseFile())) {
			if (databaseReader != null) {
				databaseReader.close();
			}
			databaseReader = new McfDatabaseReader(databaseFile);
		}
		
		String content = databaseReader.getMcfXmlContent(filename);
		if (content == null) {
			throw new IOException("MCF file not found in database: " + filename);
		}
		return content;
	}
	
	/**
	 * Closes any open database connections.
	 */
	public void close() {
		if (databaseReader != null) {
			databaseReader.close();
			databaseReader = null;
		}
	}
}
