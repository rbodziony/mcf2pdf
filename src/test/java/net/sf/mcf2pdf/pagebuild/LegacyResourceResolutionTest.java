package net.sf.mcf2pdf.pagebuild;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfelements.impl.McfBackgroundImpl;
import net.sf.mcf2pdf.mcfglobals.McfFotoFrame;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

/**
 * Covers the warnings seen when converting old MCF files (fotoksiazka_2008.mcf): colour names such as "Weiss",
 * layout-only backgrounds such as "fotobook_full_landscape_47" and frames referenced by name (6222-DECO-CC).
 */
public class LegacyResourceResolutionTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final String FRAME_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><decorations>"
			+ "<decoration designElementId=\"74132\" id=\"6222-DECO-CC\" type=\"fading\">"
			+ "<fading designElementType=\"passepartout\" file=\"6222-DECO-CC-mask.svg\" keepAspectRatio=\"1\" ratio=\"1.0003\">"
			+ "<clipart designElementType=\"clipart\" file=\"6222-DECO-CC-clip.svg\" ratio=\"1.0003\"/>"
			+ "<fotoarea height=\"0.9925\" width=\"0.9947\" x=\"0.0034\" y=\"0.0051\"/>"
			+ "</fading></decoration></decorations>";

	@Test
	public void testLegacyColourNamesMapToDesignElementIds() {
		assertEquals("201", LegacyBackgroundNames.idFor("Weiss"));
		assertEquals("201", LegacyBackgroundNames.idFor("Wei\u00df"));
		assertEquals("256", LegacyBackgroundNames.idFor("Lightolive"));
		assertEquals("257", LegacyBackgroundNames.idFor("Pastellgruen"));
		assertEquals("248", LegacyBackgroundNames.idFor("Pastellgelb"));
		assertNull(LegacyBackgroundNames.idFor("fotobook_full_landscape_47"));
		assertNull(LegacyBackgroundNames.idFor(null));
	}

	@Test
	public void testBackgroundColorFallsBackToNumericImage() {
		McfResourceScanner resources = mock(McfResourceScanner.class);
		File white = new File("201.jpg");
		when(resources.getImage("201")).thenReturn(white);
		PageRenderContext context = new PageRenderContext(100, 1.0, resources, null);

		assertSame(white, context.getBackgroundColor("Weiss"));
		assertNull(context.getBackgroundColor("NoSuchColour"));
	}

	@Test
	public void testFrameIsFoundByNameAndByDesignElementId() throws IOException {
		File dir = tmp.newFolder("decorations", "74132", "6222-DECO-CC");
		Files.write(new File(dir, "6222-DECO-CC.xml").toPath(), FRAME_XML.getBytes(StandardCharsets.UTF_8));
		assertNotNull(new File(dir, "6222-DECO-CC-clip.clp").createNewFile());
		assertNotNull(new File(dir, "6222-DECO-CC-mask.clp").createNewFile());
		McfResourceScanner scanner = new McfResourceScanner(Collections.singletonList(tmp.getRoot()));
		scanner.scan();
		PageRenderContext context = new PageRenderContext(100, 1.0, scanner, null);

		McfFotoFrame byName = context.getFotoFrame(
				"Schmuckrahmen_fading_6222-DECO-CC-mask.svg_clipart_6222-DECO-CC-clip.svg");

		assertNotNull(scanner.getDecoration("74132"));
		assertNotNull(scanner.getDecoration("6222-DECO-CC"));
		assertNotNull(byName);
		assertNotNull(byName.getFading());
		assertNotNull(byName.getClipart());
		assertNotNull(byName.getConfig().getFotoarea());
		assertNotNull(context.getFotoFrameViaDesignElementID("74132"));
	}

	@Test
	public void testLayoutBackgroundIsNotLookedUpAndColourNameIsDrawn() throws IOException {
		File white = tmp.newFile("201.png");
		BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				img.setRGB(x, y, 0xffff0000);
			}
		}
		ImageIO.write(img, "png", white);
		McfResourceScanner resources = mock(McfResourceScanner.class);
		when(resources.getImage("201")).thenReturn(white);
		PageRenderContext context = new PageRenderContext(100, 1.0, resources, null);

		McfBackgroundImpl layout = new McfBackgroundImpl();
		layout.setTemplateName("fotobook_full_landscape_47,normal");
		layout.setLayout(1);
		McfBackgroundImpl colour = new McfBackgroundImpl();
		colour.setTemplateName("Weiss,normal,");
		colour.setType(1);
		colour.setAlignment(1);

		BufferedImage page = new PageBackground(Arrays.asList(layout, colour), Collections.<McfBackgroundImpl>emptyList())
				.renderAsBitmap(context, new Point(0, 0), 200, 100);

		assertEquals(0xffff0000, page.getRGB(10, 50));
		verify(resources, never()).getImage("fotobook_full_landscape_47");
		verify(resources, never()).getColorImage("fotobook_full_landscape_47");
	}
}
