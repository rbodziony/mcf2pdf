package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Loads all CEWE catalogues needed to render a calendar. */
public final class CalendarResources {

	private final CalendarLayoutCatalogue layouts;
	private final CalendarColorCatalogue colors;
	private final HolidayCatalogue holidays;
	private final String holidayLanguage;

	private CalendarResources(CalendarLayoutCatalogue layouts, CalendarColorCatalogue colors,
			HolidayCatalogue holidays, String holidayLanguage) {
		this.layouts = layouts;
		this.colors = colors;
		this.holidays = holidays;
		this.holidayLanguage = holidayLanguage;
	}

	public static CalendarResources load(File resourcesDirectory, String locale) throws IOException {
		return load(resourcesDirectory, locale, null);
	}

	public static CalendarResources load(File resourcesDirectory, String locale, String country) throws IOException {
		return load(resourcesDirectory, locale, country, null);
	}

	public static CalendarResources load(File resourcesDirectory, String locale, String country,
			File projectImageDirectory) throws IOException {
		if (locale != null && !locale.isEmpty() && !locale.matches("[A-Za-z]{2,3}_[A-Za-z]{2,3}")) {
			throw new IOException("Invalid calendar language: " + locale);
		}
		File entriesDirectory = new File(resourcesDirectory, "calendarentries");
		File layoutFile = requiredFile(resourcesDirectory, "calendar_layout.xml");
		File schemaFile = requiredFile(resourcesDirectory, "calendar_schema.xml");
		File synonymFile = requiredFile(resourcesDirectory, "calendar_synonym.xml");
		HolidayCatalogue holidays;
		String holidayLanguage = null;
		File legacyEntriesFile = new File(resourcesDirectory, "calendarentries.xml");
		if (!entriesDirectory.isDirectory() && legacyEntriesFile.isFile()) {
			// CEWE 5.x ships a single calendarentries.xml for the installation's country.
			holidays = loadLegacyHolidayCatalogue(legacyEntriesFile, locale, country);
			if (holidays.getCountryAttributes().get("locale") == null) {
				// The file has no locale attribute; the installation language is its <locale>.lproj directory.
				holidayLanguage = installationLanguage(resourcesDirectory);
			}
		}
		else if (locale != null && !locale.isEmpty()) {
			File holidayFile = requiredFile(entriesDirectory, locale + ".xml");
			holidays = new HolidayCatalogue(holidayFile);
		}
		else {
			holidays = findHolidayCatalogue(entriesDirectory, country);
		}
		if (holidayLanguage == null) {
			holidayLanguage = holidays.getCountryAttributes().get("locale");
		}
		return new CalendarResources(new CalendarLayoutCatalogue(layoutFile),
				new CalendarColorCatalogue(schemaFile, synonymFile, projectImageDirectory), holidays, holidayLanguage);
	}

	/** Returns the language of the only <code>&lt;locale&gt;.lproj</code> directory, or <code>null</code>. */
	private static String installationLanguage(File resourcesDirectory) {
		File[] candidates = resourcesDirectory.listFiles(file -> file.isDirectory()
				&& file.getName().matches("[A-Za-z]{2,3}_[A-Za-z]{2,3}\\.lproj"));
		if (candidates == null || candidates.length != 1) {
			return null;
		}
		String name = candidates[0].getName();
		return name.substring(0, name.length() - ".lproj".length());
	}

	private static HolidayCatalogue loadLegacyHolidayCatalogue(File file, String locale, String country)
			throws IOException {
		HolidayCatalogue catalogue = new HolidayCatalogue(file);
		java.util.Map<String, String> attributes = catalogue.getCountryAttributes();
		if (locale != null && !locale.isEmpty()) {
			String fileLocale = attributes.get("locale");
			if (fileLocale != null && !fileLocale.equalsIgnoreCase(locale)) {
				throw new IOException("CEWE calendar entries file " + file.getAbsolutePath() + " is for language '"
						+ fileLocale + "', not '" + locale + "'");
			}
			return catalogue;
		}
		if (country == null || country.trim().isEmpty()) {
			throw new IOException("Calendar language and holidays country are both missing");
		}
		String fileCountry = attributes.get("iso");
		if (fileCountry == null || !fileCountry.equalsIgnoreCase(country.trim())) {
			throw new IOException("CEWE calendar entries file " + file.getAbsolutePath() + " is for country '"
					+ fileCountry + "', not '" + country + "'");
		}
		return catalogue;
	}

	private static HolidayCatalogue findHolidayCatalogue(File entriesDirectory, String country) throws IOException {
		if (country == null || country.trim().isEmpty()) {
			throw new IOException("Calendar language and holidays country are both missing");
		}
		File[] files = entriesDirectory.listFiles();
		if (files == null) {
			throw new IOException("Cannot read CEWE calendar entries directory: " + entriesDirectory.getAbsolutePath());
		}
		List<HolidayCatalogue> matches = new ArrayList<HolidayCatalogue>();
		for (File file : files) {
			if (file.isFile() && file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xml")) {
				HolidayCatalogue catalogue = new HolidayCatalogue(file);
				String countryName = catalogue.getCountryAttributes().get("iso");
				if (countryName != null && countryName.equalsIgnoreCase(country.trim())) {
					matches.add(catalogue);
				}
			}
		}
		if (matches.size() == 1) {
			return matches.get(0);
		}
		if (matches.isEmpty()) {
			throw new IOException("No CEWE calendar entries match holidays country '" + country + "'");
		}
		throw new IOException("Multiple CEWE calendar entry files match holidays country '" + country + "'");
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

	/**
	 * Returns the language of the holiday catalogue: its <code>locale</code> attribute or, for a CEWE 5.x
	 * <code>calendarentries.xml</code> without one, the installation language. May be <code>null</code>.
	 */
	public String getHolidayLanguage() {
		return holidayLanguage;
	}
}
