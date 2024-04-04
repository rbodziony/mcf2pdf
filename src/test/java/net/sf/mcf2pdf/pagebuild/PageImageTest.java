package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfImage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import java.awt.Point;

public class PageImageTest {

    @Test
    public void testGetLeftMM() {
        McfImage image = Mockito.mock(McfImage.class);
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(image.getArea()).thenReturn(area);
        Mockito.when(area.getLeft()).thenReturn(100f);
        PageImage pageImage = new PageImage(image);
        Assert.assertEquals(10.0f, pageImage.getLeftMM(), 0.01f);
    }

    @Test
    public void testGetTopMM() {
        McfImage image = Mockito.mock(McfImage.class);
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(image.getArea()).thenReturn(area);
        Mockito.when(area.getTop()).thenReturn(200f);
        PageImage pageImage = new PageImage(image);
        Assert.assertEquals(20.0f, pageImage.getTopMM(), 0.01f);
    }

    @Test
    public void testGetZPosition() {
        McfImage image = Mockito.mock(McfImage.class);
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(image.getArea()).thenReturn(area);
        Mockito.when(area.getZPosition()).thenReturn(300);
        PageImage pageImage = new PageImage(image);
        Assert.assertEquals(300, pageImage.getZPosition());
    }

    @Test
    public void testIsVectorGraphic() {
        McfImage image = Mockito.mock(McfImage.class);
        PageImage pageImage = new PageImage(image);
        Assert.assertFalse(pageImage.isVectorGraphic());
    }

    @Test
    @Ignore //TODO add PageRenderContext
    public void testRenderAsBitmap() throws IOException {
        McfImage image = Mockito.mock(McfImage.class);
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(image.getArea()).thenReturn(area);
        Mockito.when(area.getWidth()).thenReturn(400f);
        Mockito.when(area.getHeight()).thenReturn(500f);
        Mockito.when(image.getFileName()).thenReturn("");
        PageImage pageImage = new PageImage(image);
        //PageRenderContext context = Mockito.mock(PageRenderContext.class);
      //  Mockito.when(context.toPixel(Mockito.anyFloat())).thenReturn(600);
        Point drawOffsetPixels = new Point();
       // BufferedImage result = pageImage.renderAsBitmap(context, drawOffsetPixels, 700, 800);
       // Assert.assertNotNull(result);
       // Assert.assertEquals(600, result.getWidth());
        //Assert.assertEquals(600, result.getHeight());
    }
}
