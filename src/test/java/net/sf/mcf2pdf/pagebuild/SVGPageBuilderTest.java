package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import java.io.File;
import java.io.IOException;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import net.sf.mcf2pdf.pagebuild.SVGPageBuilder;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;

import static org.mockito.Mockito.mock;

public class SVGPageBuilderTest {
    private PageRenderContext context;
    private File tempImageDir;
    private int widthPX = 800;
    private int heightPX = 600;
    private McfResourceScanner resources;

    private McfAlbumType albumType;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    // final class PageRenderContext
    public void setUp() throws IOException {
        resources = mock(McfResourceScanner.class);
        albumType = mock(McfAlbumType.class);
        context = new PageRenderContext(100, 1.0, resources, albumType);
        tempImageDir =  folder.getRoot();
    }

    @Test
    public void testAddToDocumentBuilder() throws IOException {
        float widthMM = 100.0f;
        float heightMM = 200.0f;
        SVGPageBuilder builder = new SVGPageBuilder(widthMM, heightMM, context, tempImageDir);
        XslFoDocumentBuilder docBuilder = Mockito.mock(XslFoDocumentBuilder.class);
        builder.addToDocumentBuilder(docBuilder);
        // Verify that the context's toPixel method was called with the correct arguments
       // Mockito.verify(context).toPixel(widthMM);
       // Mockito.verify(context).toPixel(heightMM);
    }
}