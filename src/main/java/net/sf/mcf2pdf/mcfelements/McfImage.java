/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

/**
 * TODO comment
 */
public interface McfImage extends McfAreaContent {

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
	default java.util.Map<java.awt.Color, java.awt.Color> getColorMappings() {
		return java.util.Collections.emptyMap();
	}

}