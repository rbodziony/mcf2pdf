package net.sf.mcf2pdf.mcfconfig;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClipartTest {

    private Clipart clipart;

    @Before
    public void setUp() {
        clipart = new Clipart();
    }

    @Test
    public void testSetAndGetFile() {
        String file = "testFile";
        clipart.setFile(file);
        assertEquals(file, clipart.getFile());
    }

    @Test
    public void testSetAndGetDesignElementType() {
        String designElementType = "testDesignElementType";
        clipart.setDesignElementType(designElementType);
        assertEquals(designElementType, clipart.getDesignElementType());
    }

    @Test
    public void testSetAndGetRatio() {
        double ratio = 1.0;
        clipart.setRatio(ratio);
        assertEquals(ratio, clipart.getRatio(), 0.01);
    }

    @Test
    public void testSetAndGetDesignElementId() {
        String designElementId = "testDesignElementId";
        clipart.setDesignElementId(designElementId);
        assertEquals(designElementId, clipart.getDesignElementId());
    }
}