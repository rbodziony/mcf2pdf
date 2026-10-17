package net.sf.mcf2pdf.mcfelements.impl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
public class McfCutoutImplTest {
    private McfCutoutImpl cutout;

    @Before
    public void setUp() {
        cutout = new McfCutoutImpl();
    }

    @Test
    public void testScaleMethods() {
        cutout.setScale(1.5f);
        assertEquals(1.5f, cutout.getScale(), 0.001);
    }

    @Test
    public void testLeftMethods() {
        cutout.setLeft(10.0f);
        assertEquals(10.0f, cutout.getLeft(), 0.001);
    }

    @Test
    public void testTopMethods() {
        cutout.setTop(20.0f);
        assertEquals(20.0f, cutout.getTop(), 0.001);
    }
}