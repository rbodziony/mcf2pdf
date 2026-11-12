package net.sf.mcf2pdf.mcfelements;

import java.util.Map;

/** A project-defined calendar color schema. */
public interface McfCalendarColorSchema {

	String getSchema();

	String getGridColor();

	Map<String, String> getCellDefault();

	Map<String, Map<String, String>> getCells();
}
