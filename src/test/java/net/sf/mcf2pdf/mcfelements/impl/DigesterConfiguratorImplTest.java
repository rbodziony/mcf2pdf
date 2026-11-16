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

    @Test
    public void testParsesCalendarFontAndSizePercentage() throws IOException, SAXException {
        String xml = "<fotobook><page><area areatype=\"calendariumarea\">"
                + "<calendararea layoutschema=\"Month\" colorschema=\"Gray\" font=\"FranklinGothic\" "
                + "fontsize_percentage=\"-20\" fontsize_percentage_holiddays=\"-50\" show_holiday=\"1\" "
                + "optimizedSize=\"600cm2\" extrainformation=\"$$$$\"/></area></page></fotobook>";

        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(new StringReader(xml));
        McfCalendarArea calendar = (McfCalendarArea) fotobook.getPages().get(0).getAreas().get(0).getContent();

        assertEquals("Month", calendar.getLayoutSchema());
        assertEquals("Gray", calendar.getColorSchema());
        assertEquals("FranklinGothic", calendar.getFont());
        assertEquals(-20, calendar.getFontSizePercentage());
        assertEquals(-50, calendar.getFontSizePercentageHolidays());
        assertEquals("1", calendar.getShowHoliday());
        assertEquals("600cm2", calendar.getOptimizedSize());
        assertEquals("$$$$", calendar.getExtraInformation());
    }

    @Test
    public void testParsesHolidaysCountryWithoutLanguage() throws IOException, SAXException {
        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(
                new StringReader("<fotobook><holidays country=\"Polska\" showHolidays=\"0\"/></fotobook>"));

        assertNull(fotobook.getHolidaysLanguage());
        assertEquals("Polska", fotobook.getHolidaysCountry());
        assertEquals("0", fotobook.getHolidaysShowHolidays());
    }

    @Test
    public void testParsesCalendarHolidaySettings() throws IOException, SAXException {
        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(new StringReader(
                "<fotobook startdatecalendarium=\"01.01.2026\"><holidays language=\"pl_PL\" province=\"Mazowieckie\" features=\"holiday\" "
                        + "weekHeadline=\"1\" weekText=\"2\"/></fotobook>"));

        assertEquals("01.01.2026", fotobook.getStartDateCalendarium());
        assertEquals("pl_PL", fotobook.getHolidaysLanguage());
        assertEquals("Mazowieckie", fotobook.getHolidaysProvince());
        assertEquals("holiday", fotobook.getHolidaysFeatures());
        assertEquals("1", fotobook.getHolidaysWeekHeadline());
        assertEquals("2", fotobook.getHolidaysWeekText());
    }

    @Test
    public void testParsesCalendarAreaWithPositionElement() throws IOException, SAXException {
        String xml = "<fotobook><page><area areatype=\"calendariumarea\">"
                + "<position left=\"10\" top=\"20\" width=\"30\" height=\"40\"/>"
                + "<calendararea layoutschema=\"Month\"/></area></page></fotobook>";

        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(new StringReader(xml));
        McfArea area = fotobook.getPages().get(0).getAreas().get(0);
        McfCalendarArea calendar = (McfCalendarArea) area.getContent();

        assertEquals(10f, area.getLeft(), 0.001f);
        assertEquals(20f, area.getTop(), 0.001f);
        assertEquals(30f, area.getWidth(), 0.001f);
        assertEquals(40f, area.getHeight(), 0.001f);
        assertEquals("Month", calendar.getLayoutSchema());
    }

    @Test
    public void testParsesCalendarEventsAndUserColorSchemas() throws IOException, SAXException {
        String xml = "<fotobook>"
                + "<CalendarEvents><CalendarEvent><Name>Birthday</Name><DateTime>2000-05-04</DateTime>"
                + "<Recurrent>yes</Recurrent></CalendarEvent></CalendarEvents>"
                + "<colorSchemata><calendararea colorschema=\"USER_custom\" gridcolor=\"#123456\">"
                + "<celldefault textcolor=\"#000000\" bgcolor=\"transparent\" bordercolor=\"#ffffff\"/>"
                + "<cell type=\"CALENDAR_CELL_TYPE_MONTH\" textcolor=\"#333333\"/>"
                + "</calendararea><calendararea schema=\"USER_legacy\"/></colorSchemata></fotobook>";

        configurator.configureDigester(digester, null);
        McfFotobook fotobook = (McfFotobook) digester.parse(new StringReader(xml));

        assertEquals(1, fotobook.getCalendarEvents().size());
        assertEquals("Birthday", fotobook.getCalendarEvents().get(0).getName());
        assertEquals("2000-05-04", fotobook.getCalendarEvents().get(0).getDateTime());
        assertEquals("yes", fotobook.getCalendarEvents().get(0).getRecurrent());
        assertEquals(2, fotobook.getCalendarColorSchemas().size());
        assertEquals("USER_custom", fotobook.getCalendarColorSchemas().get(0).getSchema());
        assertEquals("#000000", fotobook.getCalendarColorSchemas().get(0).getCellDefault().get("textcolor"));
        assertEquals("#333333", fotobook.getCalendarColorSchemas().get(0).getCells()
                .get("CALENDAR_CELL_TYPE_MONTH").get("textcolor"));
        assertEquals("USER_legacy", fotobook.getCalendarColorSchemas().get(1).getSchema());
    }
}
