package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class McfBundlesizeImplTest {

    private McfBundlesizeImpl bundleSize;
    private McfPageImpl page;

    @Before
    public void setUp() {
        bundleSize = new McfBundlesizeImpl();
        page = new McfPageImpl(); // Assuming McfPage has a no-arg constructor
    }

    @Test
    public void testPageMethods() {
        bundleSize.setPage(page);
        assertEquals(page, bundleSize.getPage());
    }

    @Test
    public void testHeightMethods() {
        bundleSize.setHeight(100);
        assertEquals(100, bundleSize.getHeight());
    }

    @Test
    public void testWidthMethods() {
        bundleSize.setWidth(200);
        assertEquals(200, bundleSize.getWidth());
    }
}