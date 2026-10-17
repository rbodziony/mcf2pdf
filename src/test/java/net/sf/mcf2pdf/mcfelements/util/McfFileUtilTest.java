package net.sf.mcf2pdf.mcfelements.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.FileNotFoundException;
import net.sf.mcf2pdf.mcfelements.util.McfFileUtil;
import net.sf.mcf2pdf.mcfelements.McfFotobook;

public class McfFileUtilTest {

    private McfFotobook fotobook;

    @Before
    public void setUp() {
        fotobook = mock(McfFotobook.class);
        when(fotobook.getFile()).thenReturn(new File("./src/test/resources/fotobook.mcf"));
        when(fotobook.getImageDir()).thenReturn("./src/test/resources");
    }

    @Test
    public void testGetImageFileInFotobookDirectory() throws FileNotFoundException {
        File imageFile = McfFileUtil.getImageFile("Bertrand1-figure.png", fotobook);
        assertEquals(new File("./src/test/resources/Bertrand1-figure.png"), imageFile);
    }

    @Test
    public void testGetImageFileInCurrentDirectory() throws FileNotFoundException {
        File imageFile = McfFileUtil.getImageFile("Bertrand1-figure-withres.png", fotobook);
        assertEquals(new File("./src/test/resources/Bertrand1-figure-withres.png"), imageFile);
    }

    @Test
    public void testGetImageFileInParentDirectory() throws FileNotFoundException {
        File imageFile = McfFileUtil.getImageFile("Bertrand1-figure.png", fotobook);
        assertEquals(new File("./src/test/resources/Bertrand1-figure.png"), imageFile);
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetImageFileNotFound() throws FileNotFoundException {
        McfFileUtil.getImageFile("nonexistent.png", fotobook);
    }

    @Test
    public void testGetImageFileWithSafeContainerPrefix() throws FileNotFoundException {
        File imageFile = McfFileUtil.getImageFile("safecontainer:Bertrand1-figure.png", fotobook);
        assertEquals(new File("./src/test/resources/Bertrand1-figure.png"), imageFile);
    }
}
