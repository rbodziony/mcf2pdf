package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CalendarResourcesTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void loadsLayoutColorsSynonymsAndRegionalHolidays() throws Exception {
		File resources = temporaryFolder.newFolder("Resources");
		write(resources, "calendar_layout.xml",
				"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><calendar_layout>"
				+ "<calendararea schema=\"Month\" type=\"CALENDAR_MONTH\" renderinfo=\"MONTH\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_MONTH\" textalign=\"AlignCenter\">"
				+ "<font size=\"22\" face=\"FranklinGothic\"/></cell>"
				+ "</calendararea></calendar_layout>");
		write(resources, "calendar_schema.xml",
				"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><calendar_schema>"
				+ "<calendararea schema=\"New colors\" gridcolor=\"#123456\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_SUNDAY\" textcolor=\"#ffffff\" bgcolor=\"#ff0000\"/>"
				+ "</calendararea></calendar_schema>");
		write(resources, "calendar_synonym.xml",
				"<synonyms><synonym old=\"Legacy colors\" new=\"New colors\"/></synonyms>");
		File entries = new File(resources, "calendarentries");
		Assert.assertTrue(entries.mkdir());
		Files.write(new File(entries, "pl_PL.xml").toPath(),
				("<holidays><country iso=\"Polska\" locale=\"pl_PL\">"
				+ "<nationwide><day id=\"19\" honor=\"free\" name=\"Nowy Rok\"/></nationwide>"
				+ "<province name=\"dolnośląskie\"><day date=\"XXXX-01-06\" id=\"2\" honor=\"show\" name=\"Święto\"/>"
				+ "<vacation name=\"Ferie\" start=\"2026-01-19\" end=\"2026-02-01\"/></province>"
				+ "</country></holidays>").getBytes(StandardCharsets.UTF_8));

		CalendarResources loaded = CalendarResources.load(resources, "pl_PL");

		CalendarLayoutCatalogue.Layout month = loaded.getLayouts().getLayout("Month");
		Assert.assertEquals("CALENDAR_MONTH", month.getType());
		Assert.assertEquals("AlignCenter",
				month.getCell("CALENDAR_CELL_TYPE_MONTH").getAttributes().get("textalign"));
		Assert.assertEquals("22",
				month.getCell("CALENDAR_CELL_TYPE_MONTH").getFontAttributes().get("size"));
		CalendarColorCatalogue.ColorSchema colors = loaded.getColors().getSchema("Legacy colors");
		Assert.assertEquals("New colors", colors.getSchema());
		Assert.assertEquals("#ff0000", colors.getCell("CALENDAR_CELL_TYPE_SUNDAY").get("bgcolor"));
		Assert.assertEquals("Polska", loaded.getHolidays().getCountryAttributes().get("iso"));
		Assert.assertEquals("19", loaded.getHolidays().getHolidays().get(0).getAttribute("id"));
		Assert.assertEquals("dolnośląskie", loaded.getHolidays().getHolidays().get(1).getProvince());
		Assert.assertEquals("Ferie", loaded.getHolidays().getVacations().get(0).getAttribute("name"));
	}

	@Test
	public void missingCatalogReportsItsExpectedPath() throws Exception {
		File resources = temporaryFolder.newFolder("missing-resources");

		try {
			CalendarResources.load(resources, "pl_PL");
			Assert.fail("Missing calendar catalog should fail");
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("calendar_layout.xml"));
		}
	}

	@Test
	public void localeCannotEscapeCalendarEntriesDirectory() throws Exception {
		File resources = temporaryFolder.newFolder("locale-resources");

		try {
			CalendarResources.load(resources, ".._..");
			Assert.fail("Invalid locale should fail");
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("Invalid calendar language"));
		}
	}

	@Test
	public void infersHolidayCatalogueFromCountryWhenProjectHasNoLanguage() throws Exception {
		File resources = temporaryFolder.newFolder("country-resources");
		write(resources, "calendar_layout.xml", "<calendar_layout/>");
		write(resources, "calendar_schema.xml", "<calendar_schema/>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");
		File entries = new File(resources, "calendarentries");
		Assert.assertTrue(entries.mkdir());
		Files.write(new File(entries, "pl_PL.xml").toPath(),
				"<holidays><country iso=\"Polska\" locale=\"pl_PL\"/></holidays>".getBytes(StandardCharsets.UTF_8));

		CalendarResources loaded = CalendarResources.load(resources, null, "Polska");

		Assert.assertEquals("pl_PL", loaded.getHolidays().getCountryAttributes().get("locale"));
	}

	private static void write(File directory, String name, String contents) throws IOException {
		Files.write(new File(directory, name).toPath(), contents.getBytes(StandardCharsets.ISO_8859_1));
	}
}
