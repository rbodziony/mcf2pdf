package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Reads fixed, movable, and regional calendar entries from a CEWE locale file. */
public final class HolidayCatalogue {

	private final List<Holiday> holidays;
	private final List<Vacation> vacations;
	private final Map<String, String> countryAttributes;

	public HolidayCatalogue(File file) throws IOException {
		Document document = CalendarXml.parse(file);
		List<Holiday> parsedHolidays = new ArrayList<Holiday>();
		List<Vacation> parsedVacations = new ArrayList<Vacation>();
		Element country = firstChild(document.getDocumentElement(), "country");
		countryAttributes = country == null ? Collections.<String, String>emptyMap() : attributes(country);
		if (country != null) {
			readEntries(country, null, parsedHolidays, parsedVacations);
		}
		holidays = Collections.unmodifiableList(parsedHolidays);
		vacations = Collections.unmodifiableList(parsedVacations);
	}

	public List<Holiday> getHolidays() {
		return holidays;
	}

	public List<Vacation> getVacations() {
		return vacations;
	}

	public Map<String, String> getCountryAttributes() {
		return countryAttributes;
	}

	private static void readEntries(Element parent, String province, List<Holiday> holidays,
			List<Vacation> vacations) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (!(node instanceof Element)) {
				continue;
			}
			Element element = (Element) node;
			if ("day".equals(element.getNodeName())) {
				holidays.add(new Holiday(province, attributes(element)));
			}
			else if ("vacation".equals(element.getNodeName())) {
				vacations.add(new Vacation(province, attributes(element)));
			}
			else if ("province".equals(element.getNodeName())) {
				readEntries(element, element.getAttribute("name"), holidays, vacations);
			}
			else if ("nationwide".equals(element.getNodeName())) {
				readEntries(element, province, holidays, vacations);
			}
		}
	}

	private static Element firstChild(Element parent, String name) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element && name.equals(node.getNodeName())) {
				return (Element) node;
			}
		}
		return null;
	}

	private static Map<String, String> attributes(Element element) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			result.put(attribute.getNodeName(), attribute.getNodeValue());
		}
		return Collections.unmodifiableMap(result);
	}

	public static class Entry {

		private final String province;
		private final Map<String, String> attributes;

		private Entry(String province, Map<String, String> attributes) {
			this.province = province;
			this.attributes = attributes;
		}

		public String getProvince() {
			return province;
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public String getAttribute(String name) {
			return attributes.get(name);
		}
	}

	public static final class Holiday extends Entry {
		private Holiday(String province, Map<String, String> attributes) {
			super(province, attributes);
		}
	}

	public static final class Vacation extends Entry {
		private Vacation(String province, Map<String, String> attributes) {
			super(province, attributes);
		}
	}
}
