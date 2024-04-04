package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import java.awt.Point;
import java.net.URL;

import static org.mockito.Mockito.mock;

public class PageBindingTest {


    private McfResourceScanner resources;

    private McfAlbumType albumType;

    private PageRenderContext context;
    @Before
    public void setUp() throws IOException {
        resources = mock(McfResourceScanner.class);
        albumType = mock(McfAlbumType.class);
        context = new PageRenderContext(100, 1.0, resources, albumType);
    }
    @Test
    public void testRenderAsBitmap() throws IOException {
        URL resourceUrl = getClass().getClassLoader().getResource("Bertrand1-figure.png");
        if (resourceUrl == null) {
            throw new FileNotFoundException("Resource not found: " + "test.jpg");

        }
        File file = new File(resourceUrl.getFile());
        PageBinding pageBinding = new PageBinding(file);
        Point drawOffsetPixels = new Point();
        BufferedImage result = pageBinding.renderAsBitmap(context, drawOffsetPixels, 100, 100);
        Assert.assertNotNull(result);
        Assert.assertEquals(47, drawOffsetPixels.x);
        Assert.assertEquals(0, drawOffsetPixels.y);
    }

    @Test
    public void testGetZPosition() throws IOException {
        URL resourceUrl = getClass().getClassLoader().getResource("Bertrand1-figure.png");
        if (resourceUrl == null) {
            throw new FileNotFoundException("Resource not found: " + "test.jpg");

        }
        File file = new File(resourceUrl.getFile());
        PageBinding pageBinding = new PageBinding(file);
        int result = pageBinding.getZPosition();
        Assert.assertEquals(10000, result);
    }
}