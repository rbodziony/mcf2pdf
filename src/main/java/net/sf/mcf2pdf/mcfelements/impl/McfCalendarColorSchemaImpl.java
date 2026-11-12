package net.sf.mcf2pdf.mcfelements.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.mcf2pdf.mcfelements.McfCalendarColorCell;
import net.sf.mcf2pdf.mcfelements.McfCalendarColorSchema;

public class McfCalendarColorSchemaImpl implements McfCalendarColorSchema {

	private String schema;
	private String gridColor;
	private Map<String, String> cellDefault = Collections.emptyMap();
	private final Map<String, Map<String, String>> cells = new LinkedHashMap<String, Map<String, String>>();

	@Override
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public String getGridColor() {
		return gridColor;
	}

	public void setGridColor(String gridColor) {
		this.gridColor = gridColor;
	}

	@Override
	public Map<String, String> getCellDefault() {
		return cellDefault;
	}

	public void setCellDefault(McfCalendarColorCell cell) {
		cellDefault = cell.getAttributes();
	}

	@Override
	public Map<String, Map<String, String>> getCells() {
		return Collections.unmodifiableMap(cells);
	}

	public void addCell(McfCalendarColorCell cell) {
		if (cell.getType() != null) {
			cells.put(cell.getType(), cell.getAttributes());
		}
	}
}
