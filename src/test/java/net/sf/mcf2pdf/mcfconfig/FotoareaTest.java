package net.sf.mcf2pdf.mcfconfig;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FotoareaTest {

    private Fotoarea fotoarea;

    @Before
    public void setUp() {
        fotoarea = new Fotoarea();
    }

    @Test
    public void testSetAndGetX() {
        double x = 10.0;
        fotoarea.setX(x);
        assertEquals(x, fotoarea.getX(), 0.01);
    }

    @Test
    public void testSetAndGetY() {
        double y = 20.0;
        fotoarea.setY(y);
        assertEquals(y, fotoarea.getY(), 0.01);
    }

    @Test
    public void testSetAndGetHeight() {
        double height = 30.0;
        fotoarea.setHeight(height);
        assertEquals(height, fotoarea.getHeight(), 0.01);
    }

    @Test
    public void testSetAndGetWidth() {
        double width = 40.0;
        fotoarea.setWidth(width);
        assertEquals(width, fotoarea.getWidth(), 0.01);
    }
}
