package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class McfClipartImplTest {

    private McfClipartImpl clipart;

    @Before
    public void setUp() {
        clipart = new McfClipartImpl();
    }

    @Test
    public void testContentType() {
        assertEquals(McfAreaContent.ContentType.CLIPART, clipart.getContentType());
    }

    @Test
    public void testUniqueNameMethods() {
        clipart.setUniqueName("uniqueName");
        assertEquals("uniqueName", clipart.getUniqueName());
    }

    @Test
    public void testDesignElementIdMethods() {
        clipart.setDesignElementId("designElementId");
        assertEquals("designElementId", clipart.getDesignElementId());
    }
}