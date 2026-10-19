/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.awt.Color;
import java.util.Map;

/**
 * TODO comment
 */
public interface McfClipart extends McfAreaContent {
	
	public String getUniqueName();
	public String getDesignElementId();
	public Map<Color, Color> getColorMappings();


}
