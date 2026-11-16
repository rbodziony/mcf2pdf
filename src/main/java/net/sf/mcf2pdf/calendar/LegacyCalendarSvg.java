package net.sf.mcf2pdf.calendar;

import java.io.IOException;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import net.sf.mcf2pdf.calendar.CalendarDateModel.CalendarDay;
import net.sf.mcf2pdf.calendar.CalendarDateModel.CalendarMonth;
import net.sf.mcf2pdf.calendar.CalendarDateModel.HolidayOccurrence;
import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;

/** Expands the CEWE 5.x month-block vocabulary into ordinary SVG for Batik. */
public final class LegacyCalendarSvg {
    public static final String CEWE = "http://www.cewecolor.de/xsvg/1.0";
    private static final String SVG = "http://www.w3.org/2000/svg";
    private static final Pattern CELL = Pattern.compile("cm_(day|holiday)([0-5])([0-6])([01]|_style)?");
    private static final Pattern WEEK = Pattern.compile("cm_week([0-5])(_style)?");

    private LegacyCalendarSvg() { }

    public static SVGDocument prepare(String source, String css, LocalDate start,
            CalendarMonth month, String language) throws IOException {
        return prepare(source, css, start, month, language, LegacyCalendarStyle.CEWE6);
    }

    public static SVGDocument prepare(String source, String css, LocalDate start,
            CalendarMonth month, String language, LegacyCalendarStyle style) throws IOException {
        // External stylesheets are replaced by the explicitly resolved project styles.
        source = source.replaceAll("(?s)<\\?xml-stylesheet.*?\\?>", "")
                .replaceAll("(?s)<!DOCTYPE[^>]*>", "");
        if (!source.contains("xmlns=")) source = source.replaceFirst("<svg\\b", "<svg xmlns=\"" + SVG + "\"");
        if (!source.contains("xmlns:cewe=")) source = source.replaceFirst("<svg\\b", "<svg xmlns:cewe=\"" + CEWE + "\"");
        if (!source.contains("xmlns:xlink=")) source = source.replaceFirst("<svg\\b", "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        SVGDocument document = factory.createSVGDocument(null, new StringReader(source));
        Element root = document.getDocumentElement();
        if (!root.hasAttribute("viewBox")) {
            root.setAttribute("viewBox", "0 0 " + root.getAttribute("width") + " " + root.getAttribute("height"));
        }
        populate(root, start, month, language, style);
        if (style == LegacyCalendarStyle.CEWE5) QtCssCompat.markPreviousSiblings(root);
        Element styleElement = document.createElementNS(SVG, "style");
        styleElement.setAttribute("type", "text/css");
        styleElement.setTextContent(css);
        root.insertBefore(styleElement, root.getFirstChild());
        return document;
    }

    private static void populate(Element element, LocalDate start, CalendarMonth month, String language,
            LegacyCalendarStyle style) throws IOException {
        String key = element.getAttributeNS(CEWE, "cal");
        if (!key.isEmpty()) expand(element, key, start, month, language, style);
        for (Node node = element.getFirstChild(); node != null;) {
            Node next = node.getNextSibling();
            if (node instanceof Element) {
                Element child = (Element) node;
                // Transfer layers contain editor placeholder photos; real photos are MCF areas.
                if ("transfer".equals(child.getAttributeNS(CEWE, "layertype"))
                        || "metadata".equals(child.getLocalName()) || CEWE.equals(child.getNamespaceURI())) {
                    element.removeChild(child);
                } else {
                    if ("image".equals(child.getLocalName())) {
                        throw new IOException("Unsupported embedded image in legacy calendar SVG");
                    }
                    populate(child, start, month, language, style);
                }
            }
            node = next;
        }
    }

    private static void expand(Element e, String key, LocalDate start, CalendarMonth month, String language,
            LegacyCalendarStyle style) throws IOException {
        LocalDate end = start.plusMonths(11);
        boolean oneYear = start.getYear() == end.getYear();
        switch (key) {
        case "cov_start_year": e.setTextContent(Integer.toString(start.getYear())); return;
        case "cov_end_year": e.setTextContent(Integer.toString(end.getYear())); return;
        case "cov_oneyear_style": e.setAttribute("class", oneYear ? "cov_oneyear" : "cov_oneyear_empty"); return;
        case "cov_twoyears_style": e.setAttribute("class", oneYear ? "cov_twoyears_empty" : "cov_twoyears"); return;
        default: break;
        }
        if (month == null) throw new IOException("Month placeholder on a legacy calendar cover: " + key);
        if ("cm_year".equals(key)) { e.setTextContent(Integer.toString(month.getYearMonth().getYear())); return; }
        if ("cm_month_full".equals(key)) { e.setTextContent(month.getName()); return; }
        if ("cm_weekcaption_short".equals(key)) { e.setTextContent(language.startsWith("pl") ? "TYD" : "KW"); return; }
        if (key.matches("cm_dayname[0-6]")) {
            int day = key.charAt(key.length() - 1) - '0';
            Locale locale = Locale.forLanguageTag(language.replace('_', '-'));
            String name = DayOfWeek.of(day + 1).getDisplayName(TextStyle.FULL, locale);
            e.setTextContent(name.substring(0, 1).toUpperCase(locale) + name.substring(1));
            return;
        }
        Matcher week = WEEK.matcher(key);
        if (week.matches()) {
            int row = Integer.parseInt(week.group(1));
            boolean present = row < month.getWeeks().size();
            if (week.group(2) != null) {
                e.setAttribute("class", present ? "cm_cal_week" : "cm_cal_week_empty");
                if (!present) e.setAttribute("display", "none");
            } else e.setTextContent(present ? Integer.toString(month.getWeeks().get(row).getIsoWeekNumber()) : "");
            return;
        }
        Matcher cell = CELL.matcher(key);
        if (cell.matches()) {
            int row = Integer.parseInt(cell.group(2)), col = Integer.parseInt(cell.group(3));
            CalendarDay day = row < month.getWeeks().size() ? month.getWeeks().get(row).getDays().get(col) : null;
            boolean present = day != null && day.isInMonth();
            if ("_style".equals(cell.group(4))) {
                boolean dayOff = style == LegacyCalendarStyle.CEWE6 && present && day.isDayOff();
                String type = col == 6 || dayOff ? "sunday" : col == 5 ? "saturday" : "weekday";
                e.setAttribute("class", "cm_cal_" + type + (present ? Integer.toString(row) : "_empty"));
                if (style == LegacyCalendarStyle.CEWE5 ? day == null : !present) e.setAttribute("display", "none");
            } else if ("day".equals(cell.group(1))) {
                e.setTextContent(present ? Integer.toString(day.getDate().getDayOfMonth()) : "");
            } else {
                List<String> labels = new ArrayList<String>();
                if (present) {
                    for (HolidayOccurrence h : day.getHolidays()) if (h.isVisible()) labels.add(h.getName());
                    for (McfCalendarEvent event : day.getEvents()) labels.add(event.getName());
                }
                int line = Integer.parseInt(cell.group(4));
                // Last available line retains remaining labels rather than dropping events.
                e.setTextContent(line >= labels.size() ? "" : line == 1
                        ? String.join(", ", labels.subList(1, labels.size())) : labels.get(0));
            }
            return;
        }
        throw new IOException("Unsupported CEWE calendar SVG placeholder: " + key);
    }
}
