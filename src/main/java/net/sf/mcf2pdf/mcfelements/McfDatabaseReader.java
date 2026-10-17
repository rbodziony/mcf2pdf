/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;

/**
 * Database reader for the application's custom XML-only MCF SQLite archive.
 * Native CEWE .mcfx projects use a different schema and are extracted by
 * {@link McfxProject}.
 */
public class McfDatabaseReader {
    
    private static final String SELECT_MCF_CONTENT = "SELECT xml_content FROM mcf_files WHERE filename = ?";
    private static final String SELECT_ALL_FILES = "SELECT filename FROM mcf_files ORDER BY filename";
    
    private final Connection connection;
    private final File databaseFile;
    
    /**
     * Creates a new database reader for the given SQLite database file.
     * 
     * @param databaseFile The custom SQLite archive file
     * @throws SQLException If database connection fails
     * @throws IOException If database file doesn't exist or can't be accessed
     */
    public McfDatabaseReader(File databaseFile) throws SQLException, IOException {
        if (!databaseFile.exists()) {
            throw new IOException("Database file does not exist: " + databaseFile.getAbsolutePath());
        }
        if (!databaseFile.canRead()) {
            throw new IOException("Cannot read database file: " + databaseFile.getAbsolutePath());
        }
        
        this.databaseFile = databaseFile;
        String jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        this.connection = DriverManager.getConnection(jdbcUrl);
        
        // Verify database structure
        if (!isDatabaseValid()) {
            throw new SQLException("Invalid custom MCF archive. Expected table 'mcf_files' with columns 'filename' and 'xml_content'");
        }
    }
    
    /**
     * Checks if the database has the required structure.
     */
    private boolean isDatabaseValid() throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, "mcf_files", null)) {
            if (!rs.next()) {
                return false;
            }
        }
        
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, "mcf_files", null)) {
            boolean hasFilename = false;
            boolean hasXmlContent = false;
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                if ("filename".equals(columnName)) {
                    hasFilename = true;
                } else if ("xml_content".equals(columnName)) {
                    hasXmlContent = true;
                }
            }
            
            return hasFilename && hasXmlContent;
        }
    }
    
    /**
     * Reads an MCF file from the database and creates an MCF DOM.
     * 
     * @param filename The filename of the MCF file to read (without path)
     * @param configurator The digester configurator to use for parsing
     * @return A Fotobook object reflecting the contents of the file
     * @throws IOException If any I/O related problem occurs reading from database
     * @throws SAXException If any XML related problem occurs parsing the content
     * @throws SQLException If any database access error occurs
     */
    public McfFotobook readFotobook(String filename, DigesterConfigurator configurator) 
            throws IOException, SAXException, SQLException {
        
        String xmlContent = getMcfXmlContent(filename);
        if (xmlContent == null) {
            throw new IOException("MCF file not found in database: " + filename);
        }
        
        Digester digester = new Digester();
        configurator.configureDigester(digester, new File(filename)); // Use filename for configuration
        McfFotobook fb = digester.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
        
        // Set the file reference
        try {
            org.apache.commons.beanutils.BeanUtils.setProperty(fb, "file", new File(filename));
        } catch (Exception e) {
            // ignore - digester configurator will (hopefully) do it anyhow
        }
        
        return fb;
    }
    
    /**
     * Gets the XML content of an MCF file from the database.
     * 
     * @param filename The filename to retrieve
     * @return The XML content as a string, or null if not found
     * @throws SQLException If database access fails
     */
    public String getMcfXmlContent(String filename) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_MCF_CONTENT)) {
            stmt.setString(1, filename);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("xml_content");
                }
                return null;
            }
        }
    }
    
    /**
     * Lists all MCF files available in the database.
     * 
     * @return Array of filenames stored in the database
     * @throws SQLException If database access fails
     */
    public String[] listAvailableFiles() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_FILES);
             ResultSet rs = stmt.executeQuery()) {
            
            java.util.List<String> filenames = new java.util.ArrayList<>();
            while (rs.next()) {
                filenames.add(rs.getString("filename"));
            }
            
            return filenames.toArray(new String[0]);
        }
    }
    
    /**
     * Checks if a specific MCF file exists in the database.
     * 
     * @param filename The filename to check
     * @return True if the file exists in the database
     * @throws SQLException If database access fails
     */
    public boolean fileExists(String filename) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM mcf_files WHERE filename = ?")) {
            stmt.setString(1, filename);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * Gets the database file associated with this reader.
     * 
     * @return The database file
     */
    public File getCurrentDatabaseFile() {
        return databaseFile;
    }
    
    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // Ignore closing errors
        }
    }
}
