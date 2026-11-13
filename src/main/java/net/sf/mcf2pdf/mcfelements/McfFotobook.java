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

	/** Province selected in the holidays settings, otherwise <code>null</code>. */
	public String getHolidaysProvince();

	/** Feature settings selected in the holidays settings, otherwise <code>null</code>. */
	public String getHolidaysFeatures();

	/** Legacy CEWE showHolidays setting (0 or 1), otherwise null. */
	public String getHolidaysShowHolidays();

	/** Week headline setting of the holidays settings, otherwise <code>null</code>. */
	public String getHolidaysWeekHeadline();

	/** Week text setting of the holidays settings, otherwise <code>null</code>. */
	public String getHolidaysWeekText();

	public List<? extends McfCalendarEvent> getCalendarEvents();

	public List<? extends McfCalendarColorSchema> getCalendarColorSchemas();

	public int getTotalPages();

}
