package net.sf.mcf2pdf.mcfelements.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
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