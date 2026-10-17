package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import net.sf.mcf2pdf.mcfelements.impl.McfPositionImpl;

public class McfPositionImplTest {

    private McfPositionImpl position;

    @Before
    public void setUp() {
        position = new McfPositionImpl();
    }

    @Test
    public void testContentType() {
        assertEquals(McfAreaContent.ContentType.POSITION, position.getContentType());
    }

    @Test
    public void testLeftMethods() {
        position.setLeft(1.0f);
        assertEquals(1.0f, position.getLeft(), 0.001);
    }

    @Test
    public void testTopMethods() {
        position.setTop(2.0f);
        assertEquals(2.0f, position.getTop(), 0.001);
    }

    @Test
    public void testWidthMethods() {
        position.setWidth(3.0f);
        assertEquals(3.0f, position.getWidth(), 0.001);
    }

    @Test
    public void testHeightMethods() {
        position.setHeight(4.0f);
        assertEquals(4.0f, position.getHeight(), 0.001);
    }

    @Test
    public void testRotationMethods() {
        position.setRotation(5.0f);
        assertEquals(5.0f, position.getRotation(), 0.001);
    }

    @Test
    public void testZPositionMethods() {
        position.setZPosition(6);
        assertEquals(6, position.getZPosition());
    }
}
