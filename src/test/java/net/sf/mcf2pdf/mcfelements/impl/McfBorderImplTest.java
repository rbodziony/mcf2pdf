package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Test;
import java.awt.Color;
import static org.junit.Assert.*;

public class McfBorderImplTest {

    private McfBorderImpl border;

    @Before
    public void setUp() {
        border = new McfBorderImpl();
    }

    @Test
    public void testColorMethods() {
        Color color = new Color(255, 0, 0);
        border.setColor(color);
        assertEquals(color, border.getColor());
    }

    @Test
    public void testOffsetMethods() {
        border.setOffset(10.0f);
        assertEquals(10.0f, border.getOffset(), 0.001);
    }

    @Test
    public void testWidthMethods() {
        border.setWidth(20.0f);
        assertEquals(20.0f, border.getWidth(), 0.001);
    }

    @Test
    public void testEnabledMethods() {
        border.setEnabled(true);
        assertTrue(border.isEnabled());
    }
}
