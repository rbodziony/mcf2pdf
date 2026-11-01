package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.apache.commons.beanutils.ConvertUtils;
import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;

import net.sf.mcf2pdf.mcfelements.*;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Substitutor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.mcf2pdf.mcfconfig.Clipart;
import net.sf.mcf2pdf.mcfconfig.Decoration;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfconfig.Fotoarea;
import net.sf.mcf2pdf.mcfconfig.Template;
import net.sf.mcf2pdf.mcfelements.util.DigesterUtil;
import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class DigesterConfiguratorImplTest {

    private DigesterConfiguratorImpl configurator;
    private Digester digester;
    private AbstractConverter colorConverter;

    @Before
    public void setUp() {
        /*colorConverter = mock(AbstractConverter.class);
        digesterConfigurator = new DigesterConfiguratorImpl();
        digester = mock(Digester.class);// Assuming ColorConverter is a valid class
        digesterConfigurator = new DigesterConfiguratorImpl();
        when(colorConverter.convert(eq(Color.class), any())).thenReturn(Color.RED);*/
        configurator = new DigesterConfiguratorImpl();
        digester = new Digester();
    }

    @Test
    @Ignore
    public void testColorConverter() {
        // Call a public method that uses colorConverter and check the result.
        // Replace "publicMethod" with the actual method name and arguments.
      //  Color result = digesterConfigurator.publicMethod();
        //assertEquals(Color.RED, result);
    }

    @Test
    public void testConfigureDigester() throws IOException, SAXException {
        String xml = "<fotobook>\n" +
                "  <pagenumbering/>\n" +
                "  <page>\n" +
                "    <bundlesize/>\n" +
                "    <background/>\n" +
                "    <area>\n" +
                "      <border/>\n" +
                "      <text/>\n" +
                "      <clipart/>\n" +
                "      <image/>\n" +
                "      <position/>\n" +
                "    </area>\n" +
                "  </page>\n" +
                "</fotobook>";

        configurator.configureDigester(digester, null);
        Object root = digester.parse(new StringReader(xml));
        McfFotobook fotobook = (McfFotobook) root;
        assertNotNull(fotobook);
    }

    @Test
    public void testParsesLegacyCornersWithoutEnabledAttribute() throws Exception {
        String xml = "<fotobook><page><area><corners>"
                + "<corner length=\"1000\" shape=\"convex\" where=\"bottom-left\"/>"
                + "</corners></area></page></fotobook>";
        configurator.configureDigester(digester, null);
        McfFotobook book = digester.parse(new StringReader(xml));
        McfCorners corners = book.getPages().get(0).getAreas().get(0).getCorners();
        assertNotNull(corners);
        assertTrue(corners.isEnabled());
        assertEquals(1000, corners.getCorners().get(0).getLength());
        assertEquals("bottom-left", corners.getCorners().get(0).getWhere());
    }

    @Test
    public void testParsesPage68Corners() throws Exception {
        String xml = "<fotobook><page pagenr=\"68\"><area><decoration>"
                + "<corners enabled=\"yes\">"
                + "<corner length=\"5000\" shape=\"convex\" where=\"top-left\"/>"
                + "<corner length=\"5000\" shape=\"convex\" where=\"bottom-right\"/>"
                + "</corners></decoration></area></page></fotobook>";
        configurator.configureDigester(digester, null);
        McfFotobook book = digester.parse(new StringReader(xml));
        McfCorners corners = book.getPages().get(0).getAreas().get(0).getCorners();
        assertNotNull(corners);
        assertEquals(2, corners.getCorners().size());
        assertEquals(5000, corners.getCorners().get(0).getLength());
        assertEquals("convex", corners.getCorners().get(0).getShape());
        assertEquals("top-left", corners.getCorners().get(0).getWhere());
        assertEquals("bottom-right", corners.getCorners().get(1).getWhere());
    }

    @Test
    public void testParsesBackgroundLayoutAndAlignmentAttributes() throws IOException, SAXException {
        String xml = "<fotobook><page>" +
                "<background templatename=\"fotobook_full_landscape_47,normal\" layout=\"1\"/>" +
                "<background alignment=\"3\" templatename=\"Weiss,normal,\" type=\"1\" rotation=\"0\"/>" +
                "</page></fotobook>";

        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(new StringReader(xml));
        McfPage page = fotobook.getPages().get(0);

        assertEquals(1, page.getBackgrounds().get(0).getLayout());
        assertEquals(0, page.getBackgrounds().get(1).getLayout());
        assertEquals(3, page.getBackgrounds().get(1).getAlignment());
        assertEquals(1, page.getBackgrounds().get(1).getType());
    }

    @Test
    public void testParsesClipartColorMappings() throws IOException, SAXException {
        String xml = "<fotobook><page><area><clipart designElementId=\"120115\">" +
                "<ClipartConfiguration applySpotColor=\"0\"><colors>" +
                "<color source=\"#ffffff\" target=\"#aabb91\"/>" +
                "</colors></ClipartConfiguration></clipart></area></page></fotobook>";

        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(new StringReader(xml));
        McfClipart clipart = (McfClipart) fotobook.getPages().get(0).getAreas().get(0).getContent();

        assertEquals(new Color(0xaabb91), clipart.getColorMappings().get(Color.WHITE));
    }
}