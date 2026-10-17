/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for creating and managing the application's custom MCF SQLite
 * archive format. These files are not native CEWE .mcfx projects.
 * This class provides methods to create databases, add MCF files, and manage
 * the database structure.
 */
public class McfDatabaseManager {
    
    private final Connection connection;
    private final File databaseFile;
    
    /**
     * Creates a new database manager for the given SQLite database file.
     * If the database doesn't exist, it will be created.
     * 
     * @param databaseFile The SQLite database file, typically with a .mcfdb extension
     * @throws SQLException If database connection fails
     * @throws IOException If database file can't be created
     */
    public McfDatabaseManager(File databaseFile) throws SQLException, IOException {
        this.databaseFile = databaseFile;
        
        // Create database file if it doesn't exist
        if (!databaseFile.exists()) {
            File parentDir = databaseFile.getAbsoluteFile().getParentFile();
            if (parentDir != null && !parentDir.isDirectory() && !parentDir.mkdirs()) {
                throw new IOException("Could not create database directory: " + parentDir.getAbsolutePath());
            }
        }
        
        String jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        this.connection = DriverManager.getConnection(jdbcUrl);
        
        // Create table structure if needed
        createTableStructure();
    }
    
    /**
     * Creates the necessary table structure for storing MCF files.
     */
    private void createTableStructure() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS mcf_files (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "filename TEXT NOT NULL UNIQUE, " +
                         "xml_content TEXT NOT NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                         ")");
            
            // Create index for faster filename lookups
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_filename ON mcf_files(filename)");
        }
    }
    
    /**
     * Adds an MCF file to the database.
     * 
     * @param mcfFile The MCF file to add
     * @param filename The name to store the file under in the database
     * @throws IOException If the MCF file can't be read
     * @throws SQLException If database operation fails
     */
    public void addMcfFile(File mcfFile, String filename) throws IOException, SQLException {
        if (!mcfFile.exists()) {
            throw new IOException("MCF file does not exist: " + mcfFile.getAbsolutePath());
        }
        
        String xmlContent = new String(Files.readAllBytes(mcfFile.toPath()), "UTF-8");
        addMcfContent(filename, xmlContent);
    }
    
    /**
     * Adds MCF XML content directly to the database.
     * 
     * @param filename The name to store the content under
     * @param xmlContent The XML content of the MCF file
     * @throws SQLException If database operation fails
     */
    public void addMcfContent(String filename, String xmlContent) throws SQLException {
        String sql = "INSERT OR REPLACE INTO mcf_files (filename, xml_content, updated_at) " +
                    "VALUES (?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, filename);
            stmt.setString(2, xmlContent);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Removes an MCF file from the database.
     * 
     * @param filename The filename to remove
     * @return True if the file was removed, false if it didn't exist
     * @throws SQLException If database operation fails
     */
    public boolean removeMcfFile(String filename) throws SQLException {
        String sql = "DELETE FROM mcf_files WHERE filename = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, filename);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Lists all MCF files in the database.
     * 
     * @return Array of filenames stored in the database
     * @throws SQLException If database operation fails
     */
    public String[] listFiles() throws SQLException {
        String sql = "SELECT filename FROM mcf_files ORDER BY filename";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            java.util.List<String> filenames = new java.util.ArrayList<>();
            while (rs.next()) {
                filenames.add(rs.getString("filename"));
            }
            
            return filenames.toArray(new String[0]);
        }
    }
    
    /**
     * Gets the XML content of an MCF file from the database.
     * 
     * @param filename The filename to retrieve
     * @return The XML content, or null if not found
     * @throws SQLException If database operation fails
     */
    public String getMcfContent(String filename) throws SQLException {
        String sql = "SELECT xml_content FROM mcf_files WHERE filename = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, filename);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("xml_content");
                }
                return null;
            }
        }
    }
    
    /**
     * Checks if a file exists in the database.
     * 
     * @param filename The filename to check
     * @return True if the file exists in the database
     * @throws SQLException If database operation fails
     */
    public boolean fileExists(String filename) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mcf_files WHERE filename = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, filename);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * Gets the number of files stored in the database.
     * 
     * @return The number of files
     * @throws SQLException If database operation fails
     */
    public int getFileCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM mcf_files")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Gets the database file.
     * 
     * @return The database file
     */
    public File getDatabaseFile() {
        return databaseFile;
    }
    
    /**
     * Shows the table structure of the database.
     * 
     * @throws SQLException If database access fails
     */
    public void showTableStructure() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Database table structure:");
            System.out.println();
            
            // Get table schema
            java.sql.ResultSet rs = stmt.executeQuery(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name='mcf_files'");
            
            if (rs.next()) {
                String createSql = rs.getString("sql");
                System.out.println("  Table: mcf_files");
                System.out.println("  CREATE SQL: " + createSql);
                System.out.println();
                
                // Get column information
                java.sql.ResultSet columns = stmt.executeQuery("PRAGMA table_info(mcf_files)");
                System.out.println("  Columns:");
                while (columns.next()) {
                    String columnName = columns.getString("name");
                    String columnType = columns.getString("type");
                    System.out.println("    " + columnName + " (" + columnType + ")");
                }
            }
            
            // Get row count
            java.sql.ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM mcf_files");
            if (countRs.next()) {
                int rowCount = countRs.getInt(1);
                System.out.println("  Row count: " + rowCount);
            }
            
        } catch (SQLException e) {
            System.err.println("Error showing table structure: " + e.getMessage());
        }
    }
    
    /**
     * Queries a specific table and displays all its rows.
     * 
     * @param tableName The name of the table to query
     * @throws SQLException If database access fails
     */
    public void queryTable(String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Querying table: " + tableName);
            System.out.println();
            
            // Check if table exists
            java.sql.ResultSet tableCheck = stmt.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            
            if (!tableCheck.next()) {
                System.out.println("Table '" + tableName + "' does not exist in database.");
                
                // Show available tables
                java.sql.ResultSet tables = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
                System.out.println("Available tables:");
                while (tables.next()) {
                    System.out.println("  " + tables.getString("name"));
                }
                return;
            }
            
            // Get column information
            java.sql.ResultSet columns = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
            java.util.List<String> columnNames = new java.util.ArrayList<>();
            System.out.println("Columns:");
            while (columns.next()) {
                String columnName = columns.getString("name");
                String columnType = columns.getString("type");
                columnNames.add(columnName);
                System.out.println("  " + columnName + " (" + columnType + ")");
            }
            System.out.println();
            
            // Query all rows
            java.sql.ResultSet rows = stmt.executeQuery("SELECT * FROM " + tableName);
            
            if (!rows.next()) {
                System.out.println("No rows found in table '" + tableName + "'.");
                return;
            }
            
            // Display rows
            int rowCount = 0;
            do {
                rowCount++;
                System.out.println("Row " + rowCount + ":");
                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
                    String value = rows.getString(i + 1);
                    if (rows.wasNull()) {
                        value = "NULL";
                    } else if (value.length() > 100) {
                        value = value.substring(0, 100) + "...";
                    }
                    System.out.println("  " + columnName + ": " + value);
                }
                System.out.println();
            } while (rows.next());
            
            System.out.println("Total rows: " + rowCount);
            
        } catch (SQLException e) {
            System.err.println("Error querying table: " + e.getMessage());
        }
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
    
    /**
     * Creates a new database and adds all MCF files from a directory.
     * 
     * @param directory The directory containing MCF files
     * @param databaseFile The database file to create
     * @param recursive Whether to search recursively
     * @return The number of files added
     * @throws IOException If directory access fails
     * @throws SQLException If database operations fail
     */
    public static int createFromDirectory(File directory, File databaseFile, boolean recursive) 
            throws IOException, SQLException {
        
        if (!directory.isDirectory()) {
            throw new IOException("Not a directory: " + directory.getAbsolutePath());
        }
        
        McfDatabaseManager manager = new McfDatabaseManager(databaseFile);
        int fileCount = 0;
        
        try {
            java.util.List<File> mcfFiles = findMcfFiles(directory, recursive);
            for (File mcfFile : mcfFiles) {
                String filename = mcfFile.getName();
                manager.addMcfFile(mcfFile, filename);
                fileCount++;
            }
        } finally {
            manager.close();
        }
        
        return fileCount;
    }
    
    /**
     * Finds all MCF files in a directory.
     */
    private static java.util.List<File> findMcfFiles(File directory, boolean recursive) {
        java.util.List<File> files = new java.util.ArrayList<>();
        findMcfFilesRecursive(directory, files, recursive);
        return files;
    }
    
    private static void findMcfFilesRecursive(File dir, java.util.List<File> files, boolean recursive) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isFile() && child.getName().toLowerCase().endsWith(".mcf")) {
                    files.add(child);
                } else if (child.isDirectory() && recursive) {
                    findMcfFilesRecursive(child, files, recursive);
                }
            }
        }
    }
}
