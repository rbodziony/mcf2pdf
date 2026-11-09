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

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static final String BUNDLE = "<bundlesize width=\"2970\" height=\"4200\"/>";

    private static final String CALENDAR_PAGES =
            "<page pagenr=\"0\" type=\"calendercoverback\">" + BUNDLE + "</page>"
            + "<page pagenr=\"0\" type=\"calendercoverfront\">" + BUNDLE + "</page>"
            + "<page pagenr=\"1\" type=\"normalpage\">" + BUNDLE + "</page>"
            + "<page pagenr=\"2\" type=\"normalpage\">" + BUNDLE + "</page>"
            + "<page pagenr=\"3\" type=\"normalpage\">" + BUNDLE + "</page>"
            + "<page pagenr=\"4\" type=\"normalpage\">" + BUNDLE + "</page>";

    private File tempImageDir;

    private Mcf2FoConverter createConverter() throws IOException, SAXException {
        File install = tmp.newFolder("install");
        File products = new File(new File(install, "Resources"), "products");
        Assert.assertTrue(products.mkdirs());
        String catalogue = "<description>"
                + "<product name=\"CAL8\" safetymargin=\"0\" bleedmargin=\"0\"><usablesize width=\"2970\" height=\"4200\"/></product>"
                + "<product name=\"CAL28\" safetymargin=\"0\" bleedmargin=\"0\"><usablesize width=\"2970\" height=\"4200\"/></product>"
                + "</description>";
        Files.write(new File(products, "calendars.xml").toPath(), catalogue.getBytes(StandardCharsets.UTF_8));
        tempImageDir = tmp.newFolder("images");
        return new Mcf2FoConverter(install, tmp.newFolder("mcftemp"), tempImageDir, tmp.newFolder("hps"));
    }

    private File writeProject(String productName, String pagesXml) throws IOException {
        File dir = tmp.newFolder();
        File file = new File(dir, "calendar.mcf");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<fotobook imagedir=\"images\" productname=\"" + productName + "\" programversion=\"6003007\" "
                + "version=\"3.0\" startdatecalendarium=\"01.01.2019\">" + pagesXml + "</fotobook>";
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
    public void testCalendarMaxPageNumberLimitsSheets() throws IOException, SAXException {
        Mcf2FoConverter converter = createConverter();

        converter.convert(writeProject("CAL8", CALENDAR_PAGES), new ByteArrayOutputStream(), 60, false, false, 2, 1.0);

        // the cover and the first sheet (page records 1 and 2)
        Assert.assertEquals(2, tempImageDir.listFiles().length);
    }

    @Test
    public void testCalendarWithSvgAreasIsRejected() throws IOException, SAXException {
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