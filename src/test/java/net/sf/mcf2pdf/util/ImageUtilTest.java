package net.sf.mcf2pdf.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import javax.imageio.ImageIO;
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

	private static final int RED = 0xffff0000;
	private static final int BLUE = 0xff0000ff;

	@Test
	public void testPngWithoutExifIsNotRotated() throws Exception {
		BufferedImage img = ImageUtil.readImage(writePng(null, null));

		Assert.assertEquals(4, img.getWidth());
		Assert.assertEquals(2, img.getHeight());
		Assert.assertEquals(RED, img.getRGB(0, 0));
		Assert.assertEquals(BLUE, img.getRGB(3, 1));
	}

	@Test
	public void testPngExifOrientation3IsAppliedLikeCewe() throws Exception {
		BufferedImage img = ImageUtil.readImage(writePng(exifChunk(3), null));

		Assert.assertEquals(4, img.getWidth());
		Assert.assertEquals(2, img.getHeight());
		Assert.assertEquals(BLUE, img.getRGB(0, 0));
		Assert.assertEquals(RED, img.getRGB(3, 1));
	}

	@Test
	public void testPngExifOrientation6SwapsDimensions() throws Exception {
		BufferedImage img = ImageUtil.readImage(writePng(exifChunk(6), null));

		Assert.assertEquals(2, img.getWidth());
		Assert.assertEquals(4, img.getHeight());
	}

	@Test
	public void testPngRawProfileExifOrientation3IsApplied() throws Exception {
		// GIMP/ImageMagick style, as used by the page 28 PNG files
		byte[] tiff = exifTiff(3);
		StringBuilder hex = new StringBuilder();
		byte[] profile = new byte[tiff.length + 6];
		System.arraycopy("Exif\0\0".getBytes(StandardCharsets.ISO_8859_1), 0, profile, 0, 6);
		System.arraycopy(tiff, 0, profile, 6, tiff.length);
		for (byte b : profile) {
			hex.append(String.format("%02x", b));
		}
		String text = "Raw profile type exif\0\nexif\n" + String.format("%8d", profile.length) + "\n" + hex + "\n";

		BufferedImage img = ImageUtil.readImage(writePng(null, pngChunk("tEXt", text.getBytes(StandardCharsets.ISO_8859_1))));

		Assert.assertEquals(BLUE, img.getRGB(0, 0));
		Assert.assertEquals(RED, img.getRGB(3, 1));
	}

	@Test
	public void testPngXmpOrientation3IsApplied() throws Exception {
		String xmp = "<?xpacket begin='\uFEFF' id='W5M0MpCehiHzreSzNTczkc9d'?><x:xmpmeta xmlns:x='adobe:ns:meta/'>"
				+ "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
				+ "<rdf:Description rdf:about='' xmlns:tiff='http://ns.adobe.com/tiff/1.0/' tiff:Orientation='3'/>"
				+ "</rdf:RDF></x:xmpmeta><?xpacket end='w'?>";
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		data.write("XML:com.adobe.xmp".getBytes(StandardCharsets.US_ASCII));
		data.write(new byte[] { 0, 0, 0, 0, 0 });
		data.write(xmp.getBytes(StandardCharsets.UTF_8));

		BufferedImage img = ImageUtil.readImage(writePng(pngChunk("iTXt", data.toByteArray()), null));

		Assert.assertEquals(BLUE, img.getRGB(0, 0));
		Assert.assertEquals(RED, img.getRGB(3, 1));
	}

	@Test
	public void testPngRawProfileExifInCompressedChunkIsApplied() throws Exception {
		byte[] tiff = exifTiff(3);
		byte[] profile = new byte[tiff.length + 6];
		System.arraycopy("Exif\0\0".getBytes(StandardCharsets.ISO_8859_1), 0, profile, 0, 6);
		System.arraycopy(tiff, 0, profile, 6, tiff.length);
		StringBuilder hex = new StringBuilder();
		for (byte b : profile) {
			hex.append(String.format("%02x", b));
		}
		String text = "\nexif\n" + String.format("%8d", profile.length) + "\n" + hex + "\n";

		Deflater deflater = new Deflater();
		deflater.setInput(text.getBytes(StandardCharsets.ISO_8859_1));
		deflater.finish();
		byte[] buf = new byte[1024];
		int n = deflater.deflate(buf);
		deflater.end();

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		data.write("Raw profile type exif".getBytes(StandardCharsets.ISO_8859_1));
		data.write(new byte[] { 0, 0 });
		data.write(buf, 0, n);

		BufferedImage img = ImageUtil.readImage(writePng(pngChunk("zTXt", data.toByteArray()), null));

		Assert.assertEquals(BLUE, img.getRGB(0, 0));
		Assert.assertEquals(RED, img.getRGB(3, 1));
	}

	@Test
	public void testAllExifOrientationsIncludingMirrored() throws Exception {
		int w = 3;
		int h = 2;
		for (int o = 1; o <= 8; o++) {
			BufferedImage src = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					src.setRGB(x, y, 0xff000000 | (x * 80 << 16) | (y * 100 << 8) | 0x40);
				}
			}
			BufferedImage img = ImageUtil.readImage(writePng(src, exifChunk(o), null));

			boolean swap = o >= 5;
			Assert.assertEquals("width, orientation " + o, swap ? h : w, img.getWidth());
			Assert.assertEquals("height, orientation " + o, swap ? w : h, img.getHeight());
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int[] d = destination(o, x, y, w, h);
					Assert.assertEquals("orientation " + o + " pixel " + x + "," + y, src.getRGB(x, y), img.getRGB(d[0], d[1]));
				}
			}
		}
	}

	// EXIF spec: where the source pixel (x, y) ends up for each orientation tag
	private static int[] destination(int o, int x, int y, int w, int h) {
		switch (o) {
		case 2: return new int[] { w - 1 - x, y };
		case 3: return new int[] { w - 1 - x, h - 1 - y };
		case 4: return new int[] { x, h - 1 - y };
		case 5: return new int[] { y, x };
		case 6: return new int[] { h - 1 - y, x };
		case 7: return new int[] { h - 1 - y, w - 1 - x };
		case 8: return new int[] { y, w - 1 - x };
		default: return new int[] { x, y };
		}
	}

	private static byte[] exifTiff(int orientation) {
		return new byte[] { 'M', 'M', 0, 42, 0, 0, 0, 8, 0, 1, 0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, (byte) orientation, 0, 0, 0, 0, 0, 0 };
	}

	private static byte[] exifChunk(int orientation) throws IOException {
		return pngChunk("eXIf", exifTiff(orientation));
	}

	private static byte[] pngChunk(String type, byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bos);
		byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
		CRC32 crc = new CRC32();
		crc.update(typeBytes);
		crc.update(data);
		out.writeInt(data.length);
		out.write(typeBytes);
		out.write(data);
		out.writeInt((int) crc.getValue());
		return bos.toByteArray();
	}

	/** Writes a 4x2 PNG (left half red, right half blue) with an optional extra chunk after IHDR. */
	private static File writePng(byte[] chunk1, byte[] chunk2) throws IOException {
		BufferedImage src = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 4; x++) {
				src.setRGB(x, y, x < 2 ? RED : BLUE);
			}
		}
		return writePng(src, chunk1, chunk2);
	}

	private static File writePng(BufferedImage src, byte[] chunk1, byte[] chunk2) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(src, "png", bos);
		byte[] png = bos.toByteArray();
		int insertAt = 8 + 4 + 4 + 13 + 4;
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		result.write(png, 0, insertAt);
		if (chunk1 != null) {
			result.write(chunk1);
		}
		if (chunk2 != null) {
			result.write(chunk2);
		}
		result.write(png, insertAt, png.length - insertAt);
		File f = File.createTempFile("exif-test", ".png");
		f.deleteOnExit();
		Files.write(f.toPath(), result.toByteArray());
		return f;
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
