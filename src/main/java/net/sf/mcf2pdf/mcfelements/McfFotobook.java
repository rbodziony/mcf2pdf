/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.awt.Color;
import java.io.File;
import java.util.List;

/**
 * TODO comment
 */
public interface McfFotobook {

	default Color getFlyleafColor() {
		return Color.WHITE;
	}
	
	public File getFile();
	
	public List<? extends McfPage> getPages();
	
	public McfPageNum getPageNum();

	public int getProductType();

	public String getProductName();

	public String getVersion();

	public String getCreatedWithHPSVersion();

	public String getProgramVersion();

	public String getImageDir();

	public int getNormalPages();

	public int getTotalPages();

}