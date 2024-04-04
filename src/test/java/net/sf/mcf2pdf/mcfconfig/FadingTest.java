package net.sf.mcf2pdf.mcfconfig;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FadingTest {

    private Fading fading;

    @Before
    public void setUp() {
        fading = new Fading();
    }

    @Test
    public void testSetAndGetFile() {
        String file = "testFile";
        fading.setFile(file);
        assertEquals(file, fading.getFile());
    }

    @Test
    public void testSetAndGetDesignElementType() {
        String designElementType = "testDesignElementType";
        fading.setDesignElementType(designElementType);
        assertEquals(designElementType, fading.getDesignElementType());
    }

    @Test
    public void testSetAndGetDesignElementId() {
        String designElementId = "testDesignElementId";
        fading.setDesignElementId(designElementId);
        assertEquals(designElementId, fading.getDesignElementId());
    }

    @Test
    public void testSetAndGetRatio() {
        double ratio = 1.0;
        fading.setRatio(ratio);
        assertEquals(ratio, fading.getRatio(), 0.01);
    }

    @Test
    public void testSetAndGetKeepAspectRatio() {
        double keepAspectRatio = 1.0;
        fading.setKeepAspectRatio(keepAspectRatio);
        assertEquals(keepAspectRatio, fading.getKeepAspectRatio(), 0.01);
    }

    @Test
    public void testSetAndGetClipart() {
        Clipart clipart = new Clipart();
        fading.setClipart(clipart);
        assertEquals(clipart, fading.getClipart());
    }

    @Test
    public void testSetAndGetFotoarea() {
        Fotoarea fotoarea = new Fotoarea();
        fading.setFotoarea(fotoarea);
        assertEquals(fotoarea, fading.getFotoarea());
    }
}