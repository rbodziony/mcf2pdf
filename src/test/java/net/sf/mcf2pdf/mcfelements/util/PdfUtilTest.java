package net.sf.mcf2pdf.mcfelements.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.TransformerException;
import org.apache.fop.apps.FOPException;
import net.sf.mcf2pdf.mcfelements.util.PdfUtil;

public class PdfUtilTest {

    private InputStream foStream;
    private OutputStream pdfStream;

    @Before
    public void setUp() {
        foStream = mock(InputStream.class);
        pdfStream = mock(OutputStream.class);
    }

    @Test
    public void convertsFoToCompletePdf() throws IOException, FOPException, TransformerException {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xml:lang=\"en\">"
                + "<fo:layout-master-set><fo:simple-page-master master-name=\"page\" "
                + "page-width=\"8.5in\" page-height=\"11in\" margin=\"0.5in\">"
                + "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
                + "<fo:page-sequence id=\"test-sequence\" master-reference=\"page\"><fo:flow flow-name=\"xsl-region-body\">"
                + "<fo:block>Java 25 PDF conversion regression test</fo:block>"
                + "</fo:flow></fo:page-sequence></fo:root>";
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();

        PdfUtil.convertFO2PDF(
                new ByteArrayInputStream(fo.getBytes(StandardCharsets.UTF_8)), pdf, 150);

        String pdfContent = new String(pdf.toByteArray(), StandardCharsets.ISO_8859_1).trim();
        assertTrue(pdfContent.startsWith("%PDF-"));
        assertTrue(pdfContent.endsWith("%%EOF"));
    }

    @Test
    @Ignore
    public void testConvertFO2PDF() throws IOException, FOPException, TransformerException {
        PdfUtil.convertFO2PDF(foStream, pdfStream, 300);
        verify(pdfStream).flush();
    }

    @Test(expected = FOPException.class)
    @Ignore
    public void testConvertFO2PDFWithFOPException() throws IOException, FOPException, TransformerException {
        doThrow(FOPException.class).when(foStream).read();
        PdfUtil.convertFO2PDF(foStream, pdfStream, 300);
    }

    @Test(expected = TransformerException.class)
    @Ignore
    public void testConvertFO2PDFWithTransformerException() throws IOException, FOPException, TransformerException {
        doThrow(TransformerException.class).when(foStream).read();
        PdfUtil.convertFO2PDF(foStream, pdfStream, 300);
    }

    @Test(expected = IOException.class)
    @Ignore
    public void testConvertFO2PDFWithIOException() throws IOException, FOPException, TransformerException {
        doThrow(IOException.class).when(foStream).read();
        PdfUtil.convertFO2PDF(foStream, pdfStream, 300);
    }
}