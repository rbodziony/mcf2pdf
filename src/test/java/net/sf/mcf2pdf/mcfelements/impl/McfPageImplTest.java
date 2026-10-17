package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfBackground;
import net.sf.mcf2pdf.mcfelements.impl.McfAreaImpl;
import net.sf.mcf2pdf.mcfelements.impl.McfBackgroundImpl;
import net.sf.mcf2pdf.mcfelements.impl.McfPageImpl;

public class McfPageImplTest {

    private McfPageImpl page;
    private McfArea area;
    private McfBackground background;

    @Before
    public void setUp() {
        page = new McfPageImpl();
        area = new McfAreaImpl(); // Assuming McfAreaImpl has a no-arg constructor
        background = new McfBackgroundImpl(); // Assuming McfBackgroundImpl has a no-arg constructor
    }

    @Test
    public void testAddAndGetArea() {
        page.addArea(area);
        List<? extends McfArea> areas = page.getAreas();
        assertTrue(areas.contains(area));
    }

    @Test
    public void testAddAndGetBackground() {
        page.addBackground(background);
        List<McfBackground> backgrounds = page.getBackgrounds();
        assertTrue(backgrounds.contains(background));
    }

    @Test
    public void testFotobookMethods() {
        McfFotobookImpl fotobook = new McfFotobookImpl(); // Assuming McfFotobookImpl has a no-arg constructor
        page.setFotobook(fotobook);
        assertEquals(fotobook, page.getFotobook());
    }

    @Test
    public void testPageNrMethods() {
        page.setPageNr(1);
        assertEquals(1, page.getPageNr());
    }

    @Test
    public void testTypeMethods() {
        page.setType("type");
        assertEquals("type", page.getType());
    }

    @Test
    public void testBundlesizeMethods() {
        McfBundlesizeImpl bundlesize = new McfBundlesizeImpl(); // Assuming McfBundlesizeImpl has a no-arg constructor
        page.setBundlesize(bundlesize);
        assertEquals(bundlesize, page.getBundlesize());
    }
}
