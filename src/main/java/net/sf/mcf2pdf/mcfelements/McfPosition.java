/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

/**
 * TODO comment
 */
public interface McfPosition {

	public McfArea getArea();

	public float getLeft();

	public float getTop();

	public float getHeight();

	public float getWidth();

	public float getRotation();

	public int getZPosition();
}
