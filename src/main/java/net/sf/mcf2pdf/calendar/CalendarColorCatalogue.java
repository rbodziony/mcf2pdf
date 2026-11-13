package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.mcf2pdf.mcfelements.McfCalendarColorSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Reads CEWE calendar colors and the legacy schema-name aliases. */
public final class CalendarColorCatalogue {

	private final Map<String, ColorSchema> schemas;
	private final Map<String, String> synonyms;

	public CalendarColorCatalogue(File schemaFile, File synonymFile) throws IOException {
		this(schemaFile, synonymFile, null);
	}

	/** Embedded project schemas take precedence over these sidecar schemas in getSchema. */
	public CalendarColorCatalogue(File schemaFile, File synonymFile, File projectImageDirectory) throws IOException {
		Map<String, ColorSchema> loaded = new LinkedHashMap<String, ColorSchema>(readSchemas(schemaFile));
		if (projectImageDirectory != null && projectImageDirectory.isDirectory()) {
			File[] files = projectImageDirectory.listFiles(file -> file.isFile()
					&& file.getName().toLowerCase(java.util.Locale.ROOT).endsWith("-user-calendar-schema.xml"));
			if (files == null) {
				throw new IOException("Cannot read project calendar schema directory: " + projectImageDirectory);
			}
			java.util.Arrays.sort(files, java.util.Comparator.comparing(File::getName));
			for (File file : files) {
				loaded.putAll(readSchemas(file));
			}
		}
		schemas = Collections.unmodifiableMap(loaded);
		synonyms = readSynonyms(synonymFile);
	}

	public ColorSchema getSchema(String name) {
		return getSchema(name, Collections.<McfCalendarColorSchema>emptyList());
	}

	public ColorSchema getSchema(String name, Collection<? extends McfCalendarColorSchema> userSchemas) {
		if (name == null) {
			return null;
		}
		for (McfCalendarColorSchema userSchema : userSchemas) {
			if (name.equals(userSchema.getSchema())) {
				return fromUserSchema(userSchema);
			}
		}
		String resolved = resolveSchemaName(name);
		return resolved == null ? null : schemas.get(resolved);
	}

	private static ColorSchema fromUserSchema(McfCalendarColorSchema schema) {
		Map<String, Map<String, String>> cells = new LinkedHashMap<String, Map<String, String>>();
		for (Map.Entry<String, Map<String, String>> entry : schema.getCells().entrySet()) {
			Map<String, String> attributes = new LinkedHashMap<String, String>(schema.getCellDefault());
			attributes.putAll(entry.getValue());
			cells.put(entry.getKey(), Collections.unmodifiableMap(attributes));
		}
		Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put("schema", schema.getSchema());
		if (schema.getGridColor() != null) {
			attributes.put("gridcolor", schema.getGridColor());
		}
		return new ColorSchema(schema.getSchema(), schema.getGridColor(),
				Collections.unmodifiableMap(attributes), cells, schema.getCellDefault());
	}

	public String resolveSchemaName(String name) {
		if (name == null) {
			return null;
		}
		String current = name;
		for (int i = 0; i <= synonyms.size(); i++) {
			String next = synonyms.get(current);
			if (next == null || next.equals(current)) {
				return current;
			}
			current = next;
		}
		return null;
	}

	public Map<String, ColorSchema> getSchemas() {
		return schemas;
	}

	public Map<String, String> getSynonyms() {
		return synonyms;
	}

	private static Map<String, ColorSchema> readSchemas(File file) throws IOException {
		Document document = CalendarXml.parse(file);
		Map<String, ColorSchema> result = new LinkedHashMap<String, ColorSchema>();
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
			Map<String, Map<String, String>> cells = new LinkedHashMap<String, Map<String, String>>();
			NodeList cellNodes = element.getChildNodes();
			for (int j = 0; j < cellNodes.getLength(); j++) {
				Node cellNode = cellNodes.item(j);
				if (cellNode instanceof Element && "cell".equals(cellNode.getNodeName())) {
					Element cell = (Element) cellNode;
					String type = cell.getAttribute("type");
					if (!type.isEmpty()) {
						cells.put(type, attributes(cell));
					}
				}
			}
			result.put(schema, new ColorSchema(schema, element.getAttribute("gridcolor"),
					attributes(element), cells, Collections.<String, String>emptyMap()));
		}
		return Collections.unmodifiableMap(result);
	}

	private static Map<String, String> readSynonyms(File file) throws IOException {
		Document document = CalendarXml.parse(file);
		Map<String, String> result = new LinkedHashMap<String, String>();
		NodeList children = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element && "synonym".equals(node.getNodeName())) {
				Element synonym = (Element) node;
				String oldName = synonym.getAttribute("old");
				String newName = synonym.getAttribute("new");
				if (!oldName.isEmpty() && !newName.isEmpty()) {
					result.put(oldName, newName);
				}
			}
		}
		return Collections.unmodifiableMap(result);
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

	public static final class ColorSchema {

		private final String schema;
		private final String gridColor;
		private final Map<String, String> attributes;
		private final Map<String, Map<String, String>> cells;
		private final Map<String, String> defaultCellAttributes;

		private ColorSchema(String schema, String gridColor, Map<String, String> attributes,
				Map<String, Map<String, String>> cells, Map<String, String> defaultCellAttributes) {
			this.schema = schema;
			this.gridColor = gridColor;
			this.attributes = attributes;
			this.cells = Collections.unmodifiableMap(cells);
			this.defaultCellAttributes = Collections.unmodifiableMap(defaultCellAttributes);
		}

		public String getSchema() {
			return schema;
		}

		public String getGridColor() {
			return gridColor;
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public Map<String, Map<String, String>> getCells() {
			return cells;
		}

		public Map<String, String> getCell(String type) {
			Map<String, String> cell = cells.get(type);
			return cell == null ? defaultCellAttributes : cell;
		}
	}
}
