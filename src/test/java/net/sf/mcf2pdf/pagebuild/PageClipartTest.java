package net.sf.mcf2pdf.pagebuild;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Point;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import net.sf.mcf2pdf.mcfelements.McfClipart;
import net.sf.mcf2pdf.mcfelements.McfArea;

public class PageClipartTest {

    @Test
    public void testGetLeftMM() {
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(area.getLeft()).thenReturn(100f);
        McfClipart clipart = Mockito.mock(McfClipart.class);
        Mockito.when(clipart.getArea()).thenReturn(area);
        PageClipart pageClipart = new PageClipart(clipart);
        Assert.assertEquals(10.0f, pageClipart.getLeftMM(), 0.01f);
    }

    @Test
    public void testGetTopMM() {
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(area.getTop()).thenReturn(200f);
        McfClipart clipart = Mockito.mock(McfClipart.class);
        Mockito.when(clipart.getArea()).thenReturn(area);
        PageClipart pageClipart = new PageClipart(clipart);
        Assert.assertEquals(20.0f, pageClipart.getTopMM(), 0.01f);
    }

    @Test
    public void testIsVectorGraphic() {
        McfClipart clipart = Mockito.mock(McfClipart.class);
        PageClipart pageClipart = new PageClipart(clipart);
        Assert.assertTrue(pageClipart.isVectorGraphic());
    }

    @Test
    @Ignore
    public void testRenderAsBitmap() throws Exception {
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(area.getWidth()).thenReturn(1000f);
        Mockito.when(area.getHeight()).thenReturn(2000f);
        Mockito.when(area.getRotation()).thenReturn(0f);
        McfClipart clipart = Mockito.mock(McfClipart.class);
        Mockito.when(clipart.getArea()).thenReturn(area);
        Mockito.when(clipart.getUniqueName()).thenReturn("test");
        //PageRenderContext context = Mockito.mock(PageRenderContext.class);
       // Mockito.when(context.toPixel(Mockito.anyFloat())).thenReturn(100);
        File file = new File("test.clp");
       // Mockito.when(context.getClipart(Mockito.anyString())).thenReturn(file);
        PageClipart pageClipart = new PageClipart(clipart);
        //BufferedImage result = pageClipart.renderAsBitmap(context, new Point(), 100, 100);
        //Assert.assertNotNull(result);
    }

    @Test
    public void testGetZPosition() {
        McfArea area = Mockito.mock(McfArea.class);
        Mockito.when(area.getZPosition()).thenReturn(100);
        McfClipart clipart = Mockito.mock(McfClipart.class);
        Mockito.when(clipart.getArea()).thenReturn(area);
        PageClipart pageClipart = new PageClipart(clipart);
        Assert.assertEquals(100, pageClipart.getZPosition());
    }
}
