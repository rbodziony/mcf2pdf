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

}
