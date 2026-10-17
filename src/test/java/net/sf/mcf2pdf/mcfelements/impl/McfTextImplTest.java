package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import net.sf.mcf2pdf.mcfelements.impl.McfTextImpl;

public class McfTextImplTest {

    private McfTextImpl text;

    @Before
    public void setUp() {
        text = new McfTextImpl();
    }

    @Test
    public void testContentType() {
        assertEquals(McfAreaContent.ContentType.TEXT, text.getContentType());
    }

    @Test
    public void testSpineTextMethods() {
        text.setSpineText(true);
        assertTrue(text.isSpineText());
        text.setSpineText(false);
        assertFalse(text.isSpineText());
    }

    @Test
    public void testHtmlContentMethods() {
        text.setHtmlContent("htmlContent");
        assertEquals("htmlContent", text.getHtmlContent());
    }

    @Test
    public void testVerticalIndentMarginMethods() {
        text.setVerticalIndentMargin(1.0f);
        assertEquals(1.0f, text.getVerticalIndentMargin(), 0.001);
    }

    @Test
    public void testIndentMarginMethods() {
        text.setIndentMargin(2.0f);
        assertEquals(2.0f, text.getIndentMargin(), 0.001);
    }

    @Test
    public void testBackgroundColorAlphaMethods() {
        text.setBackgroundColorAlpha(255);
        assertEquals(255, text.getBackgroundColorAlpha());
    }
}