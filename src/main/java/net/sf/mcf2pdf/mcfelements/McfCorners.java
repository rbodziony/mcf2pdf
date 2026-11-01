package net.sf.mcf2pdf.mcfelements;

import java.util.List;

public interface McfCorners {
	default boolean isEnabled() { return true; }
	public List<? extends McfCorner> getCorners();
}
