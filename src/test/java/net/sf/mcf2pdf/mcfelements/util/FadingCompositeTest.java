package net.sf.mcf2pdf.mcfelements.util;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.*;
import java.awt.image.*;
import net.sf.mcf2pdf.mcfelements.util.FadingComposite;

public class FadingCompositeTest {

    private FadingComposite fadingComposite;

    @Before
    public void setUp() {
        fadingComposite = FadingComposite.INSTANCE;
    }

    @Test
    public void testValidateColorModel() {
        ColorModel validColorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
        assertTrue(FadingComposite.validateColorModel(validColorModel));

        ColorModel invalidColorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00FF0000);
        assertFalse(FadingComposite.validateColorModel(invalidColorModel));
    }

    @Test(expected = RasterFormatException.class)
    public void testCreateContextWithInvalidColorModel() {
        ColorModel invalidColorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00FF0000);
        fadingComposite.createContext(invalidColorModel, invalidColorModel, null);
    }

    @Test
    public void testCreateContextWithValidColorModel() {
        ColorModel validColorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
        CompositeContext context = fadingComposite.createContext(validColorModel, validColorModel, null);
        assertNotNull(context);
    }

    @Test
    public void testCompose() {
        // Create a source image with a single pixel.
        BufferedImage src = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, new Color(255, 255, 255, 255).getRGB());

        // Create a destination image with a single pixel.
        BufferedImage dst = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        dst.setRGB(0, 0, new Color(0, 0, 0, 255).getRGB());

        // Apply the FadingComposite to the source image.
        Graphics2D g = dst.createGraphics();
        g.setComposite(fadingComposite);
        g.drawImage(src, 0, 0, null);
        g.dispose();

        // Check the resulting image.
        int result = dst.getRGB(0, 0);
        assertEquals(new Color(0, 0, 0, 0).getRGB(), result);
    }
}
