package net.sf.mcf2pdf.mcfelements;

/**
 * The calendar part (<code>calendararea</code>) of a calendar area of a wall calendar.
 */
public interface McfCalendarArea extends McfAreaContent {

	/** Name of the CEWE layout schema, e.g. <code>Month</code> or <code>Year (Long-Name-Big)</code>. */
	public String getLayoutSchema();

	/** Name of the CEWE colour schema. */
	public String getColorSchema();

	/** Font family name. */
	public String getFont();

	/** Percentage adjustment to the font size defined by the selected layout. */
	public int getFontSizePercentage();

	/** Percentage adjustment for holiday text. */
	public int getFontSizePercentageHolidays();

	/** Raw CEWE show_holiday value (commonly 0 or 1). */
	public String getShowHoliday();

	/** CEWE optimizedSize value, for example <code>600cm2</code>. */
	public String getOptimizedSize();

	/** Additional layout-specific calendar information. */
	public String getExtraInformation();

}
