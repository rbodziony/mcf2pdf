package net.sf.mcf2pdf.pagebuild;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import org.junit.Test;

import net.sf.mcf2pdf.mcfconfig.Clipart;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

public class PageRenderContextTest {

	@Test
	public void missingPassepartoutDecorationReturnsNull() {
		McfResourceScanner resources = new McfResourceScanner(Collections.emptyList());
		PageRenderContext context = new PageRenderContext(150, 0.82, resources, null);

		assertNull(context.getFotoFrameViaDesignElementID("missing-decoration"));
	}

	@Test
	public void clipartWithDotsInFileNameIsFoundByDesignElementId() {
		McfResourceScanner resources = mock(McfResourceScanner.class);
		Clipart clipart = new Clipart();
		clipart.setFile("5361.20-CLIP-CC.svg");
		Fading decoration = new Fading();
		decoration.setClipart(clipart);
		File expectedClip = new File("5361.20-CLIP-CC.clp");
		when(resources.getDecoration("69144")).thenReturn(decoration);
		when(resources.getClip("5361.20-CLIP-CC")).thenReturn(expectedClip);

		PageRenderContext context = new PageRenderContext(150, 0.82, resources, null);

		assertSame(expectedClip, context.getClipartViaDesignElementId("69144"));
	}
}
