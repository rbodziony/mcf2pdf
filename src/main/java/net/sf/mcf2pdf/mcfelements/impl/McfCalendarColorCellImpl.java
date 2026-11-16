package net.sf.mcf2pdf.mcfelements.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.mcf2pdf.mcfelements.McfCalendarColorCell;

public class McfCalendarColorCellImpl implements McfCalendarColorCell {

	private String type;
	private final Map<String, String> attributes = new LinkedHashMap<String, String>();

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTextColor(String value) {
		attributes.put("textcolor", value);
	}

	public void setBackgroundColor(String value) {
		attributes.put("bgcolor", value);
	}

	public void setBorderColor(String value) {
		attributes.put("bordercolor", value);
	}

	@Override
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}
}
