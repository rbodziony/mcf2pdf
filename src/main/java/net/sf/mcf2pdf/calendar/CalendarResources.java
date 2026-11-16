package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Loads all CEWE catalogues needed to render a calendar. */
public final class CalendarResources {

	private final File layoutFile;
	private CalendarLayoutCatalogue layouts;
	private final CalendarColorCatalogue colors;
	private final HolidayCatalogue holidays;
	private final String holidayLanguage;
	private final File holidayFile;

	private CalendarResources(File layoutFile, CalendarColorCatalogue colors,
			HolidayCatalogue holidays, String holidayLanguage, File holidayFile) {
		this.layoutFile = layoutFile;
		this.colors = colors;
		this.holidays = holidays;
		this.holidayLanguage = holidayLanguage;
		this.holidayFile = holidayFile;
	}

	public static CalendarResources load(File resourcesDirectory, String locale) throws IOException {
		return load(resourcesDirectory, locale, null);
	}

	public static CalendarResources load(File resourcesDirectory, String locale, String country) throws IOException {
		return load(resourcesDirectory, locale, country, null);
	}

	public static CalendarResources load(File resourcesDirectory, String locale, String country,
			File projectImageDirectory) throws IOException {
		return load(resourcesDirectory, null, locale, country, projectImageDirectory);
	}

	public static CalendarResources load(File resourcesDirectory, File writableResourcesDirectory, String locale,
			String country, File projectImageDirectory) throws IOException {
		if (locale != null && !locale.isEmpty() && !locale.matches("[A-Za-z]{2,3}_[A-Za-z]{2,3}")) {
			throw new IOException("Invalid calendar language: " + locale);
		}
		File entriesDirectory = new File(resourcesDirectory, "calendarentries");
		File writableEntriesDirectory = writableResourcesDirectory == null ? null
				: new File(writableResourcesDirectory, "calendarentries");
		File layoutFile = requiredFile(resourcesDirectory, "calendar_layout.xml");
		File schemaFile = requiredFile(resourcesDirectory, "calendar_schema.xml");
		File synonymFile = requiredFile(resourcesDirectory, "calendar_synonym.xml");
		HolidayCatalogue holidays;
		String holidayLanguage = null;
		File holidayFile;
		File legacyEntriesFile = CeweInstallation.mostCurrent(new File(resourcesDirectory, "calendarentries.xml"),
				writableResourcesDirectory == null ? null : new File(writableResourcesDirectory, "calendarentries.xml"));
		boolean entriesDirectoryExists = entriesDirectory.isDirectory()
				|| writableEntriesDirectory != null && writableEntriesDirectory.isDirectory();
		if (!entriesDirectoryExists && legacyEntriesFile.isFile()) {
			holidayFile = legacyEntriesFile;
			holidays = loadLegacyHolidayCatalogue(legacyEntriesFile, locale, country);
			if (holidays.getCountryAttributes().get("locale") == null) {
				holidayLanguage = installationLanguage(resourcesDirectory);
			}
		}
		else if (locale != null && !locale.isEmpty()) {
			holidayFile = entriesFile(entriesDirectory, writableEntriesDirectory, locale + ".xml");
			if (!holidayFile.isFile()) {
				throw new IOException("Required CEWE calendar resource is missing: " + holidayFile.getAbsolutePath());
			}
			holidays = new HolidayCatalogue(holidayFile);
		}
		else {
			holidayFile = findHolidayCatalogue(entriesDirectory, writableEntriesDirectory, country);
			holidays = new HolidayCatalogue(holidayFile);
		}
		if (holidayLanguage == null) {
			holidayLanguage = holidays.getCountryAttributes().get("locale");
		}
		return new CalendarResources(layoutFile,
				new CalendarColorCatalogue(schemaFile, synonymFile, projectImageDirectory), holidays, holidayLanguage,
				holidayFile);
	}

	private static File entriesFile(File entriesDirectory, File writableEntriesDirectory, String name) {
		return CeweInstallation.mostCurrent(new File(entriesDirectory, name),
				writableEntriesDirectory == null ? null : new File(writableEntriesDirectory, name));
	}

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

	private static File findHolidayCatalogue(File entriesDirectory, File writableEntriesDirectory,
			String country) throws IOException {
		if (country == null || country.trim().isEmpty()) {
			throw new IOException("Calendar language and holidays country are both missing");
		}
		Set<String> names = new TreeSet<String>();
		boolean readable = false;
		for (File directory : new File[] { entriesDirectory, writableEntriesDirectory }) {
			File[] files = directory == null ? null : directory.listFiles();
			if (files == null) {
				continue;
			}
			readable = true;
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xml")) {
					names.add(file.getName());
				}
			}
		}
		if (!readable) {
			throw new IOException("Cannot read CEWE calendar entries directory: " + entriesDirectory.getAbsolutePath());
		}
		List<File> matches = new ArrayList<File>();
		for (String name : names) {
			File file = entriesFile(entriesDirectory, writableEntriesDirectory, name);
			String countryName = new HolidayCatalogue(file).getCountryAttributes().get("iso");
			if (countryName != null && countryName.equalsIgnoreCase(country.trim())) {
				matches.add(file);
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
		if (layouts == null) {
			try {
				layouts = new CalendarLayoutCatalogue(layoutFile);
			}
			catch (IOException e) {
				throw new java.io.UncheckedIOException(e);
			}
		}
		return layouts;
	}

	public CalendarColorCatalogue getColors() {
		return colors;
	}

	public HolidayCatalogue getHolidays() {
		return holidays;
	}

	public File getHolidayFile() {
		return holidayFile;
	}

	public String getHolidayLanguage() {
		return holidayLanguage;
	}
}
