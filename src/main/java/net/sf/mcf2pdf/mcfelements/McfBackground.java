/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

/**
 * TODO comment
 */
public interface McfBackground {
	
	public McfPage getPage();

	public String getTemplateName();

	public String getDesignElementId();
	public int getType();

	public int getLayout();

	public int getHue();

	public int getRotation();

	public int getFading();

	/** CEWE alignment: 1 left, 2 centre, 3 right, 4 automatic by page side; 0 if not given. */
	public int getAlignment();

}