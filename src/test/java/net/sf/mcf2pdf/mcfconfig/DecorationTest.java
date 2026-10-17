package net.sf.mcf2pdf.mcfconfig;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DecorationTest {

    private Decoration decoration;

    @Before
    public void setUp() {
        decoration = new Decoration();
    }

    @Test
    public void testSetAndGetId() {
        String id = "testId";
        decoration.setId(id);
        assertEquals(id, decoration.getId());
    }

    @Test
    public void testSetAndGetType() {
        String type = "testType";
        decoration.setType(type);
        assertEquals(type, decoration.getType());
    }

    @Test
    public void testSetAndGetDesignElementId() {
        String designElementId = "testDesignElementId";
        decoration.setDesignElementId(designElementId);
        assertEquals(designElementId, decoration.getDesignElementId());
    }

    @Test
    public void testSetAndGetFading() {
        Fading fading = new Fading();
        decoration.setFading(fading);
        assertEquals(fading, decoration.getFading());
    }

    @Test
    public void testSetAndGetClipart() {
        Clipart clipart = new Clipart();
        decoration.setClipart(clipart);
        assertEquals(clipart, decoration.getClipart());
    }
}