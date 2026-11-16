package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Reads the layout and cell definitions from CEWE's calendar_layout.xml. */
public final class CalendarLayoutCatalogue {

	private final Map<String, Layout> layouts;

	public CalendarLayoutCatalogue(File file) throws IOException {
		Document document = CalendarXml.parse(file);
		Map<String, Layout> parsed = new LinkedHashMap<String, Layout>();
		NodeList children = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (!(node instanceof Element) || !"calendararea".equals(node.getNodeName())) {
				continue;
			}
			Element element = (Element) node;
			String schema = element.getAttribute("schema");
			if (schema.isEmpty()) {
				continue;
			}
			Map<String, Cell> cells = new LinkedHashMap<String, Cell>();
			NodeList cellNodes = element.getChildNodes();
			for (int j = 0; j < cellNodes.getLength(); j++) {
				Node cellNode = cellNodes.item(j);
				if (cellNode instanceof Element && "cell".equals(cellNode.getNodeName())) {
					Element cell = (Element) cellNode;
					String type = cell.getAttribute("type");
					if (!type.isEmpty()) {
						cells.put(type, readCell(cell));
					}
				}
			}
			parsed.put(schema, new Layout(schema, element.getAttribute("type"),
					element.getAttribute("renderinfo"), attributes(element), cells));
		}
		layouts = Collections.unmodifiableMap(parsed);
	}

	public Layout getLayout(String schema) {
		return layouts.get(schema);
	}

	public Map<String, Layout> getLayouts() {
		return layouts;
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

	private static Cell readCell(Element element) {
		Map<String, String> fontAttributes = Collections.emptyMap();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element && "font".equals(child.getNodeName())) {
				fontAttributes = attributes((Element) child);
				break;
			}
		}
		return new Cell(attributes(element), fontAttributes);
	}

	public static final class Layout {

		private final String schema;
		private final String type;
		private final String renderInfo;
		private final Map<String, String> attributes;
		private final Map<String, Cell> cells;

		private Layout(String schema, String type, String renderInfo, Map<String, String> attributes,
				Map<String, Cell> cells) {
			this.schema = schema;
			this.type = type;
			this.renderInfo = renderInfo;
			this.attributes = attributes;
			this.cells = Collections.unmodifiableMap(cells);
		}

		public String getSchema() {
			return schema;
		}

		public String getType() {
			return type;
		}

		public String getRenderInfo() {
			return renderInfo;
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public Map<String, Cell> getCells() {
			return cells;
		}

		public Cell getCell(String type) {
			return cells.get(type);
		}
	}

	public static final class Cell {

		private final Map<String, String> attributes;
		private final Map<String, String> fontAttributes;

		private Cell(Map<String, String> attributes, Map<String, String> fontAttributes) {
			this.attributes = attributes;
			this.fontAttributes = fontAttributes;
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public Map<String, String> getFontAttributes() {
			return fontAttributes;
		}
	}
}
