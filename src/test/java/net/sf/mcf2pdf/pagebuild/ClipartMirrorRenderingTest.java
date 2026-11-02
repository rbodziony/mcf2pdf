package net.sf.mcf2pdf.pagebuild;

import static net.sf.mcf2pdf.testutil.McfTestSupport.parse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfconfig.Clipart;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfelements.McfClipart;
import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

public class ClipartMirrorRenderingTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private BufferedImage render(String mirror, int rotation) throws Exception {
		File svg = tmp.newFile();
		Files.writeString(svg.toPath(),
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\"><rect width=\"10\" height=\"10\" fill=\"red\"/></svg>");
		McfResourceScanner resources = mock(McfResourceScanner.class);
		Fading config = new Fading();
		Clipart clip = new Clipart();
		clip.setFile("frame.svg");
		config.setClipart(clip);
		when(resources.getDecoration("7")).thenReturn(config);
		when(resources.getClip("frame")).thenReturn(svg);
		String mirrorAttribute = mirror == null ? "" : " mirror=\"" + mirror + "\"";
		McfFotobook book = parse("<fotobook><page><area><position width=\"20\" height=\"20\" rotation=\"" + rotation
				+ "\"/>"
				+ "<clipart uniqueName=\"original\" designElementId=\"7\"><ClipartConfiguration uniqueName=\"ignored\""
				+ mirrorAttribute + "/></clipart></area></page></fotobook>");
		McfClipart clipart = (McfClipart) book.getPages().get(0).getAreas().get(0).getContent();
		assertEquals(mirror, clipart.getMirror());
		assertEquals("original", clipart.getUniqueName());
		Point offset = new Point();
		BufferedImage result = new PageClipart(clipart).renderAsBitmap(new PageRenderContext(254, 1, resources, null),
				offset, 0, 0);
		assertEquals(new Point(), offset);
		return result;
	}

	private void assertRedCorner(BufferedImage image, int cornerX, int cornerY) {
		for (int y : new int[] { 5, 15 }) {
			for (int x : new int[] { 5, 15 }) {
				assertEquals(x == cornerX && y == cornerY ? Color.RED.getRGB() : 0, image.getRGB(x, y));
			}
		}
	}

	@Test
	public void rendersMirrorModesAndIgnoresUnrelatedConfigurationAttributes() throws Exception {
		assertRedCorner(render("x", 0), 5, 15);
		assertRedCorner(render("y", 0), 15, 5);
		assertRedCorner(render("both", 0), 15, 15);
	}

	@Test
	public void absentAndUnknownMirrorLeaveOrientationUnchanged() throws Exception {
		assertRedCorner(render(null, 0), 5, 5);
		assertRedCorner(render("none", 0), 5, 5);
		assertRedCorner(render("unknown", 0), 5, 5);
	}

	@Test
	public void mirrorIsAppliedBeforeClockwiseRotation() throws Exception {
		// Horizontal flip moves the red square to top-right; then +90 degrees moves it to
		// bottom-right.
		// Reversing the operations would put it at bottom-right instead.
		assertRedCorner(render("x", 90), 5, 5);
	}

}
