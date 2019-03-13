package net.sf.mcf2pdf.util;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.mcf2pdf.mcfelements.util.ImageUtil;

import javax.imageio.IIOException;

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
	public void testJpgIssueUnsupportedSOFtype() throws Exception {
		BufferedImage res = ImageUtil.readImage(new File("./src/test/resources/error01.jpg"));
		Assert.assertNotNull(res);
	}
	@Test
	public void testJpgIssueUnsupportedSOFtype2() throws Exception {
		BufferedImage res = ImageUtil.readImage(new File("./src/test/resources/error02.jpg"));
		Assert.assertNotNull(res);
	}


}
