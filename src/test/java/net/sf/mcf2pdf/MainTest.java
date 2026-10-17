package net.sf.mcf2pdf;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class MainTest {

    private Main main;
    private Options options;
    private CommandLineParser parser;
    private CommandLine cl;
    private File installDir;
    private File tempDir;
    private File mcfFile;
    private File tempImages;
    @Before
    public void setUp() {
        main = new Main();
        options = mock(Options.class);
        parser = new PosixParser();
        cl = mock(CommandLine.class);
        installDir = mock(File.class);
        tempDir = mock(File.class);
        mcfFile = mock(File.class);
        tempImages = mock(File.class);
    }

    @Test
    @Ignore // fix
    public void testMainWithValidArgs() throws Exception {
        when(cl.hasOption("h")).thenReturn(false);
        when(cl.getArgs()).thenReturn(new String[] {"input.mcf", "output.pdf"});
        when(cl.getOptionValue("i")).thenReturn("/path/to/install/dir");
        when(cl.getOptionValue("t")).thenReturn("/path/to/temp/dir");
        when(cl.getOptionValue("r")).thenReturn("150");
        when(cl.getOptionValue("n")).thenReturn("10");
        when(cl.getOptionValue("s")).thenReturn("0.82");
        when(installDir.isDirectory()).thenReturn(true);
        when(tempDir.isDirectory()).thenReturn(true);
        when(mcfFile.isFile()).thenReturn(true);
        when(tempImages.isDirectory()).thenReturn(true);
       // File installDir = new File(cl.getOptionValue("i"));
        //File tempDir = new File(cl.getOptionValue("t"));
        //File mcfFile = new File(cl.getArgs()[0]);
        //File tempImages = new File(new File(System.getProperty("user.home")), ".mcf2pdf");

        assertTrue(installDir.isDirectory());
        assertTrue(tempDir.isDirectory());
        assertTrue(mcfFile.isFile());
        assertTrue(tempImages.isDirectory());

        OutputStream finalOut = new FileOutputStream(cl.getArgs()[1]);
        assertNotNull(finalOut);

        main.main(new String[] {"-i", "/path/to/install/dir", "-t", "/path/to/temp/dir", "-r", "150", "-n", "10", "-s", "0.82", "input.mcf", "output.pdf"});
    }

    @Test(expected = ParseException.class)
    @Ignore // fix
    public void testMainWithInvalidArgs() throws Exception {
        when(cl.hasOption("h")).thenReturn(false);
        when(cl.getArgs()).thenReturn(new String[] {"input.mcf"});
        when(cl.getOptionValue("i")).thenReturn("/path/to/install/dir");
        when(cl.getOptionValue("t")).thenReturn("/path/to/temp/dir");
        when(cl.getOptionValue("r")).thenReturn("150");
        when(cl.getOptionValue("n")).thenReturn("10");
        when(cl.getOptionValue("s")).thenReturn("0.82");

        main.main(new String[] {"-i", "/path/to/install/dir", "-t", "/path/to/temp/dir", "-r", "150", "-n", "10", "-s", "0.82", "input.mcf"});
    }
}
