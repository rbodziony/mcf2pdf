package net.sf.mcf2pdf.pagebuild;

import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

public class PageRenderContextTest {

	@Test
	public void missingPassepartoutDecorationReturnsNull() {
		McfResourceScanner resources = new McfResourceScanner(Collections.emptyList());
		PageRenderContext context = new PageRenderContext(150, 0.82, resources, null);

		assertNull(context.getFotoFrameViaDesignElementID("missing-decoration"));
	}
}
