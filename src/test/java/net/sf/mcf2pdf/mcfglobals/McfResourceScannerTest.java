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

    @org.junit.Rule
    public org.junit.rules.TemporaryFolder tmp = new org.junit.rules.TemporaryFolder();

    @Test
    public void thumbnailsAndRefinementMasksDoNotReplaceTheBackground() throws IOException {
        File extra = tmp.newFolder("extra");
        File install = tmp.newFolder("install");
        File background = write(new File(install, "photofun/backgrounds/6731/6731/6731.jpg"));
        write(new File(install, "photofun/backgrounds/6731/6731/refinement/refinement-6731.jpg"));
        write(new File(install, "photofun/thumbnails/backgrounds/spotcolor/6731.webp"));
        write(new File(extra, "photofun/backgrounds/spotcolor/6731.webp"));
        File thumbnailOnly = write(new File(install, "photofun/thumbnails/backgrounds/6544.webp"));
        File thumbnailBeforeMask = write(new File(extra, "photofun/thumbnails/backgrounds/6600.webp"));
        write(new File(install, "photofun/thumbnails/backgrounds/spotcolor/6600.webp"));
        write(new File(extra, "photofun/thumbnails/backgrounds/6543.webp"));
        File laterThumbnail = write(new File(install, "photofun/thumbnails/backgrounds/multicolor/6543.webp"));

        McfResourceScanner real = new McfResourceScanner(Arrays.asList(extra, install));
        real.scan();

        assertEquals(background, real.getImage("6731"));
        assertEquals("A thumbnail is still better than nothing", thumbnailOnly, real.getImage("6544"));
        assertEquals("A thumbnail is better than a mask", thumbnailBeforeMask, real.getImage("6600"));
        assertEquals("Among thumbnails the last one scanned wins", laterThumbnail, real.getImage("6543"));
        assertTrue(McfResourceScanner.isPreviewOrMask("C:\\CEWE\\photofun\\thumbnails\\backgrounds\\1.webp"));
        assertFalse(McfResourceScanner.isPreviewOrMask("/cewe/photofun/backgrounds/multicolor/6544.webp"));
    }

    @Test
    public void backgroundTemplatesTellMulticolorAndTheirMulticolorImage() throws IOException {
        File hps = tmp.newFolder("hps");
        File install = tmp.newFolder("install-mc");
        File source = write(new File(hps, "photofun/backgrounds/9060/9060/9060-0-0.jpg"));
        java.nio.file.Files.write(new File(install, "backgrounds_default.xml").toPath(), ("<templates>"
                + "<template name=\"6909\" multicolor_id=\"9060\" multicolor=\"1\" designElementType=\"background\"/>"
                + "<template name=\"6000\" designElementType=\"background\"/></templates>").getBytes("UTF-8"));
        File addon = new File(hps, "addons/6540/backgrounds/v1/6540.xml");
        write(addon);
        java.nio.file.Files.write(addon.toPath(),
                "<templates><template name=\"6540\" designElementType=\"background\"/></templates>".getBytes("UTF-8"));
        File later = new File(hps, "photofun/backgrounds/6909/6909/6909.xml");
        write(later);
        java.nio.file.Files.write(later.toPath(),
                "<templates><template name=\"6909\" designElementType=\"background\"/></templates>".getBytes("UTF-8"));

        McfResourceScanner real = new McfResourceScanner(Arrays.asList(install, hps));
        real.scan();

        assertEquals(Boolean.TRUE, real.isMulticolorBackground("6909"));
        assertEquals("9060", real.getMulticolorSourceId("6909"));
        assertEquals(source, real.getImage("9060"));
        assertEquals(Boolean.FALSE, real.isMulticolorBackground("6540"));
        assertEquals(Boolean.FALSE, real.isMulticolorBackground("6000"));
        assertNull(real.isMulticolorBackground("1234"));
    }

    private static File write(File file) throws IOException {
        assertTrue(file.getParentFile().isDirectory() || file.getParentFile().mkdirs());
        java.nio.file.Files.write(file.toPath(), new byte[] { 1 });
        return file;
    }
}
