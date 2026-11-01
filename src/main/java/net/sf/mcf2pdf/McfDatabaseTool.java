/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import net.sf.mcf2pdf.mcfelements.McfDatabaseManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Command-line tool for managing the application's custom MCF SQLite archives.
 * This tool allows users to create, populate, and manage databases containing
 * MCF XML documents. This format is not compatible with native CEWE .mcfx projects.
 */
public class McfDatabaseTool {
    
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        Options options = new Options();
        
        options.addOption("h", false, "Prints this help and exits.");
        options.addOption("c", false, "Create a new database.");
        options.addOption("a", true, "Add an MCF file to the database.");
        options.addOption("d", true, "Directory containing MCF files to add (use with -r for recursive).");
        options.addOption("r", false, "Process directory recursively.");
        options.addOption("l", false, "List all files in the database.");
        options.addOption("x", true, "Remove a file from the database.");
        options.addOption("g", true, "Get XML content of a file from database.");
        options.addOption("s", false, "Show database statistics.");
        options.addOption("t", false, "Show table structure.");
        options.addOption("table", true, "Query specific table and show all rows.");
        
        CommandLine cl;
        try {
            CommandLineParser parser = new PosixParser();
            cl = parser.parse(options, args);
        }
        catch (ParseException pe) {
            printUsage(options, pe);
            System.exit(3);
            return;
        }
        
        if (cl.hasOption("h")) {
            printUsage(options, null);
            return;
        }
        
        if (cl.getArgs().length != 1) {
            printUsage(options, new ParseException("Database file must be specified as the only argument."));
            System.exit(3);
            return;
        }
        
        File databaseFile = new File(cl.getArgs()[0]);
        if (!databaseFile.getName().toLowerCase().endsWith(".mcfdb")) {
            printUsage(options, new ParseException("Custom archive file must have .mcfdb extension."));
            System.exit(3);
            return;
        }
        
        McfDatabaseManager manager = null;
        try {
            // Create database if requested
            if (cl.hasOption("c")) {
                System.out.println("Creating database: " + databaseFile.getAbsolutePath());
                manager = new McfDatabaseManager(databaseFile);
                System.out.println("Database created successfully.");
            }
            
            // Open existing database
            if (manager == null) {
                if (!databaseFile.exists()) {
                    System.err.println("Database file does not exist. Use -c to create it first.");
                    System.exit(1);
                    return;
                }
                manager = new McfDatabaseManager(databaseFile);
            }
            
            // Add single file
            if (cl.hasOption("a")) {
                File mcfFile = new File(cl.getOptionValue("a"));
                String filename = mcfFile.getName();
                manager.addMcfFile(mcfFile, filename);
                System.out.println("Added file: " + filename);
            }
            
            // Add directory
            if (cl.hasOption("d")) {
                File dir = new File(cl.getOptionValue("d"));
                boolean recursive = cl.hasOption("r");
                int count = 0;
                
                if (recursive) {
                    System.out.println("Adding MCF files from directory recursively: " + dir.getAbsolutePath());
                } else {
                    System.out.println("Adding MCF files from directory: " + dir.getAbsolutePath());
                }
                
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".mcf")) {
                            manager.addMcfFile(file, file.getName());
                            count++;
                            System.out.println("  Added: " + file.getName());
                        } else if (file.isDirectory() && recursive) {
                            count += addDirectoryRecursive(manager, file, "  ");
                        }
                    }
                }
                
                System.out.println("Added " + count + " files to database.");
            }
            
            // List files
            if (cl.hasOption("l")) {
                String[] files = manager.listFiles();
                System.out.println("Files in database (" + files.length + "):");
                for (String file : files) {
                    System.out.println("  " + file);
                }
            }
            
            // Remove file
            if (cl.hasOption("x")) {
                String filename = cl.getOptionValue("x");
                boolean removed = manager.removeMcfFile(filename);
                if (removed) {
                    System.out.println("Removed file: " + filename);
                } else {
                    System.out.println("File not found: " + filename);
                }
            }
            
            // Get file content
            if (cl.hasOption("g")) {
                String filename = cl.getOptionValue("g");
                String content = manager.getMcfContent(filename);
                if (content != null) {
                    System.out.println("Content of " + filename + ":");
                    System.out.println(content);
                } else {
                    System.out.println("File not found: " + filename);
                }
            }
            
            // Show statistics
            if (cl.hasOption("s")) {
                int fileCount = manager.getFileCount();
                System.out.println("Database statistics:");
                System.out.println("  Database file: " + databaseFile.getAbsolutePath());
                System.out.println("  File count: " + fileCount);
                System.out.println("  Database size: " + formatFileSize(databaseFile.length()));
                
                if (fileCount == 0) {
                    System.out.println("  No files found in database.");
                }
            }
            
            // Show table structure
            if (cl.hasOption("t")) {
                try {
                    manager.showTableStructure();
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                }
            }
            
            // Query specific table
            if (cl.hasOption("table")) {
                try {
                    String tableName = cl.getOptionValue("table");
                    manager.queryTable(tableName);
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (manager != null) {
                manager.close();
            }
        }
    }
    
    private static int addDirectoryRecursive(McfDatabaseManager manager, File dir, String indent) 
            throws IOException, SQLException {
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".mcf")) {
                    manager.addMcfFile(file, file.getName());
                    count++;
                    System.out.println(indent + "Added: " + file.getName());
                } else if (file.isDirectory()) {
                    count += addDirectoryRecursive(manager, file, indent + "  ");
                }
            }
        }
        return count;
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    private static void printUsage(Options options, ParseException pe) {
        if (pe != null)
            System.err.println("ERROR: " + pe.getMessage());
        System.out.println();
        System.out.println("mcf2pdf Database Tool - Manage custom XML-only MCF archives (.mcfdb)");
        System.out.println();
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("mcfdbtool <options> DATABASE_FILE.mcfdb", "Options:", options,
                "\nExamples:\n" +
                "  Create a new database:\n" +
                "    mcfdbtool -c mydatabase.mcfdb\n\n" +
                "  Add a single MCF file:\n" +
                "    mcfdbtool -a myfile.mcf mydatabase.mcfdb\n\n" +
                "  Add all MCF files from a directory:\n" +
                "    mcfdbtool -d /path/to/mcf/files mydatabase.mcfdb\n\n" +
                "  Add files recursively:\n" +
                "    mcfdbtool -d /path/to/mcf/files -r mydatabase.mcfdb\n\n" +
                "  List all files in database:\n" +
                "    mcfdbtool -l mydatabase.mcfdb\n\n" +
                "  Show database statistics:\n" +
                "    mcfdbtool -s mydatabase.mcfdb\n\n" +
                "  Show table structure:\n" +
                "    mcfdbtool -t mydatabase.mcfdb\n\n" +
                "  Query specific table:\n" +
                "    mcfdbtool -table mcf_files mydatabase.mcfdb\n\n" +
                "  Note: This custom format is not a native CEWE .mcfx project and cannot be used as one.");
    }
}
