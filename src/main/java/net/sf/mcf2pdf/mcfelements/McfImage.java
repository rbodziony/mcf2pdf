/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;

/**
 * TODO comment
 */
public interface McfImage extends McfAreaContent {

	/** Nested cutout used by modern photo and background elements. */
	default McfCutout getCutout() {
		return null;
	}

	public String getParentChildRelationshipNature();

	public float getScale();

	public int getUseABK();

	public float getLeft();

	public float getTop();

	public String getFileNameMaster();

	public String getSafeContainerLocation();

	public String getFileName();
	
	public String getFadingFile();

	public String getPassepartoutDesignElementId();

	/** Frame clipart colour substitutions; photographs and masks keep their original colours. */
	default Map<Color, Color> getColorMappings() {
		return Collections.emptyMap();
	}

}