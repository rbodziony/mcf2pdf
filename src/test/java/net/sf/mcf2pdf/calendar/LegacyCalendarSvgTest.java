package net.sf.mcf2pdf.calendar;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import net.sf.mcf2pdf.mcfelements.impl.McfCalendarEventImpl;
import static org.junit.Assert.*;

public class LegacyCalendarSvgTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    private HolidayCatalogue holidays() throws Exception {
        java.io.File file = tmp.newFile();
        Files.write(file.toPath(), ("<holidays><country iso=\"Polska\" locale=\"pl_PL\">"
                + "<day date=\"XXXX-01-01\" honor=\"free\" name=\"New Year\"/>"
                + "</country></holidays>").getBytes(StandardCharsets.UTF_8));
        return new HolidayCatalogue(file);
    }

    private String grid() {
        StringBuilder svg = new StringBuilder("<svg width=\"606\" height=\"432\">");
        svg.append("<text cewe:cal=\"cm_month_full\"/><text cewe:cal=\"cm_year\"/>");
        for (int w = 0; w < 6; w++) {
            svg.append("<g cewe:cal=\"cm_week" + w + "_style\"><text cewe:cal=\"cm_week" + w + "\"/>");
            for (int d = 0; d < 7; d++) {
                svg.append("<g cewe:cal=\"cm_day" + w + d + "_style\"><text cewe:cal=\"cm_day" + w + d + "\">99</text>");
                svg.append("<text cewe:cal=\"cm_holiday" + w + d + "0\"/><text cewe:cal=\"cm_holiday" + w + d + "1\"/></g>");
            }
            svg.append("</g>");
        }
        return svg.append("</svg>").toString();
    }

    private Element field(SVGDocument doc, String key) {
        NodeList elements = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element e = (Element) elements.item(i);
            if (key.equals(e.getAttributeNS(LegacyCalendarSvg.CEWE, "cal"))) return e;
        }
        throw new AssertionError("Missing field " + key);
    }

    @Test public void everyDateAndWeekLandsInItsCellAcrossLeapYearsAndSixRowMonths() throws Exception {
        HolidayCatalogue holidays = holidays();
        String template = grid();
        for (int year = 2014; year <= 2030; year++) {
            LocalDate start = LocalDate.of(year, 1, 1);
            CalendarDateModel dates = new CalendarDateModel(start, "pl_PL", null, holidays, Collections.emptyList());
            for (int m = 0; m < 12; m++) {
                CalendarDateModel.CalendarMonth month = dates.getMonth(m);
                SVGDocument doc = LegacyCalendarSvg.prepare(template, "", start, month, "pl_PL");
                assertEquals(month.getName(), field(doc, "cm_month_full").getTextContent());
                Set<Integer> days = new HashSet<Integer>();
                for (int w = 0; w < 6; w++) {
                    for (int d = 0; d < 7; d++) {
                        String value = field(doc, "cm_day" + w + d).getTextContent();
                        if (w < month.getWeeks().size() && month.getWeeks().get(w).getDays().get(d).isInMonth()) {
                            int expected = month.getWeeks().get(w).getDays().get(d).getDate().getDayOfMonth();
                            assertEquals(Integer.toString(expected), value);
                            assertTrue(days.add(expected));
                        } else assertEquals("", value);
                    }
                    assertEquals(w < month.getWeeks().size()
                            ? Integer.toString(month.getWeeks().get(w).getIsoWeekNumber()) : "",
                            field(doc, "cm_week" + w).getTextContent());
                }
                assertEquals(month.getYearMonth().lengthOfMonth(), days.size());
            }
        }
    }

    @Test public void holidaysEventsAndDisabledHolidaySwitchUseTheSharedDateModel() throws Exception {
        McfCalendarEventImpl event = new McfCalendarEventImpl();
        event.setName("Birthday"); event.setDateTime("2015-01-01");
        LocalDate start = LocalDate.of(2015, 1, 1);
        for (boolean enabled : new boolean[] { true, false }) {
            CalendarDateModel model = new CalendarDateModel(start, "pl_PL", null, holidays(),
                    Collections.singletonList(event), enabled);
            SVGDocument doc = LegacyCalendarSvg.prepare(grid(), "", start, model.getMonth(0), "pl_PL");
            assertEquals(enabled ? "New Year" : "Birthday", field(doc, "cm_holiday030").getTextContent());
            assertEquals(enabled ? "Birthday" : "", field(doc, "cm_holiday031").getTextContent());
            assertEquals(enabled ? "cm_cal_sunday0" : "cm_cal_day", field(doc, "cm_day03_style").getAttribute("class"));
        }
    }

    @Test public void coverSelectsOneYearOrCrossYearAndRemovesTransferPlaceholders() throws Exception {
        String source = "<svg width=\"100\" height=\"50\"><g cewe:layertype=\"transfer\"><image/></g>"
                + "<g cewe:cal=\"cov_oneyear_style\"><text cewe:cal=\"cov_start_year\">2010</text></g>"
                + "<g cewe:cal=\"cov_twoyears_style\"><text cewe:cal=\"cov_end_year\">2010</text></g></svg>";
        for (int month : new int[] { 1, 12 }) {
            SVGDocument doc = LegacyCalendarSvg.prepare(source, "text {fill:red;}", LocalDate.of(2026, month, 1), null, "pl_PL");
            assertEquals("2026", field(doc, "cov_start_year").getTextContent());
            assertEquals(month == 1 ? "2026" : "2027", field(doc, "cov_end_year").getTextContent());
            assertEquals(month == 1 ? "cov_oneyear" : "cov_oneyear_empty", field(doc, "cov_oneyear_style").getAttribute("class"));
            assertEquals(0, doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "image").getLength());
        }
    }

    @Test public void unknownPlaceholdersFailClearlyRatherThanPrintingSampleDates() throws Exception {
        try {
            LegacyCalendarSvg.prepare("<svg width=\"10\" height=\"10\"><text cewe:cal=\"future_token\">2010</text></svg>",
                    "", LocalDate.of(2026, 1, 1), null, "pl_PL");
            fail();
        } catch (java.io.IOException expected) { assertTrue(expected.getMessage().contains("future_token")); }
    }

    @Test public void visibleNonDayOffHolidaysUseTheHolidayStyleAndLocalizedHeadings() throws Exception {
        java.io.File file = tmp.newFile();
        Files.write(file.toPath(), ("<holidays><country iso=\"Polska\" locale=\"pl_PL\">"
                + "<day date=\"XXXX-12-24\" honor=\"show\" name=\"Christmas Eve\"/>"
                + "</country></holidays>").getBytes(StandardCharsets.UTF_8));
        LocalDate start = LocalDate.of(2016, 12, 1);
        CalendarDateModel model = new CalendarDateModel(start, "pl_PL", null,
                new HolidayCatalogue(file), Collections.emptyList());
        SVGDocument doc = LegacyCalendarSvg.prepare(grid().replace("</svg>",
                "<text cewe:cal=\"cm_dayname0\"/><text cewe:cal=\"cm_weekcaption_short\"/></svg>"),
                "", start, model.getMonth(0), "pl_PL");
        assertEquals("cm_cal_sunday3", field(doc, "cm_day35_style").getAttribute("class"));
        assertEquals("Christmas Eve", field(doc, "cm_holiday350").getTextContent());
        assertEquals("TYD", field(doc, "cm_weekcaption_short").getTextContent());
        assertTrue(Character.isUpperCase(field(doc, "cm_dayname0").getTextContent().charAt(0)));
    }
}
