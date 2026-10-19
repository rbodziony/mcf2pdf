package net.sf.mcf2pdf.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.io.File;
import java.util.Map;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import javax.imageio.IIOException;

import net.sf.mcf2pdf.mcfelements.util.ImageUtil;

public class ImageUtilTest {

	@Test
	@Ignore("Currently disabled as no longer used by CEWE?!")
	public void testPngWithRes() throws Exception {
		float[] res = ImageUtil.getImageResolution(new File("./src/test/resources/Bertrand1-figure-withres.png"));
		Assert.assertEquals(72.009f, res[0], 0.01f);
		Assert.assertEquals(72.009f, res[1], 0.01f);
	}

	@Test
	public void testPngWithoutRes() throws Exception {
		float[] res = ImageUtil.getImageResolution(new File("./src/test/resources/Bertrand1-figure.png"));
		Assert.assertEquals(180.0f, res[0], 0.01f);
		Assert.assertEquals(180.0f, res[1], 0.01f);
	}

	@Test(expected = IIOException.class)
	public void testJpgIssueUnsupportedSOFile() throws Exception {
		BufferedImage res = ImageUtil.readImage(new File("./src/test/resources/error01.jpg"));
		Assert.assertNotNull(res);
	}

	@Ignore("Will be working if changed to imageio by twelvemonkeys")
	@Test
	public void testJpgIssueUnsupportedSOFSecondfile() throws Exception {
		BufferedImage res = ImageUtil.readImage(new File("./src/test/resources/error02.jpg"));
		Assert.assertNotNull(res);
	}

	//@Ignore("Will be working if changed to imageio by twelvemonkeys")
	@Test
	public void testSVGShouldwork() throws Exception {
		BufferedImage res = ImageUtil.readImage(new File("./src/test/resources/test.svg"));
		Assert.assertNotNull(res);
	}

	@Test
	public void testNegativeViewBoxRendersWithoutCropping() throws Exception {
		BufferedImage image = ImageUtil.loadClpFile(
				new File("./src/test/resources/negative-viewbox.svg"), 200, 200);

		Assert.assertTrue((image.getRGB(60, 100) >>> 24) > 0);
		Assert.assertEquals(0, image.getRGB(180, 100) >>> 24);
	}

	@Test
	public void testReplaceColorsPreservesAlphaAndUnmappedPixels() {
		BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, new Color(255, 255, 255, 221).getRGB());
		image.setRGB(1, 0, new Color(12, 34, 56, 255).getRGB());
		Map<Color, Color> mappings = new HashMap<Color, Color>();
		mappings.put(Color.WHITE, new Color(0xaabb91));

		ImageUtil.replaceColors(image, mappings);

		Assert.assertEquals(new Color(170, 187, 145, 221), new Color(image.getRGB(0, 0), true));
		Assert.assertEquals(new Color(12, 34, 56, 255), new Color(image.getRGB(1, 0), true));
	}

}
