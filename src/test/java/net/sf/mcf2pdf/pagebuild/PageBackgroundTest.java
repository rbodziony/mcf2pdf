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
    public void testIsMulticolorNeedsHueAndFadingPair() {
        Assert.assertFalse(PageBackground.isMulticolor(-1, -1));
        Assert.assertFalse(PageBackground.isMulticolor(30, -1));
        Assert.assertFalse(PageBackground.isMulticolor(0, 0));
        Assert.assertTrue(PageBackground.isMulticolor(30, 4));
        Assert.assertTrue(PageBackground.isMulticolor(0, 3));
    }

    @Test
    public void testFadeSaturationClassicShiftsRight() {
        Assert.assertEquals(255, PageBackground.fadeSaturation(255, 255, 0));
        Assert.assertEquals(127, PageBackground.fadeSaturation(255, 255, 1));
        Assert.assertEquals(63, PageBackground.fadeSaturation(255, 255, 2));
    }

    @Test
    public void testFadeSaturation2021NormalisesToMax180_90_45() {
        Assert.assertEquals(180, PageBackground.fadeSaturation(100, 100, 3));
        Assert.assertEquals(90, PageBackground.fadeSaturation(100, 100, 4));
        Assert.assertEquals(45, PageBackground.fadeSaturation(100, 100, 5));
        // a weakly saturated pixel is relative to the most saturated one
        Assert.assertEquals(45, PageBackground.fadeSaturation(50, 100, 4));
        Assert.assertEquals(0, PageBackground.fadeSaturation(0, 0, 4));
    }

    @Test
    public void testRecolorReplacesHueAndKeepsValue() {
        BufferedImage src = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xff0000ff); // saturated blue
        src.setRGB(1, 0, 0xff808080); // grey stays grey

        BufferedImage res = PageBackground.recolor(src, 30, 0);

        float[] hsb = java.awt.Color.RGBtoHSB((res.getRGB(0, 0) >> 16) & 0xff, (res.getRGB(0, 0) >> 8) & 0xff,
                res.getRGB(0, 0) & 0xff, null);
        Assert.assertEquals(30 / 360.0f, hsb[0], 0.01f);
        Assert.assertEquals(1.0f, hsb[1], 0.01f);
        Assert.assertEquals(1.0f, hsb[2], 0.01f);
        Assert.assertEquals(0xff808080, res.getRGB(1, 0));
    }

    @Test
    public void testRecolorFadingMakesPaleAndPreservesAlpha() {
        BufferedImage src = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0x80ff0000);

        BufferedImage res = PageBackground.recolor(src, 30, 4);

        int argb = res.getRGB(0, 0);
        Assert.assertEquals(0x80, argb >>> 24);
        float[] hsb = java.awt.Color.RGBtoHSB((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, null);
        Assert.assertEquals(90 / 255.0f, hsb[1], 0.02f);
    }

    @Test
    public void testRotateQuarterTurnsClockwise() {
        BufferedImage src = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xffff0000); // top-left red

        BufferedImage r90 = PageBackground.rotateQuarterTurns(src, 90);
        Assert.assertEquals(2, r90.getWidth());
        Assert.assertEquals(3, r90.getHeight());
        Assert.assertEquals(0xffff0000, r90.getRGB(1, 0)); // top-left goes to top-right

        BufferedImage r180 = PageBackground.rotateQuarterTurns(src, 180);
        Assert.assertEquals(0xffff0000, r180.getRGB(2, 1));

        BufferedImage r270 = PageBackground.rotateQuarterTurns(src, 270);
        Assert.assertEquals(0xffff0000, r270.getRGB(0, 2)); // top-left goes to bottom-left

        Assert.assertSame(src, PageBackground.rotateQuarterTurns(src, 45));
        Assert.assertSame(src, PageBackground.rotateQuarterTurns(src, 0));
    }

    @Test
    public void testAlignmentSelectsLeftCentreRightPartOfSpreadCrop() {
        // image exactly spread shaped: 400x100, pages 100x100 each (spread 200x100)
        int[] left = PageBackground.computeSourceRect(400, 100, 100, 100, 1, false);
        int[] centre = PageBackground.computeSourceRect(400, 100, 100, 100, 2, false);
        int[] right = PageBackground.computeSourceRect(400, 100, 100, 100, 3, false);

        // the spread crop is 200x100 in the image centre (x 100..300); a page is half of it
        Assert.assertArrayEquals(new int[] { 100, 0, 100, 100 }, left);
        Assert.assertArrayEquals(new int[] { 150, 0, 100, 100 }, centre);
        Assert.assertArrayEquals(new int[] { 200, 0, 100, 100 }, right);
    }

    @Test
    public void testAutomaticAlignmentFollowsPageSide() {
        int[] leftPage = PageBackground.computeSourceRect(400, 100, 100, 100, 4, false);
        int[] rightPage = PageBackground.computeSourceRect(400, 100, 100, 100, 4, true);

        Assert.assertArrayEquals(PageBackground.computeSourceRect(400, 100, 100, 100, 1, false), leftPage);
        Assert.assertArrayEquals(PageBackground.computeSourceRect(400, 100, 100, 100, 3, true), rightPage);
        Assert.assertTrue(rightPage[0] > leftPage[0]);
    }

    @Test
    public void testAlignmentCropsHeightWhenImageIsTallerThanSpread() {
        // 200x200 image, spread 200x100: the crop is the vertical middle
        int[] r = PageBackground.computeSourceRect(200, 200, 100, 100, 1, false);

        Assert.assertArrayEquals(new int[] { 0, 50, 100, 100 }, r);
    }

    @Test
    public void testBackgroundImplDefaultsMeanNotMulticolor() {
        net.sf.mcf2pdf.mcfelements.impl.McfBackgroundImpl bg = new net.sf.mcf2pdf.mcfelements.impl.McfBackgroundImpl();

        Assert.assertEquals(-1, bg.getHue());
        Assert.assertEquals(-1, bg.getFading());
        Assert.assertEquals(0, bg.getAlignment());
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