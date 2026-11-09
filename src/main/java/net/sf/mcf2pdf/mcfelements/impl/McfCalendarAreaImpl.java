package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfCalendarArea;

public class McfCalendarAreaImpl extends AbstractMcfAreaContentImpl implements McfCalendarArea {

	private String layoutSchema;

	private String colorSchema;

	private String font;

	@Override
	public ContentType getContentType() {
		return ContentType.CALENDAR;
	}

	@Override
	public String getLayoutSchema() {
		return layoutSchema;
	}

	public void setLayoutSchema(String layoutSchema) {
		this.layoutSchema = layoutSchema;
	}

	@Override
	public String getColorSchema() {
		return colorSchema;
	}

	public void setColorSchema(String colorSchema) {
		this.colorSchema = colorSchema;
	}

	@Override
	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

}
