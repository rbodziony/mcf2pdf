package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class McfBackgroundImplTest {

    private McfBackgroundImpl background;
    private McfPageImpl page;

    @Before
    public void setUp() {
        background = new McfBackgroundImpl();
        page = new McfPageImpl(); // Assuming McfPage has a no-arg constructor
    }

    @Test
    public void testPageMethods() {
        background.setPage(page);
        assertEquals(page, background.getPage());
    }

    @Test
    public void testTemplateNameMethods() {
        background.setTemplateName("template");
        assertEquals("template", background.getTemplateName());
    }

    @Test
    public void testDesignElementIdMethods() {
        background.setDesignElementId("elementId");
        assertEquals("elementId", background.getDesignElementId());
    }

    @Test
    public void testTypeMethods() {
        background.setType(1);
        assertEquals(1, background.getType());
    }

    @Test
    public void testLayoutMethods() {
        background.setLayout(2);
        assertEquals(2, background.getLayout());
    }

    @Test
    public void testHueMethods() {
        background.setHue(3);
        assertEquals(3, background.getHue());
    }

    @Test
    public void testFadingMethods() {
        background.setFading(4);
        assertEquals(4, background.getFading());
    }

    @Test
    public void testRotationMethods() {
        background.setRotation(5);
        assertEquals(5, background.getRotation());
    }
}
