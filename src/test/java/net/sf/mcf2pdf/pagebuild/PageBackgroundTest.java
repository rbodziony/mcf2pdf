package net.sf.mcf2pdf.pagebuild;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import net.sf.mcf2pdf.mcfelements.McfBackground;

public class PageBackgroundTest {

    @Test
    public void testIsVectorGraphic() {
        PageBackground pageBackground = new PageBackground(null, null);
        Assert.assertFalse(pageBackground.isVectorGraphic());
    }

    @Test
    public void testGetZPosition() {
        PageBackground pageBackground = new PageBackground(null, null);
        Assert.assertEquals(0, pageBackground.getZPosition());
    }

    @Test
    public void testGetLeftMM() {
        PageBackground pageBackground = new PageBackground(null, null);
        Assert.assertEquals(0.0f, pageBackground.getLeftMM(), 0.01f);
    }

    @Test
    public void testGetTopMM() {
        PageBackground pageBackground = new PageBackground(null, null);
        Assert.assertEquals(0.0f, pageBackground.getTopMM(), 0.01f);
    }

    @Test
    @Ignore //TODO add PageRenderContext
    public void testRenderAsBitmap() throws IOException {
        McfBackground leftBg = Mockito.mock(McfBackground.class);
        McfBackground rightBg = Mockito.mock(McfBackground.class);
        List<McfBackground> leftBgList = Arrays.asList(leftBg);
        List<McfBackground> rightBgList = Arrays.asList(rightBg);
        PageRenderContext context = Mockito.mock(PageRenderContext.class);
        File file = new File("test.jpg");
        Mockito.when(context.getBackgroundImage(Mockito.anyString())).thenReturn(file);
        Mockito.when(leftBg.getHue()).thenReturn(0);
        Mockito.when(rightBg.getHue()).thenReturn(0);
        PageBackground pageBackground = new PageBackground(leftBgList, rightBgList);
        BufferedImage result = pageBackground.renderAsBitmap(context, null, 100, 100);
        Assert.assertNotNull(result);
    }
}