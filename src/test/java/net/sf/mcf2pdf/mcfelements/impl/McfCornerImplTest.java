package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class McfCornerImplTest {

    private McfCornerImpl corner;

    @Before
    public void setUp() {
        corner = new McfCornerImpl();
    }

    @Test
    public void testLengthMethods() {
        corner.setLength(10);
        assertEquals(10, corner.getLength());
    }

    @Test
    @Ignore
    public void testShapeMethods() {
        corner.setShape("shape");
        assertEquals("shape", corner.getShape());
    }

    @Test
    public void testWhereMethods() {
        corner.setWhere("where");
        assertEquals("where", corner.getWhere());
    }
}