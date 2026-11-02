package net.sf.mcf2pdf.pagebuild;

import static net.sf.mcf2pdf.testutil.McfTestSupport.parse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfImage;
import net.sf.mcf2pdf.mcfelements.McfImageBackground;
import net.sf.mcf2pdf.mcfelements.impl.McfFotobookImpl;
import net.sf.mcf2pdf.mcfelements.impl.McfImageBackgroundImpl;
import net.sf.mcf2pdf.mcfglobals.McfAlbumType;

public class ModernImageBackgroundTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void nestedCutoutRendersAllPositionsAtDifferentDpi() throws Exception {
		BufferedImage source = new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = source.createGraphics();
		g.setColor(Color.RED);
		g.fillRect(0, 0, 40, 40);
		g.setColor(Color.BLUE);
		g.fillRect(40, 0, 40, 40);
		g.dispose();
		File dir = tmp.newFolder();
		ImageIO.write(source, "png", new File(dir, "photo.png"));
		for (String position : new String[] { "LEFT_OR_TOP", "RIGHT_OR_BOTTOM", "BUNDLE" }) {
			McfFotobook book = parse(
					"<fotobook version=\"4.0\" imagedir=\".\"><page><area><position width=\"80\" height=\"80\"/>"
							+ "<imagebackground filename=\"photo.png\" backgroundPosition=\"" + position
							+ "\"><cutout scale=\"2\" left=\"-80\" top=\"0\"/>"
							+ "</imagebackground></area></page></fotobook>");
			((McfFotobookImpl) book).setFile(new File(dir, "book.mcf"));
			McfImageBackground model = (McfImageBackground) book.getPages().get(0).getAreas().get(0).getContent();
			assertEquals(2, model.getScale(), 0.001);
			McfAlbumType album = mock(McfAlbumType.class);
			when(album.getUsableWidth()).thenReturn(100);
			when(album.getBleedMargin()).thenReturn(10);
			for (int dpi : new int[] { 127, 254 }) {
				PageRenderContext ctx = new PageRenderContext(dpi, 1, null, album);
				Point offset = new Point();
				BufferedImage result = new PageImageBackground(model).renderAsBitmap(ctx, offset, 0, 0);
				assertEquals(Color.BLUE.getRGB(), result.getRGB(0, 0));
				assertEquals(Color.BLUE.getRGB(), result.getRGB(result.getWidth() - 1, result.getHeight() - 1));
				assertEquals("RIGHT_OR_BOTTOM".equals(position) ? ctx.toPixel(11) : 0, offset.x);
			}
		}
	}

	@Test
	public void legacyScaleStillUsesImageResolution() {
		McfImageBackgroundImpl model = new McfImageBackgroundImpl();
		model.setScale(2);
		assertEquals(30 * Math.sqrt(2), new PageImageBackground(model).sourceExtent(10, 6, 2), 0.001);
	}

	@Test
	public void ordinaryPhotoCutoutIgnoresEmbeddedAndOutputDpi() throws Exception {
		File dir = tmp.newFolder();
		BufferedImage source = new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = source.createGraphics();
		g.setColor(Color.RED);
		g.fillRect(0, 0, 40, 40);
		g.setColor(Color.BLUE);
		g.fillRect(40, 0, 40, 40);
		g.dispose();
		// Encode two genuinely different source DPIs rather than comparing the formula
		// with itself.
		for (int sourceDpi : new int[] { 72, 300 }) {
			File file = new File(dir, "photo-" + sourceDpi + ".png");
			ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
			IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(source),
					writer.getDefaultWriteParam());
			IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
			IIOMetadataNode physical = new IIOMetadataNode("pHYs");
			int pixelsPerMeter = (int) Math.round(sourceDpi / 0.0254);
			physical.setAttribute("pixelsPerUnitXAxis", Integer.toString(pixelsPerMeter));
			physical.setAttribute("pixelsPerUnitYAxis", Integer.toString(pixelsPerMeter));
			physical.setAttribute("unitSpecifier", "meter");
			root.appendChild(physical);
			metadata.mergeTree("javax_imageio_png_1.0", root);
			try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
				writer.setOutput(out);
				writer.write(null, new IIOImage(source, null, metadata), writer.getDefaultWriteParam());
			}
			finally {
				writer.dispose();
			}
			ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
			try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
				reader.setInput(in);
				IIOMetadataNode stored = (IIOMetadataNode) reader.getImageMetadata(0)
					.getAsTree("javax_imageio_png_1.0");
				IIOMetadataNode density = (IIOMetadataNode) stored.getElementsByTagName("pHYs").item(0);
				assertEquals(pixelsPerMeter, Integer.parseInt(density.getAttribute("pixelsPerUnitXAxis")));
			}
			finally {
				reader.dispose();
			}
			McfFotobook book = parse("<fotobook version=\"4.0\" imagedir=\".\"><page><area>"
					+ "<position width=\"80\" height=\"80\"/><image filename=\"" + file.getName()
					+ "\"><cutout scale=\"2\" left=\"-80\" top=\"0\"/></image></area></page></fotobook>");
			((McfFotobookImpl) book).setFile(new File(dir, "book.mcf"));
			McfImage model = (McfImage) book.getPages().get(0).getAreas().get(0).getContent();
			for (int outputDpi : new int[] { 127, 254 }) {
				BufferedImage result = new PageImage(model)
					.renderAsBitmap(new PageRenderContext(outputDpi, 1, null, null), new Point(), 0, 0);
				assertEquals(Color.BLUE.getRGB(), result.getRGB(0, 0));
				assertEquals(Color.BLUE.getRGB(), result.getRGB(result.getWidth() - 1, result.getHeight() - 1));
			}
		}
	}

}
