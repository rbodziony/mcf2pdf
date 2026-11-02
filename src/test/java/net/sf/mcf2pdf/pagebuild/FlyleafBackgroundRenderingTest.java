package net.sf.mcf2pdf.pagebuild;

import static net.sf.mcf2pdf.testutil.McfTestSupport.parse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

public class FlyleafBackgroundRenderingTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void emptyPageUsesFlyleafColorWithoutResourceLookup() throws Exception {
		for (String color : new String[] { "white", "black", "#d26838", "" }) {
			String addon = color.isEmpty() ? "" : "<addOns><addOn variant=\"flyleafcolor:" + color + "\"/></addOns>";
			McfFotobook book = parse("<fotobook>" + addon
					+ "<page type=\"emptypage\"><background designElementId=\"99\" type=\"1\"/></page></fotobook>");
			McfResourceScanner resources = mock(McfResourceScanner.class);
			BufferedImage result = new PageBackground(book.getPages().get(0).getBackgrounds(), Collections.emptyList())
				.renderAsBitmap(new PageRenderContext(254, 1, resources, null), new Point(), 40, 20);
			int rgb = color.equals("black") ? Color.BLACK.getRGB()
					: color.startsWith("#") ? Color.decode(color).getRGB() : Color.WHITE.getRGB();
			assertEquals(rgb, result.getRGB(5, 5));
			assertEquals(0, result.getRGB(35, 5) >>> 24);
			verifyNoInteractions(resources);
		}
	}

	@Test
	public void normalPage99StillUsesResourceLookup() throws Exception {
		McfFotobook book = parse(
				"<fotobook><page type=\"normalpage\"><background designElementId=\"99\" type=\"1\"/></page></fotobook>");
		McfResourceScanner resources = mock(McfResourceScanner.class);
		new PageBackground(book.getPages().get(0).getBackgrounds(), Collections.emptyList())
			.renderAsBitmap(new PageRenderContext(254, 1, resources, null), new Point(), 40, 20);
		verify(resources, atLeastOnce()).getImage("99");
	}

}
