package net.sf.mcf2pdf;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.xml.sax.SAXException;
import net.sf.mcf2pdf.Mcf2FoConverter;
import net.sf.mcf2pdf.mcfelements.FotobookBuilder;
import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfPage;

public class Mcf2FoConverterTest {

    @Test
    public void calendarHolidaySwitchSupportsLegacyAndModernProjects() {
        McfFotobook book = Mockito.mock(McfFotobook.class);
        Assert.assertTrue(Mcf2FoConverter.calendarHolidaysEnabled(book));
        Mockito.when(book.getHolidaysShowHolidays()).thenReturn("0");
        Assert.assertFalse(Mcf2FoConverter.calendarHolidaysEnabled(book));
        Mockito.when(book.getHolidaysShowHolidays()).thenReturn("1");
        Assert.assertTrue(Mcf2FoConverter.calendarHolidaysEnabled(book));
        Mockito.when(book.getHolidaysFeatures()).thenReturn("personalEntries, holiday_colors");
        Assert.assertFalse(Mcf2FoConverter.calendarHolidaysEnabled(book));
        Mockito.when(book.getHolidaysFeatures()).thenReturn("holidays, holiday_colors, personalEntries");
        Assert.assertTrue(Mcf2FoConverter.calendarHolidaysEnabled(book));
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void legacyPersonalDatesComeFromHpsOnlyWhenEnabledAndNotEmbedded() throws Exception {
        Mcf2FoConverter converter = createConverter();
        Files.write(new File(tmp.getRoot(), "hps/calendarEvents.xml").toPath(),
                ("<CalendarEvents><CalendarEvent><Name>Birthday</Name><DateTime>2023-03-01T00:00:00</DateTime>"
                        + "<Recurrent>yes</Recurrent></CalendarEvent></CalendarEvents>").getBytes(StandardCharsets.UTF_8));
        McfFotobook book = Mockito.mock(McfFotobook.class);
        Mockito.when(book.getCalendarEvents()).thenReturn(java.util.Collections.emptyList());
        Assert.assertEquals("Birthday", converter.calendarEvents(book, true).get(0).getName());
        Assert.assertTrue("Modern projects do not import account dates", converter.calendarEvents(book, false).isEmpty());
        Mockito.when(book.getHolidaysShowEvents()).thenReturn("0");
        Assert.assertTrue(converter.calendarEvents(book, true).isEmpty());
        Mockito.when(book.getHolidaysShowEvents()).thenReturn("1");
        net.sf.mcf2pdf.mcfelements.McfCalendarEvent embedded = Mockito.mock(net.sf.mcf2pdf.mcfelements.McfCalendarEvent.class);
        Mockito.doReturn(java.util.Collections.singletonList(embedded)).when(book).getCalendarEvents();
        Assert.assertSame(embedded, converter.calendarEvents(book, true).get(0));
    }

    private static final String BUNDLE = "<bundlesize width=\"2970\" height=\"4200\"/>";

    private static final String CALENDAR_PAGES =
            "<page pagenr=\"0\" type=\"calendercoverback\">" + BUNDLE + "</page>"
            + "<page pagenr=\"0\" type=\"calendercoverfront\">" + BUNDLE + "</page>"
            + "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE + "</page>"
            + "<page pagenr=\"2\" type=\"normalpage\">" + BUNDLE + "</page>"
            + "<page pagenr=\"3\" type=\"normalpage\">" + BUNDLE + "</page>"
            + "<page pagenr=\"4\" type=\"normalpage\">" + BUNDLE + "</page>";

    private File tempImageDir;
	private File calendarLayoutFile;

    private Mcf2FoConverter createConverter() throws IOException, SAXException {
        return createConverter(java.util.Collections.emptyList());
    }

    private Mcf2FoConverter createConverter(java.util.List<File> extraResources) throws IOException, SAXException {
        File install = tmp.newFolder("install");
        File products = new File(new File(install, "Resources"), "products");
        Assert.assertTrue(products.mkdirs());
        String catalogue = "<description>"
                + "<product name=\"CAL8\" safetymargin=\"0\" bleedmargin=\"0\"><usablesize width=\"2970\" height=\"4200\"/></product>"
                + "<product name=\"CAL28\" safetymargin=\"0\" bleedmargin=\"0\"><usablesize width=\"2970\" height=\"4200\"/></product>"
                + "</description>";
        Files.write(new File(products, "calendars.xml").toPath(), catalogue.getBytes(StandardCharsets.UTF_8));
        File resources = new File(install, "Resources");
        if (!extraResources.isEmpty()) {
            writeSolidBackground(resources, "87654322.bmp", java.awt.Color.GREEN);
        }
		calendarLayoutFile = new File(resources, "calendar_layout.xml");
        Files.write(new File(resources, "calendar_layout.xml").toPath(),
                "<calendar_layout><calendararea schema=\"Month\" type=\"MONTH\" renderinfo=\"MONTH_NAME\"/></calendar_layout>"
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(resources, "calendar_schema.xml").toPath(),
                "<calendar_schema><calendararea schema=\"Black\" gridcolor=\"#000000\"/></calendar_schema>"
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(resources, "calendar_synonym.xml").toPath(),
                "<synonyms><synonym old=\"Legacy black\" new=\"Black\"/></synonyms>"
                        .getBytes(StandardCharsets.UTF_8));
        File entries = new File(resources, "calendarentries");
        Assert.assertTrue(entries.mkdir());
        Files.write(new File(entries, "pl_PL.xml").toPath(),
                ("<holidays><country iso=\"Polska\" locale=\"pl_PL\"><day id=\"19\" honor=\"free\" name=\"Nowy Rok\"/>"
                        + "</country></holidays>").getBytes(StandardCharsets.UTF_8));
        tempImageDir = tmp.newFolder("images");
        return new Mcf2FoConverter(install, tmp.newFolder("mcftemp"), tempImageDir, tmp.newFolder("hps"), extraResources);
    }

    private void writeSolidBackground(File directory, String name, java.awt.Color color) throws IOException {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 8, 8);
        graphics.dispose();
        javax.imageio.ImageIO.write(image, "bmp", new File(directory, name));
    }

    @Test
    public void extraResourcesFillMissingBackgroundsWithoutOverridingInstalledOnes() throws Exception {
        File extra = tmp.newFolder("extra-resources");
        writeSolidBackground(extra, "87654321.bmp", java.awt.Color.RED);
        writeSolidBackground(extra, "87654322.bmp", java.awt.Color.BLUE);
        Mcf2FoConverter converter = createConverter(java.util.Collections.singletonList(extra));
        String pages = "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE
                + "<background type=\"1\" designElementId=\"87654321\"/></page>"
                + "<page pagenr=\"2\" type=\"normalpage\">" + BUNDLE
                + "<background type=\"1\" designElementId=\"87654322\"/></page>";
        converter.convert(writeProject("CAL8", pages), new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, "1.jpg"));
        int top = image.getRGB(100, 100);
        int bottom = image.getRGB(100, 800);
        Assert.assertTrue("Supplemental red background", ((top >> 16) & 255) > 200 && ((top >> 8) & 255) < 30);
        Assert.assertTrue("Installed green wins over supplemental blue", ((bottom >> 8) & 255) > 200 && (bottom & 255) < 30);
    }

    @Test
    public void calendarCoverFrontBackgroundFillsTheBottomHalf() throws Exception {
        File extra = tmp.newFolder("extra-resources");
        writeSolidBackground(extra, "87654321.bmp", java.awt.Color.RED);
        Mcf2FoConverter converter = createConverter(java.util.Collections.singletonList(extra));
        String pages = "<page pagenr=\"0\" type=\"calendarcoverback\">" + BUNDLE
                + "<background type=\"1\" designElementId=\"0\"/></page>"
                + "<page pagenr=\"0\" type=\"calendarcoverfront\">" + BUNDLE
                + "<background type=\"1\" designElementId=\"87654321\"/></page>";
        converter.convert(writeProject("CAL8", pages), new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, "1.jpg"));
        int top = image.getRGB(100, 100);
        int bottom = image.getRGB(100, 800);
        Assert.assertFalse("Cover back stays without the front background", ((top >> 16) & 255) > 200 && ((top >> 8) & 255) < 30);
        Assert.assertTrue("Cover front background in the bottom half", ((bottom >> 16) & 255) > 200 && ((bottom >> 8) & 255) < 30);
    }

    @Test
    public void backgroundTemplateNamesWithParametersAreRendered() throws Exception {
        File extra = tmp.newFolder("extra-resources");
        writeSolidBackground(extra, "87654321.bmp", java.awt.Color.RED);
        writeSolidBackground(extra, "87654323.bmp", java.awt.Color.RED);
        Mcf2FoConverter converter = createConverter(java.util.Collections.singletonList(extra));
        String pages = "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE
                + "<background type=\"1\" templatename=\"87654321,backgroundId=87654321,normal,\"/></page>"
                + "<page pagenr=\"2\" type=\"normalpage\">" + BUNDLE
                + "<background type=\"1\" templatename=\"87654323,hue=120,fading=0,normal,\"/></page>";
        converter.convert(writeProject("CAL8", pages), new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, "1.jpg"));
        int top = image.getRGB(100, 100);
        int bottom = image.getRGB(100, 800);
        Assert.assertTrue("Template with backgroundId parameter", ((top >> 16) & 255) > 200 && ((top >> 8) & 255) < 30);
        Assert.assertTrue("Template recoloured to green by hue=120", ((bottom >> 8) & 255) > 200 && ((bottom >> 16) & 255) < 30);
    }

    @Test
    public void calendarImageBackgroundRightOrBottomFillsTheBottomHalf() throws Exception {
        Mcf2FoConverter converter = createConverter();
        String pages = "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE
                + "<area areatype=\"imagebackgroundarea\">"
                + "<position height=\"2160\" left=\"-30\" rotation=\"0\" top=\"-30\" width=\"3030\" zposition=\"1\"/>"
                + "<imagebackground backgroundPosition=\"RIGHT_OR_BOTTOM\" filename=\"photo.png\">"
                + "<cutout left=\"0\" scale=\"10\" top=\"0\"/></imagebackground></area></page>"
                + "<page pagenr=\"2\" type=\"normalpage\">" + BUNDLE + "</page>";
        File project = writeProject("CAL8", pages);
        File images = new File(project.getParentFile(), "images");
        Assert.assertTrue(images.mkdir());
        java.awt.image.BufferedImage photo = new java.awt.image.BufferedImage(303, 216, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = photo.createGraphics();
        graphics.setColor(java.awt.Color.RED);
        graphics.fillRect(0, 0, 303, 216);
        graphics.dispose();
        javax.imageio.ImageIO.write(photo, "png", new File(images, "photo.png"));
        converter.convert(project, new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, "1.jpg"));
        int top = image.getRGB(100, 100);
        int bottom = image.getRGB(100, 800);
        Assert.assertFalse("Top half stays without the bottom photo", ((top >> 16) & 255) > 200 && ((top >> 8) & 255) < 30);
        Assert.assertTrue("Photo background in the bottom half", ((bottom >> 16) & 255) > 200 && ((bottom >> 8) & 255) < 30);
    }

    @Test
    public void missingExtraResourceDirectoryReportsItsPath() throws Exception {
        File missing = new File(tmp.getRoot(), "missing-resources");
        try {
            createConverter(java.util.Collections.singletonList(missing));
            Assert.fail("Missing supplemental resources must fail clearly");
        } catch (IOException error) {
            Assert.assertTrue(error.getMessage(), error.getMessage().contains(missing.toString()));
        }
    }

    private File writeProject(String productName, String pagesXml) throws IOException {
        File dir = tmp.newFolder();
        File file = new File(dir, "calendar.mcf");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<fotobook imagedir=\"images\" productname=\"" + productName + "\" programversion=\"6003007\" "
                + "version=\"3.0\" startdatecalendarium=\"01.01.2019\"><holidays language=\"pl_PL\"/>"
                + pagesXml + "</fotobook>";
        Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    public void testCalendarPageRecordsPairIntoOneSheetEach() throws IOException, SAXException {
        Mcf2FoConverter converter = createConverter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        converter.convert(writeProject("CAL8", CALENDAR_PAGES), out, 60, false, false, -1, 1.0);

        // the cover and two sheets, each of them one image
        Assert.assertEquals(3, tempImageDir.listFiles().length);
        String fo = new String(out.toByteArray(), StandardCharsets.UTF_8);
        Assert.assertTrue(fo.contains("master-name=\"default\""));
        Assert.assertFalse(fo.contains("master-name=\"cover\""));
    }

    @Test
    public void acceptsOldCalendarWithCoverAndTwoMonths() throws Exception {
        assertAcceptanceFixture("acceptance-old.mcf");
    }

    @Test
    public void rendersCalendarColorsFromMcfImageDirectory() throws Exception {
        assertProjectSidecarRendered(false);
    }

    @Test
    public void rendersCalendarColorsFromExtractedMcfxImageDirectory() throws Exception {
        assertProjectSidecarRendered(true);
    }

    private void assertProjectSidecarRendered(boolean mcfx) throws Exception {
        Mcf2FoConverter converter = createConverter();
        File project = writeProject("CAL8", "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE
                + "<area areatype=\"calendariumarea\" left=\"100\" top=\"2300\" width=\"1000\" height=\"200\">"
                + "<calendararea layoutschema=\"Month\" colorschema=\"USER_sidecar\"/></area></page>");
        String schema = "<calendar_schema><calendararea schema=\"USER_sidecar\">"
                + "<cell type=\"CALENDAR_CELL_TYPE_MONTH\" textcolor=\"#ff0000\"/>"
                + "</calendararea></calendar_schema>";
        if (mcfx) {
            File database = new File(project.getParentFile(), "calendar.mcfx");
            try (java.sql.Connection db = java.sql.DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
                 java.sql.Statement statement = db.createStatement()) {
                statement.execute("CREATE TABLE Files (Filename TEXT, Data BLOB, LastModified INTEGER)");
                try (java.sql.PreparedStatement insert = db.prepareStatement("INSERT INTO Files VALUES (?, ?, 0)")) {
                    insert.setString(1, "data.mcf");
                    insert.setBytes(2, Files.readAllBytes(project.toPath()));
                    insert.executeUpdate();
                    insert.setString(1, "images/000-user-calendar-schema.xml");
                    insert.setBytes(2, schema.getBytes(StandardCharsets.UTF_8));
                    insert.executeUpdate();
                }
            }
            project = database;
        } else {
            File images = new File(project.getParentFile(), "images");
            Assert.assertTrue(images.mkdir());
            Files.write(new File(images, "000-user-calendar-schema.xml").toPath(), schema.getBytes(StandardCharsets.UTF_8));
        }
        converter.convert(project, new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, "1.jpg"));
        int redPixels = 0;
        for (int y = 540; y < 600; y++) {
            for (int x = 24; x < 270; x++) {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 16) & 255) > ((rgb >> 8) & 255) + 80 && (rgb & 255) < 150) redPixels++;
            }
        }
        Assert.assertTrue("Month title must use the project's red schema", redPixels > 20);
    }

    @Test
    public void acceptsModernCalendarWithCoverAndTwoMonths() throws Exception {
        assertAcceptanceFixture("acceptance-modern.mcf");
    }

    private void assertAcceptanceFixture(String name) throws Exception {
        assertAcceptanceFixture(name, false);
    }

    @Test
    public void splitsOldCalendarIncludingBlankCoverHalf() throws Exception {
        assertAcceptanceFixture("acceptance-old.mcf", true);
    }

    @Test
    public void splitsModernCalendarIncludingBlankCoverHalf() throws Exception {
        assertAcceptanceFixture("acceptance-modern.mcf", true);
    }

    private void assertAcceptanceFixture(String name, boolean split) throws Exception {
        Mcf2FoConverter converter = createConverter();
        converter.setSplitPages(split);
        StringBuilder layouts = new StringBuilder("<calendar_layout>");
        for (String schema : new String[] { "OneDayPerRow (1)", "OneDayPerRow (2)", "OneDayPerRow (4col)" }) {
            layouts.append("<calendararea schema=\"").append(schema).append("\">");
            for (String type : new String[] { "TOPLINE", "WEEKDAY", "SATURDAY", "SUNDAY", "HEAD_WEEKDAY", "HEAD_SATURDAY", "HEAD_SUNDAY" }) {
                layouts.append("<cell type=\"CALENDAR_CELL_TYPE_").append(type)
                        .append("\"><font size=\"12\" face=\"SansSerif\"/></cell>");
            }
            layouts.append("</calendararea>");
        }
        layouts.append("</calendar_layout>");
        Files.write(calendarLayoutFile.toPath(), layouts.toString().getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        converter.convert(new File("src/test/resources/calendar/" + name), out, 60, false, false, -1, 1.0);
        Assert.assertEquals("Cover and two months", split ? 6 : 3, tempImageDir.listFiles().length);
        if (split) Assert.assertTrue(out.toString("UTF-8").contains("page-height=\"496px\""));
        for (int sheet = 1; sheet <= (split ? 6 : 3); sheet++) {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, sheet + ".jpg"));
            Assert.assertEquals(702, image.getWidth());
            Assert.assertEquals(split ? 496 : 992, image.getHeight());
            int ink = 0;
            for (int y = split ? 44 : 540; y < (split ? 399 : 895); y++) {
                for (int x = 24; x < 660; x++) {
                    if ((image.getRGB(x, y) & 0xff) < 120) ink++;
                }
            }
            if (split && sheet % 2 == 1) {
                Assert.assertEquals("Blank top half on sheet " + sheet, 0, ink);
            } else {
                Assert.assertTrue("Visible calendar on sheet " + sheet, ink > 30);
            }
        }
    }

    @Test
    public void testCalendarMaxPageNumberLimitsSheets() throws IOException, SAXException {
        Mcf2FoConverter converter = createConverter();

        converter.convert(writeProject("CAL8", CALENDAR_PAGES), new ByteArrayOutputStream(), 60, false, false, 2, 1.0);

        // the cover and the first sheet (page records 1 and 2)
        Assert.assertEquals(2, tempImageDir.listFiles().length);
    }

    @Test
    public void splitCalendarMaxPageNumberStillLimitsSourceSheets() throws Exception {
        Mcf2FoConverter converter = createConverter();
        converter.setSplitPages(true);
        converter.convert(writeProject("CAL8", CALENDAR_PAGES), new ByteArrayOutputStream(), 60, false, false, 2, 1.0);
        Assert.assertEquals("Two halves of cover plus two halves of first month", 4, tempImageDir.listFiles().length);
    }

    @Test
    public void testCalendarWithMalformedSvgAreasIsRejected() throws IOException, SAXException {
        Mcf2FoConverter converter = createConverter();
        String pages = "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE
                + "<area areatype=\"svgarea\"><position left=\"0\" top=\"0\" width=\"10\" height=\"10\"/></area></page>";

        try {
            converter.convert(writeProject("CAL8", pages), new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
            Assert.fail("SVG calendar areas must be rejected");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("SVG calendar areas"));
        }
    }

    @Test
    public void rendersLegacyInlineAndReferencedSvgWithLocalStylesAndSplitOutput() throws Exception {
        File extra = tmp.newFolder("legacy-resources");
        File global = new File(extra, "categories/SVGCalendars/calendars/globalstyles/black.css");
        File local = new File(extra, "categories/SVGCalendars/calendars/calendar8/synthetic/black.css");
        Assert.assertTrue(global.getParentFile().mkdirs());
        Assert.assertTrue(local.getParentFile().mkdirs());
        Files.write(global.toPath(), ".background {fill:red;}".getBytes(StandardCharsets.UTF_8));
        Files.write(local.toPath(), ".background {fill:blue;}".getBytes(StandardCharsets.UTF_8));
        File template = new File(local.getParentFile(), "month.svg");
        Files.write(template.toPath(), ("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">"
                + "<rect class=\"background\" width=\"100\" height=\"100\"/></svg>").getBytes(StandardCharsets.UTF_8));
        Mcf2FoConverter converter = createConverter(java.util.Collections.singletonList(extra));
        converter.setSplitPages(true);
        String prefix = "<area areatype=\"svgarea\" left=\"0\" top=\"2100\" width=\"100\" height=\"100\">"
                + "<svg cssSheet=\"categories/SVGCalendars/calendars/globalstyles/black.css\">";
        String inline = "<svg_image_calendar><cssSheetTitleIdMap StandardSheet=\"categories/SVGCalendars/calendars/calendar8/synthetic/unused.css\"/>"
                + "<svgDocument>&lt;svg width=\"100\" height=\"100\"&gt;&lt;rect class=\"background\" width=\"100\" height=\"100\"/&gt;&lt;/svg&gt;</svgDocument></svg_image_calendar>";
        String reference = "<svg_image fileName=\"categories/SVGCalendars/calendars/calendar8/synthetic/month.svg\"/>";
        String pages = "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE + prefix + inline + "</svg></area></page>"
                + "<page pagenr=\"2\" type=\"normalpage\">" + BUNDLE + prefix + reference + "</svg></area></page>";
        converter.convert(writeProject("CAL8", pages), new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
        Assert.assertEquals(2, tempImageDir.listFiles().length);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(tempImageDir, "2.jpg"));
        java.awt.Color pixel = new java.awt.Color(image.getRGB(8, 8));
        Assert.assertTrue("Local CSS overrides global colour", pixel.getBlue() > 180 && pixel.getRed() < 50);
    }

    @Test
    public void testOtherCalendarProductsAreRejected() throws IOException, SAXException {
        Mcf2FoConverter converter = createConverter();

        try {
            converter.convert(writeProject("CAL28", CALENDAR_PAGES), new ByteArrayOutputStream(), 60, false, false, -1, 1.0);
            Assert.fail("Only CAL8 is supported");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("only CAL8"));
        }
    }

    @Test
    public void testCalendarCoverPattern() {
        for (String type : new String[] { "calendercoverback", "calendercoverfront", "calendarcoverback", "calendarcoverfront" }) {
            Assert.assertTrue(type, McfPage.CALENDAR_COVER.matcher(type).matches());
        }
        Assert.assertFalse(McfPage.CALENDAR_COVER.matcher("fullcover").matches());
        Assert.assertFalse(McfPage.CALENDAR_COVER.matcher("normalpage").matches());
    }
    @Test(expected = IOException.class)
    public void testConvertwithoutResource() throws IOException, SAXException {
        File mcfInstallDir = new File("./mcfInstallDir");
        File mcfTempDir = new File("./mcfTempDir");
        File tempImageDir = new File("./tempImageDir");
        File hpsDirPath = new File("./hpsDirPath");
        Mcf2FoConverter converter = new Mcf2FoConverter(mcfInstallDir, mcfTempDir, tempImageDir, hpsDirPath);
        File mcfFile = new File("./mcfFile");
        OutputStream xslFoOut = Mockito.mock(OutputStream.class);
        int dpi = 300;
        boolean binding = true;
        boolean pageNum = true;
        int maxPageNo = 10;
        double sx = 1.0;
        converter.convert(mcfFile, xslFoOut, dpi, binding, pageNum, maxPageNo, sx);
        // Add assertions here to verify the expected results
    }
}
