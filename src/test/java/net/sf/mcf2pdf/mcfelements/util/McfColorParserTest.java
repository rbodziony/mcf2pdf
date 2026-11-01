package net.sf.mcf2pdf.mcfelements.util;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import net.sf.mcf2pdf.mcfelements.impl.McfClipartImpl;

public class McfColorParserTest {

	@Test
	public void testRgbHex() {
		Assert.assertEquals(new Color(0xbe, 0xdb, 0xe5), McfColorParser.parse("#bedbe5"));
	}

	@Test
	public void testArgbHexDoesNotOverflow() {
		// failed with Color.decode in fotoksiazka_2008.mcf
		Color c = McfColorParser.parse("#ff1a171b");

		Assert.assertEquals(new Color(0x1a, 0x17, 0x1b, 0xff), c);
	}

	@Test
	public void testRgbWithAlphaSuffix() {
		// "#bedbe5,B8" was found in fotoksiazka_2005.mcfx
		Color c = McfColorParser.parse("#bedbe5,B8");

		Assert.assertEquals(new Color(0xbe, 0xdb, 0xe5, 0xb8), c);
	}

	@Test
	public void testEmptyValuesMeanNoColour() {
		// bgcolor="#" in fotoksiazka_2009.mcf
		Assert.assertNull(McfColorParser.parse("#"));
		Assert.assertNull(McfColorParser.parse(""));
		Assert.assertNull(McfColorParser.parse("  "));
		Assert.assertNull(McfColorParser.parse(null));
	}

	@Test
	public void testDecimalArgb() {
		Assert.assertEquals(new Color(0x11, 0x22, 0x33, 0xff), McfColorParser.parse(String.valueOf(0xff112233L)));
	}

	@Test(expected = NumberFormatException.class)
	public void testGarbageIsStillRejected() {
		McfColorParser.parse("#zzzzzz");
	}

	@Test
	public void testClipartColorMappingAcceptsAllNotations() {
		McfClipartImpl clipart = new McfClipartImpl();

		clipart.addColorMapping("#ff1a171b", "#ff1a171b");
		clipart.addColorMapping("#bedbe5", "#bedbe5,B8");
		clipart.addColorMapping("#", "#ffffff");

		Assert.assertEquals(2, clipart.getColorMappings().size());
	}
}
