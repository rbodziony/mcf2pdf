package net.sf.mcf2pdf.mcfelements;

import java.util.Map;

/** A per-cell color override in a project-defined calendar color schema. */
public interface McfCalendarColorCell {

	String getType();

	Map<String, String> getAttributes();
}
