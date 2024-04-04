package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import net.sf.mcf2pdf.mcfelements.impl.McfCornersImpl;
import net.sf.mcf2pdf.mcfelements.McfCorner;

public class McfCornersImplTest {

    private McfCornersImpl cornersImpl;
    private McfCornerImpl corner;

    @Before
    public void setUp() {
        cornersImpl = new McfCornersImpl();
        corner = new McfCornerImpl(); // Assuming McfCorner has a no-arg constructor
    }

    @Test
    public void testAddCorner() {
        cornersImpl.addCorner(corner);
        List<? extends McfCorner> corners = cornersImpl.getCorners();
        assertTrue(corners.contains(corner));
    }

    @Test
    public void testGetCorners() {
        cornersImpl.addCorner(corner);
        List<? extends McfCorner> corners = cornersImpl.getCorners();
        assertEquals(1, corners.size());
        assertEquals(corner, corners.get(0));
    }
}
