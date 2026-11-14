package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;
import net.sf.mcf2pdf.mcfelements.impl.McfCalendarEventImpl;

/** Legacy CEWE keeps personal dates in hps/calendarEvents.xml, outside the MCF. */
public final class CalendarEventCatalogue {
    private CalendarEventCatalogue() { }

    public static List<McfCalendarEvent> load(File file) throws IOException {
        List<McfCalendarEvent> events = new ArrayList<McfCalendarEvent>();
        if (file == null) return events;
        Element root = CalendarXml.parse(file).getDocumentElement();
        if (!"CalendarEvents".equals(root.getTagName())) {
            throw new IOException("Invalid calendar event catalogue: " + file);
        }
        NodeList nodes = root.getElementsByTagName("CalendarEvent");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            McfCalendarEventImpl event = new McfCalendarEventImpl();
            event.setName(text(node, "Name"));
            event.setDateTime(text(node, "DateTime"));
            event.setRecurrent(text(node, "Recurrent"));
            events.add(event);
        }
        return events;
    }

    private static String text(Element element, String tag) {
        NodeList values = element.getElementsByTagName(tag);
        return values.getLength() == 0 ? null : values.item(0).getTextContent();
    }
}
