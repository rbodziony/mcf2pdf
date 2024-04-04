package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Color;
import net.sf.mcf2pdf.mcfelements.impl.McfPageNumImpl;
import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.impl.McfFotobookImpl;

public class McfPageNumImplTest {

    private McfPageNumImpl pageNum;
    private McfFotobook fotobook;

    @Before
    public void setUp() {
        pageNum = new McfPageNumImpl();
        fotobook = new McfFotobookImpl(); // Assuming McfFotobookImpl has a no-arg constructor
    }

    @Test
    public void testFotobookMethods() {
        pageNum.setFotobook(fotobook);
        assertEquals(fotobook, pageNum.getFotobook());
    }

    @Test
    public void testTextStringMethods() {
        pageNum.setTextString("textString");
        assertEquals("textString", pageNum.getTextString());
    }

    @Test
    public void testPositionMethods() {
        pageNum.setPosition(1);
        assertEquals(1, pageNum.getPosition());
    }

    @Test
    public void testVerticalMarginMethods() {
        pageNum.setVerticalMargin(10);
        assertEquals(10, pageNum.getVerticalMargin());
    }

    @Test
    public void testHorizontalMarginMethods() {
        pageNum.setHorizontalMargin(20);
        assertEquals(20, pageNum.getHorizontalMargin());
    }

    @Test
    public void testTextColorMethods() {
        Color color = new Color(255, 255, 255);
        pageNum.setTextColor(color);
        assertEquals(color, pageNum.getTextColor());
    }

    @Test
    public void testBgColorMethods() {
        Color color = new Color(255, 255, 255);
        pageNum.setBgColor(color);
        assertEquals(color, pageNum.getBgColor());
    }

    @Test
    public void testFontSizeMethods() {
        pageNum.setFontSize(1.5f);
        assertEquals(1.5f, pageNum.getFontSize(), 0.001);
    }

    @Test
    public void testFontBoldMethods() {
        pageNum.setFontBold(1);
        assertEquals(1, pageNum.getFontBold());
        assertTrue(pageNum.getFBold());
        pageNum.setFontBold(0);
        assertEquals(0, pageNum.getFontBold());
        assertFalse(pageNum.getFBold());
    }

    @Test
    public void testFontItalicsMethods() {
        pageNum.setFontItalics(1);
        assertEquals(1, pageNum.getFontItalics());
        assertTrue(pageNum.getFItalics());
        pageNum.setFontItalics(0);
        assertEquals(0, pageNum.getFontItalics());
        assertFalse(pageNum.getFItalics());
    }

    @Test
    public void testFontFamilyMethods() {
        pageNum.setFontFamily("fontFamily");
        assertEquals("fontFamily", pageNum.getFontFamily());
    }

    @Test
    public void testFormatMethods() {
        pageNum.setFormat(1);
        assertEquals(1, pageNum.getFormat());
    }
}