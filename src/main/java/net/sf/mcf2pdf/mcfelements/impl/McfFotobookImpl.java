/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfPageNum;
import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;
import net.sf.mcf2pdf.mcfelements.McfCalendarColorSchema;
import net.sf.mcf2pdf.mcfelements.util.McfColorParser;

public class McfFotobookImpl implements McfFotobook {

	private static final Log log = LogFactory.getLog(McfFotobookImpl.class);

	private Color flyleafColor = Color.WHITE;

	public Color getFlyleafColor() {
		return flyleafColor;
	}

	public void addAddOnVariant(String variant) {
		if (variant == null || !variant.startsWith("flyleafcolor:"))
			return;
		String color = variant.substring("flyleafcolor:".length());
		if ("white".equalsIgnoreCase(color))
			flyleafColor = Color.WHITE;
		else if ("black".equalsIgnoreCase(color))
			flyleafColor = Color.BLACK;
		else {
			try {
				Color parsed = McfColorParser.parse(color);
				if (parsed != null)
					flyleafColor = parsed;
			}
			catch (NumberFormatException ex) {
				// TODO: extend named colors when additional CEWE flyleaf variants are
				// verified.
				log.warn("Unknown flyleaf color: " + color);
			}
		}
	}

	public File file;
	
	private int productType;
	
	private String productName;
	
	private String version;
	
	private String createdWithHPSVersion;
	
	private String programVersion;
	
	private String imageDir;
	
	private int normalPages;
	
	private int totalPages;
	
	private McfPageNum pageNumbering;
	
	private List<McfPage> pages = new Vector<McfPage>();

	private List<McfCalendarEvent> calendarEvents = new Vector<McfCalendarEvent>();

	private List<McfCalendarColorSchema> calendarColorSchemas = new Vector<McfCalendarColorSchema>();
	
	public void addPage(McfPage page) {
		pages.add(page);
	}
	
	public List<? extends McfPage> getPages() {
		return Collections.unmodifiableList(pages);
	}

	public void addCalendarEvent(McfCalendarEvent event) {
		calendarEvents.add(event);
	}

	@Override
	public List<? extends McfCalendarEvent> getCalendarEvents() {
		return Collections.unmodifiableList(calendarEvents);
	}

	public void addCalendarColorSchema(McfCalendarColorSchema schema) {
		calendarColorSchemas.add(schema);
	}

	@Override
	public List<? extends McfCalendarColorSchema> getCalendarColorSchemas() {
		return Collections.unmodifiableList(calendarColorSchemas);
	}
	
	public void addPageNum(McfPageNum pageNumbering) {
		this.pageNumbering = pageNumbering;
	}
	
	public McfPageNum getPageNum() {
		return pageNumbering;
	}
	
	public int getProductType() {
		return productType;
	}

	public void setProductType(int productType) {
		this.productType = productType;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreatedWithHPSVersion() {
		return createdWithHPSVersion;
	}

	public void setCreatedWithHPSVersion(String createdWithHPSVersion) {
		this.createdWithHPSVersion = createdWithHPSVersion;
	}

	public String getProgramVersion() {
		return programVersion;
	}

	public void setProgramVersion(String programVersion) {
		this.programVersion = programVersion;
	}

	public String getImageDir() {
		return imageDir;
	}

	public void setImageDir(String imageDir) {
		this.imageDir = imageDir;
	}

	private String startDateCalendarium;

	private String holidaysLanguage;

	private String holidaysCountry;

	private String holidaysProvince;

	private String holidaysFeatures;
	private String holidaysShowHolidays;
	private String holidaysShowEvents;

	private String holidaysWeekHeadline;

	private String holidaysWeekText;

	public String getStartDateCalendarium() {
		return startDateCalendarium;
	}

	public void setStartDateCalendarium(String startDateCalendarium) {
		this.startDateCalendarium = startDateCalendarium;
	}

	public String getHolidaysLanguage() {
		return holidaysLanguage;
	}

	public void setHolidaysLanguage(String holidaysLanguage) {
		this.holidaysLanguage = holidaysLanguage;
	}

	public String getHolidaysCountry() {
		return holidaysCountry;
	}

	public void setHolidaysCountry(String holidaysCountry) {
		this.holidaysCountry = holidaysCountry;
	}

	public String getHolidaysProvince() {
		return holidaysProvince;
	}

	public void setHolidaysProvince(String holidaysProvince) {
		this.holidaysProvince = holidaysProvince;
	}

	public String getHolidaysFeatures() {
		return holidaysFeatures;
	}

	public String getHolidaysShowHolidays() {
		return holidaysShowHolidays;
	}

	public void setHolidaysShowHolidays(String value) {
		holidaysShowHolidays = value;
	}

	@Override
	public String getHolidaysShowEvents() {
		return holidaysShowEvents;
	}

	public void setHolidaysShowEvents(String value) {
		holidaysShowEvents = value;
	}

	public void setHolidaysFeatures(String holidaysFeatures) {
		this.holidaysFeatures = holidaysFeatures;
	}

	public String getHolidaysWeekHeadline() {
		return holidaysWeekHeadline;
	}

	public void setHolidaysWeekHeadline(String holidaysWeekHeadline) {
		this.holidaysWeekHeadline = holidaysWeekHeadline;
	}

	public String getHolidaysWeekText() {
		return holidaysWeekText;
	}

	public void setHolidaysWeekText(String holidaysWeekText) {
		this.holidaysWeekText = holidaysWeekText;
	}

	public int getNormalPages() {
		return normalPages;
	}

	public void setNormalPages(int normalPages) {
		this.normalPages = normalPages;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
}
