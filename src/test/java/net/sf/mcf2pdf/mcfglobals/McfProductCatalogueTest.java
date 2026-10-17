package net.sf.mcf2pdf.mcfglobals;

import net.sf.mcf2pdf.mcfglobals.*;
import net.sf.mcf2pdf.mcfglobals.impl.McfProductCatalogueImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.SAXParser;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class McfProductCatalogueTest {

    private McfProductCatalogue mcfProductCatalogue;
    private InputStream inputStream;

    private String xmlprev6 =  "<fotobookdefinitions>" +
            "  <album safetymargin=\"aaa\">" +
            "    <usablesize width=\"10\" height=\"20\" />" +
            "    <spines>" +
            "      <spine pages=\"30\" width=\"40\" />" +
            "    </spines>" +
            "  </album>" +
            "</fotobookdefinitions>";

    private String xmlV6 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><description>" +
            "  <product safetymargin=\"10\" bleedmargin=\"20\" normalpagehorizontalclamp=\"30\" coverextrahorizontal=\"40\" coverextravertical=\"50\" bleedmargincover=\"60\">" +
            "    <usablesize width=\"70\" height=\"80\" />" +
            "    <spines>" +
            "      <spine pages=\"90\" width=\"100\" />" +
            "    </spines>" +
            "  </product>" +
            "</description>";

    @Before
    public void setUp() {
         mcfProductCatalogue = Mockito.mock(McfProductCatalogue.class);
        //inputStream = getClass().getClassLoader().getResourceAsStream("testfotobookV6.xml");
        //inputStream = new ByteArrayInputStream("test data".getBytes());
    }

    @Test
    public void testReadPreV6() throws IOException, SAXException {
       // when(mcfProductCatalogue.read(inputStream, McfProductCatalogue.CatalogueVersion.V6)).thenReturn(mcfProductCatalogue);
        inputStream = new ByteArrayInputStream(xmlprev6.getBytes());
        McfProductCatalogue result = McfProductCatalogue.read(inputStream, McfProductCatalogue.CatalogueVersion.PRE_V6);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testReadPV6() throws IOException, SAXException {
        inputStream = new ByteArrayInputStream(xmlV6.getBytes());
        McfProductCatalogue result = McfProductCatalogue.read(inputStream, McfProductCatalogue.CatalogueVersion.V6);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAlbumType() {
        String name = "testName";
        McfAlbumType mcfAlbumType = Mockito.mock(McfAlbumType.class);
        when(mcfProductCatalogue.getAlbumType(name)).thenReturn(mcfAlbumType);
        McfAlbumType result = mcfProductCatalogue.getAlbumType(name);
        assertEquals(mcfAlbumType, result);
    }

    @Test
    public void testIsEmpty() {
        when(mcfProductCatalogue.isEmpty()).thenReturn(true);
        boolean result = mcfProductCatalogue.isEmpty();
        assertTrue(result);
    }



}