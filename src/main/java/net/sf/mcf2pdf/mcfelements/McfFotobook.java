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

	/** Value of <code>startdatecalendarium</code> (dd.MM.yyyy) of a calendar, otherwise <code>null</code>. */
	public String getStartDateCalendarium();

	/** Language of the holidays settings of a calendar, e.g. <code>pl_PL</code>, otherwise <code>null</code>. */
	public String getHolidaysLanguage();

	/** Country selected in the holidays settings, otherwise <code>null</code>. */
	public String getHolidaysCountry();

	public int getTotalPages();

}