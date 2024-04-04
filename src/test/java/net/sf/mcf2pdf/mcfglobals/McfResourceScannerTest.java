package net.sf.mcf2pdf.mcfglobals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class McfResourceScannerTest {

    private McfResourceScanner scanner;
    private File mockFile;

    @Before
    public void setUp() {
        mockFile = Mockito.mock(File.class);
        List<File> scanDirs = Arrays.asList(mockFile);
        scanner = new McfResourceScanner(scanDirs);
    }

    @Test
    public void testScanDirectory() throws IOException {
        when(mockFile.isDirectory()).thenReturn(true).thenReturn(false);
        when(mockFile.listFiles()).thenReturn(new File[]{mockFile});
        when(mockFile.getName()).thenReturn("123132.jpg");
        when(mockFile.getAbsolutePath()).thenReturn("/path/to/123132.jpg");

        scanner.scan();

        assertTrue(scanner.getImage("123132") !=null);
    }

    @Test
    public void testScanDirectoryWithUnsupportedFile() throws IOException {
        when(mockFile.isDirectory()).thenReturn(false);
        when(mockFile.listFiles()).thenReturn(new File[]{mockFile});
        when(mockFile.getName()).thenReturn("test.unsupported");
        when(mockFile.getAbsolutePath()).thenReturn("/path/to/test.unsupported");

        scanner.scan();

        assertTrue(scanner.getImage("22222")==null);
    }
    @Test
    public void testcheckCLP() throws IOException {
        when(mockFile.isDirectory()).thenReturn(true).thenReturn(false);
        when(mockFile.listFiles()).thenReturn(new File[]{mockFile});
        when(mockFile.getName()).thenReturn("123132.clp");
        when(mockFile.getAbsolutePath()).thenReturn("/path/to/123132.clp");

        scanner.scan();

        assertTrue(scanner.getClip("123132") !=null);
    }


    @Test
    @Ignore // add mocking or  xml file to test it
    public void testcheckOneColorBackgrounds() throws IOException {
        when(mockFile.isDirectory()).thenReturn(true).thenReturn(false);
        when(mockFile.listFiles()).thenReturn(new File[]{mockFile});
        when(mockFile.getName()).thenReturn("1_color_backgrounds.xml");
        when(mockFile.getAbsolutePath()).thenReturn("/path/to/1_color_backgrounds.xml");

        scanner.scan();

        assertTrue(scanner.getColorImage("123132") !=null);
    }
    // Add more tests here for other file types and scenarios
}
