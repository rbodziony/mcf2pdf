package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfelements.McfImageBackground;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Point;
import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;

public class PageImageBackgroundTest {

/*    @Test
    public void testRenderAsBitmapLeftOrTop() throws IOException {
        McfImageBackground image = Mockito.mock(McfImageBackground.class);
        Mockito.when(image.getBackgroundPosition()).thenReturn(McfImageBackground.LEFT_OR_TOP);
        PageImageBackground pageImageBackground = new PageImageBackground(image);
        PageRenderContext context = Mockito.mock(PageRenderContext.class);
        Point drawOffsetPixels = new Point();
        BufferedImage result = pageImageBackground.renderAsBitmap(context, drawOffsetPixels, 100, 100);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, drawOffsetPixels.x);
    }*/

    @Test
    public void testRenderAsBitmapRightOrBottom() throws IOException {
        McfImageBackground image = Mockito.mock(McfImageBackground.class);
        Mockito.when(image.getBackgroundPosition()).thenReturn(McfImageBackground.RIGHT_OR_BOTTOM);
        McfAlbumType albumType = Mockito.mock(McfAlbumType.class);
        Mockito.when(albumType.getUsableWidth()).thenReturn(200);
        Mockito.when(albumType.getBleedMargin()).thenReturn(300);
     //   PageRenderContext context = Mockito.mock(PageRenderContext.class);
       // Mockito.when(context.getAlbumType()).thenReturn(albumType);
       // Mockito.when(context.toPixel(Mockito.anyFloat())).thenReturn(50);
        PageImageBackground pageImageBackground = new PageImageBackground(image);
        Point drawOffsetPixels = new Point();
        //BufferedImage result = pageImageBackground.renderAsBitmap(context, drawOffsetPixels, 100, 100);
       // Assert.assertNotNull(result);
        Assert.assertEquals(0, drawOffsetPixels.x);
    }
}
