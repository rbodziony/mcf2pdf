package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import net.sf.mcf2pdf.mcfelements.impl.McfImageBackgroundImpl;

public class McfImageBackgroundImplTest {

    private McfImageBackgroundImpl imageBackground;

    @Before
    public void setUp() {
        imageBackground = new McfImageBackgroundImpl();
    }

    @Test
    public void testContentType() {
        assertEquals(McfAreaContent.ContentType.IMAGEBACKGROUND, imageBackground.getContentType());
    }

    @Test
    public void testBackgroundPositionMethods() {
        imageBackground.setBackgroundPosition("backgroundPosition");
        assertEquals("backgroundPosition", imageBackground.getBackgroundPosition());
    }
}