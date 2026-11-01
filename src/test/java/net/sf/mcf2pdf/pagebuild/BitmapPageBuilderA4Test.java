package net.sf.mcf2pdf.pagebuild;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;
import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

public class BitmapPageBuilderA4Test {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private PageRenderContext context;

	private final List<String> emitted = new ArrayList<>();

	private XslFoDocumentBuilder docBuilder;

	@Before
	public void setUp() {
		context = new PageRenderContext(50, 1.0, mock(McfResourceScanner.class), mock(McfAlbumType.class));
		docBuilder = new XslFoDocumentBuilder() {
			@Override
			public void addPageElement(String imgSource) {
				emitted.add(imgSource);
			}
		};
	}

	private BitmapPageBuilder spread() throws IOException {
		// 400x200 spread: left half red, right half blue
		BitmapPageBuilder b = new BitmapPageBuilder(400, 200, context, folder.getRoot());
		b.addDrawable(new HalfColoredDrawable());
		return b;
	}

	@Test
	public void defaultModeEmitsOneSpreadInOriginalSize() throws IOException {
		spread().addToDocumentBuilder(docBuilder);

		assertEquals(1, emitted.size());
		BufferedImage img = ImageIO.read(new File(emitted.get(0)));
		assertEquals(400, img.getWidth());
		assertEquals(200, img.getHeight());
	}

	@Test
	public void splitModeEmitsTwoPagesOfTargetSizeWithEachHalf() throws IOException {
		BitmapPageBuilder b = spread();
		b.setOutputPage(210, 297, true, true, true);
		b.addToDocumentBuilder(docBuilder);

		assertEquals(2, emitted.size());
		BufferedImage left = ImageIO.read(new File(emitted.get(0)));
		BufferedImage right = ImageIO.read(new File(emitted.get(1)));
		assertEquals(210, left.getWidth());
		assertEquals(297, left.getHeight());
		assertDominant(Color.RED, left.getRGB(105, 148));
		assertDominant(Color.BLUE, right.getRGB(105, 148));
		// centered vertically: top and bottom borders stay white
		assertDominant(Color.WHITE, left.getRGB(105, 5));
	}

	@Test
	public void splitModeKeepsOriginalSizeWhenTargetIsHalfSpread() throws IOException {
		BitmapPageBuilder b = spread();
		b.setOutputPage(200, 200, true, true, true);
		b.addToDocumentBuilder(docBuilder);

		assertEquals(2, emitted.size());
		BufferedImage left = ImageIO.read(new File(emitted.get(0)));
		BufferedImage right = ImageIO.read(new File(emitted.get(1)));
		assertEquals(200, left.getWidth());
		assertEquals(200, left.getHeight());
		// no white border: the half fills the whole page
		assertDominant(Color.RED, left.getRGB(2, 2));
		assertDominant(Color.RED, left.getRGB(197, 197));
		assertDominant(Color.BLUE, right.getRGB(2, 2));
		assertDominant(Color.BLUE, right.getRGB(197, 197));
	}

	@Test
	public void splitModeSkipsSuppressedHalves() throws IOException {
		BitmapPageBuilder b = spread();
		b.setOutputPage(210, 297, true, false, true);
		b.addToDocumentBuilder(docBuilder);

		assertEquals(1, emitted.size());
		assertDominant(Color.BLUE, ImageIO.read(new File(emitted.get(0))).getRGB(105, 148));
	}

	@Test
	public void fitModeEmitsWholeSpreadOnOnePage() throws IOException {
		BitmapPageBuilder b = spread();
		b.setOutputPage(297, 210, false, true, true);
		b.addToDocumentBuilder(docBuilder);

		assertEquals(1, emitted.size());
		BufferedImage img = ImageIO.read(new File(emitted.get(0)));
		assertEquals(297, img.getWidth());
		assertEquals(210, img.getHeight());
		assertDominant(Color.RED, img.getRGB(60, 105));
		assertDominant(Color.BLUE, img.getRGB(237, 105));
	}

	private static void assertDominant(Color expected, int rgb) {
		Color c = new Color(rgb);
		int tolerance = 40;
		assertEquals("red of " + c, expected.getRed(), c.getRed(), tolerance);
		assertEquals("green of " + c, expected.getGreen(), c.getGreen(), tolerance);
		assertEquals("blue of " + c, expected.getBlue(), c.getBlue(), tolerance);
	}

	private static class HalfColoredDrawable implements PageDrawable {
		@Override
		public boolean isVectorGraphic() {
			return false;
		}

		@Override
		public BufferedImage renderAsBitmap(PageRenderContext context, java.awt.Point drawOffsetPixels,
				int widthPX, int heightPX) {
			BufferedImage img = new BufferedImage(widthPX, heightPX, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = img.createGraphics();
			g.setColor(Color.RED);
			g.fillRect(0, 0, widthPX / 2, heightPX);
			g.setColor(Color.BLUE);
			g.fillRect(widthPX / 2, 0, widthPX - widthPX / 2, heightPX);
			g.dispose();
			return img;
		}

		@Override
		public float getLeftMM() {
			return 0;
		}

		@Override
		public float getTopMM() {
			return 0;
		}

		@Override
		public int getZPosition() {
			return 0;
		}

		@Override
		public void renderAsSvgElement(java.io.Writer writer, PageRenderContext context) {
		}
	}
}
