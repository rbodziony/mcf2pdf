package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BitmapPageBuilderHorizontalSplitTest {
    private List<BufferedImage> split(BufferedImage source) throws IOException {
        List<BufferedImage> pages = new ArrayList<>();
        PageRenderContext context = new PageRenderContext(254, 1, mock(McfResourceScanner.class), null);
        BitmapPageBuilder builder = new BitmapPageBuilder(source.getWidth(), source.getHeight(), context, null) {
            @Override
            public void addImage(XslFoDocumentBuilder document, BufferedImage image) {
                pages.add(image);
            }
        };
        PageDrawable drawable = mock(PageDrawable.class);
        when(drawable.renderAsBitmap(any(), any(), anyInt(), anyInt())).thenReturn(source);
        builder.addDrawable(drawable);
        builder.addHorizontalHalvesToDocumentBuilder(mock(XslFoDocumentBuilder.class));
        verify(drawable, times(1)).renderAsBitmap(any(), any(), anyInt(), anyInt());
        assertEquals(2, pages.size());
        return pages;
    }

    @Test
    public void oddHeightPreservesEveryRowWithoutScalingOrDuplication() throws Exception {
        int[] colors = {Color.RED.getRGB(), Color.GREEN.getRGB(), Color.BLUE.getRGB(),
                        Color.YELLOW.getRGB(), Color.MAGENTA.getRGB()};
        BufferedImage source = new BufferedImage(6, 5, BufferedImage.TYPE_INT_ARGB);
        for (int y=0; y<5; y++) for (int x=0; x<6; x++) source.setRGB(x, y, colors[y]);
        List<BufferedImage> pages = split(source);
        for (BufferedImage page : pages) { assertEquals(6, page.getWidth()); assertEquals(3, page.getHeight()); }
        for (int x=0; x<6; x++) {
            assertEquals(colors[0], pages.get(0).getRGB(x,0));
            assertEquals(colors[1], pages.get(0).getRGB(x,1));
            assertEquals(Color.WHITE.getRGB(), pages.get(0).getRGB(x,2));
            for (int y=0; y<3; y++) assertEquals(colors[y+2], pages.get(1).getRGB(x,y));
        }
    }

    @Test
    public void elementCrossingTheMiddleAppearsOnBothHalves() throws Exception {
        BufferedImage source = new BufferedImage(10, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y=2; y<6; y++) for (int x=3; x<7; x++) source.setRGB(x,y,Color.RED.getRGB());
        List<BufferedImage> pages = split(source);
        for (BufferedImage page : pages) assertEquals(4, page.getHeight());
        assertEquals(Color.RED.getRGB(),pages.get(0).getRGB(4,3));
        assertEquals(Color.RED.getRGB(),pages.get(1).getRGB(4,0));
        assertEquals(Color.WHITE.getRGB(),pages.get(0).getRGB(4,0));
        assertEquals(Color.WHITE.getRGB(),pages.get(1).getRGB(4,3));
    }
}
