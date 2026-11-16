package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfCalendarArea;

public class McfCalendarAreaImpl extends AbstractMcfAreaContentImpl implements McfCalendarArea {

	private String layoutSchema;

	private String colorSchema;

	private String font;

	private int fontSizePercentage;

	private int fontSizePercentageHolidays;

	private String showHoliday;

	private String optimizedSize;

	private String extraInformation;

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

	@Override
	public int getFontSizePercentage() {
		return fontSizePercentage;
	}

	public void setFontSizePercentage(int fontSizePercentage) {
		this.fontSizePercentage = fontSizePercentage;
	}

	@Override
	public int getFontSizePercentageHolidays() {
		return fontSizePercentageHolidays;
	}

	public void setFontSizePercentageHolidays(int fontSizePercentageHolidays) {
		this.fontSizePercentageHolidays = fontSizePercentageHolidays;
	}

	@Override
	public String getShowHoliday() {
		return showHoliday;
	}

	public void setShowHoliday(String showHoliday) {
		this.showHoliday = showHoliday;
	}

	@Override
	public String getOptimizedSize() {
		return optimizedSize;
	}

	public void setOptimizedSize(String optimizedSize) {
		this.optimizedSize = optimizedSize;
	}

	@Override
	public String getExtraInformation() {
		return extraInformation;
	}

	public void setExtraInformation(String extraInformation) {
		this.extraInformation = extraInformation;
	}

}
