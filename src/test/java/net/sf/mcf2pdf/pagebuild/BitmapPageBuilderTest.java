package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import net.sf.mcf2pdf.pagebuild.BitmapPageBuilder;
import net.sf.mcf2pdf.pagebuild.PageDrawable;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;
import org.junit.rules.TemporaryFolder;

public class BitmapPageBuilderTest {

    private BitmapPageBuilder builder;
    private PageRenderContext context;
    private File tempImageDir;
    private int widthPX = 800;
    private int heightPX = 600;
    private McfResourceScanner resources;

    private McfAlbumType albumType;

/*    @TempDir
    static Path tempDir;*/
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Before
    // final class PageRenderContext
    public void setUp() throws IOException {
        resources = mock(McfResourceScanner.class);
        albumType = mock(McfAlbumType.class);
        context = new PageRenderContext(100, 1.0, resources, albumType);
        tempImageDir =  folder.getRoot();
        builder = new BitmapPageBuilder(widthPX, heightPX, context, tempImageDir);
    }

    @Test
    public void testAddToDocumentBuilder() throws IOException {
        XslFoDocumentBuilder docBuilder = mock(XslFoDocumentBuilder.class);
        PageDrawable drawable = mock(PageDrawable.class);
        List<PageDrawable> drawables = new Vector<>();
        drawables.add(drawable);
        when(drawable.renderAsBitmap(any(), any(), anyInt(), anyInt())).thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB));
        builder.addToDocumentBuilder(docBuilder);
        verify(docBuilder).addPageElement(anyString());
    }

    @Test(expected = NullPointerException.class) // temp dir exits fix this
    public void testAddToDocumentBuilderWithIOException() throws IOException {
        XslFoDocumentBuilder docBuilder = mock(XslFoDocumentBuilder.class);
        File wrongpath = new File("/aaaa/aaa/aa");
        builder = new BitmapPageBuilder(widthPX, heightPX, context, wrongpath);
        //when(tempImageDir.isFile()).thenReturn(true);
        builder.addToDocumentBuilder(docBuilder);
    }

   /* @Test
    public void testCreateImgFoElement() throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Namespace xslFoNs = mock(Namespace.class);
        String src = builder.createImgFoElement(img, xslFoNs);
        assertNotNull(src);
        assertTrue(new File(src).exists());
    }*/
}
