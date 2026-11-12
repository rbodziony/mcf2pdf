package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;

/** Loads all CEWE catalogues needed to render a calendar. */
public final class CalendarResources {

	private final CalendarLayoutCatalogue layouts;
	private final CalendarColorCatalogue colors;
	private final HolidayCatalogue holidays;

	private CalendarResources(CalendarLayoutCatalogue layouts, CalendarColorCatalogue colors,
			HolidayCatalogue holidays) {
		this.layouts = layouts;
		this.colors = colors;
		this.holidays = holidays;
	}

	public static CalendarResources load(File resourcesDirectory, String locale) throws IOException {
		if (locale == null || !locale.matches("[A-Za-z]{2,3}_[A-Za-z]{2,3}")) {
			throw new IOException("Invalid or missing calendar language: " + locale);
		}
		File entriesDirectory = new File(resourcesDirectory, "calendarentries");
		File layoutFile = requiredFile(resourcesDirectory, "calendar_layout.xml");
		File schemaFile = requiredFile(resourcesDirectory, "calendar_schema.xml");
		File synonymFile = requiredFile(resourcesDirectory, "calendar_synonym.xml");
		File holidayFile = requiredFile(entriesDirectory, locale + ".xml");
		return new CalendarResources(new CalendarLayoutCatalogue(layoutFile),
				new CalendarColorCatalogue(schemaFile, synonymFile), new HolidayCatalogue(holidayFile));
	}

	private static File requiredFile(File directory, String name) throws IOException {
		File file = new File(directory, name);
		if (!file.isFile()) {
			throw new IOException("Required CEWE calendar resource is missing: " + file.getAbsolutePath());
		}
		return file;
	}

	public CalendarLayoutCatalogue getLayouts() {
		return layouts;
	}

	public CalendarColorCatalogue getColors() {
		return colors;
	}

	public HolidayCatalogue getHolidays() {
		return holidays;
	}
}
