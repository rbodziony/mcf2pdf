/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

/**
 * TODO comment
 */
public interface McfText extends McfAreaContent {

	public boolean isSpineText();

	public String getHtmlContent();

	public float getVerticalIndentMargin();

	public float getIndentMargin();

	public int getBackgroundColorAlpha();

	/** The raw textFormat Alignment attribute, e.g. "ALIGNVCENTER,ALIGNHCENTER"; may be null. */
	public String getAlignment();

}