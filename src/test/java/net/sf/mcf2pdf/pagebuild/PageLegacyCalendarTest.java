package net.sf.mcf2pdf.pagebuild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import net.sf.mcf2pdf.calendar.CalendarDateModel;
import net.sf.mcf2pdf.calendar.HolidayCatalogue;
import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.impl.McfSvgCalendarImpl;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PageLegacyCalendarTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test public void weekdayDigitsAreVisibleOnLightCellsAndTransferPhotosAreOmitted() throws Exception {
        File css = tmp.newFile("black.css");
        Files.write(css.toPath(), ("text {fill:white; font-size:20px;} .weekdays text {fill:black;}"
                + ".day_bg {fill:white;}").getBytes(StandardCharsets.UTF_8));
        File holidays = tmp.newFile("holidays.xml");
        Files.write(holidays.toPath(), "<holidays><country iso=\"Polska\" locale=\"pl_PL\"/></holidays>".getBytes(StandardCharsets.UTF_8));
        McfResourceScanner resources = mock(McfResourceScanner.class);
        when(resources.getCalendarResource("categories/SVGCalendars/black.css")).thenReturn(css);
        McfArea area = mock(McfArea.class);
        when(area.getWidth()).thenReturn(100f); when(area.getHeight()).thenReturn(100f);
        McfSvgCalendarImpl svg = new McfSvgCalendarImpl(); svg.setArea(area);
        svg.setCssSheet("categories/SVGCalendars/black.css");
        svg.setSvgDocument("<svg width=\"100\" height=\"100\"><g cewe:layertype=\"transfer\"><image/></g>"
                + "<g cewe:cal=\"cm_day00_style\"><rect class=\"day_bg\" width=\"100\" height=\"100\"/>"
                + "<text x=\"20\" y=\"40\" cewe:cal=\"cm_day00\">99</text></g></svg>");
        LocalDate start = LocalDate.of(2026, 6, 1); // Monday, in row 0 column 0.
        CalendarDateModel model = new CalendarDateModel(start, "pl_PL", null,
                new HolidayCatalogue(holidays), Collections.emptyList());
        BufferedImage image = new PageLegacyCalendar(svg, start, model.getMonth(0), "pl_PL")
                .renderAsBitmap(new PageRenderContext(254, 1, resources, null), new Point(), 100, 100);
        assertEquals(100, image.getWidth()); assertEquals(100, image.getHeight());
        int dark = 0;
        for (int y = 15; y < 45; y++) for (int x = 15; x < 45; x++) {
            if ((image.getRGB(x, y) & 0xffffff) < 0x808080) dark++;
        }
        assertTrue("Rendered date must remain readable on a white cell", dark > 10);
        assertEquals(0xffffff, image.getRGB(80, 80) & 0xffffff);
    }

    @Test public void missingTemplateReportsTheRequiredPathAndExtraResourceOption() throws Exception {
        McfSvgCalendarImpl svg = new McfSvgCalendarImpl();
        svg.setFileName("categories/SVGCalendars/missing.svg");
        PageRenderContext context = new PageRenderContext(60, 1, mock(McfResourceScanner.class), null);
        try {
            new PageLegacyCalendar(svg, LocalDate.of(2026, 1, 1), null, "pl_PL")
                    .renderAsBitmap(context, new Point(), 100, 100);
            fail();
        } catch (java.io.IOException expected) {
            assertTrue(expected.getMessage().contains("missing.svg"));
            assertTrue(expected.getMessage().contains("-e"));
        }
    }

    @Test public void exactPathsRespectRootPrecedenceAndRejectTraversal() throws Exception {
        File extra = tmp.newFolder("extra"), installed = tmp.newFolder("installed");
        String path = "categories/SVGCalendars/calendar8/layout/black.css";
        File a = new File(extra, path), b = new File(installed, "photofun/" + path);
        assertTrue(a.getParentFile().mkdirs()); assertTrue(b.getParentFile().mkdirs());
        assertTrue(a.createNewFile()); assertTrue(b.createNewFile());
        McfResourceScanner resources = new McfResourceScanner(java.util.Arrays.asList(extra, installed));
        assertEquals(b, resources.getCalendarResource(path));
        assertNull(resources.getCalendarResource("categories/SVGCalendars/another/black.css"));
        try { resources.getCalendarResource("categories/SVGCalendars/../../secret.css"); fail(); }
        catch (java.io.IOException expected) { assertTrue(expected.getMessage().contains("Invalid")); }
    }
}
