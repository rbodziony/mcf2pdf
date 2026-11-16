package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfelements.impl.McfCalendarColorCellImpl;
import net.sf.mcf2pdf.mcfelements.impl.McfCalendarColorSchemaImpl;

public class CalendarResourcesTest {

	@Test
	public void loadsProjectSidecarColorsWithEmbeddedSchemaTakingPrecedence() throws Exception {
		File resources = temporaryFolder.newFolder("sidecar-resources");
		write(resources, "calendar_schema.xml", "<calendar_schema/>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");
		File images = temporaryFolder.newFolder("project_mcf-Dateien");
		write(images, "000-user-calendar-schema.xml", "<calendar_schema>"
				+ "<calendararea schema=\"USER_sample\" gridcolor=\"#ffffff\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_MONTH\" textcolor=\"#ffffff\" bgcolor=\"#00000000\"/>"
				+ "</calendararea></calendar_schema>");
		// Unrelated XML in the image directory must not be parsed.
		write(images, "unrelated.xml", "not XML");
		CalendarColorCatalogue catalogue = new CalendarColorCatalogue(new File(resources, "calendar_schema.xml"),
				new File(resources, "calendar_synonym.xml"), images);
		Assert.assertEquals("#ffffff", catalogue.getSchema("USER_sample")
				.getCell("CALENDAR_CELL_TYPE_MONTH").get("textcolor"));
		McfCalendarColorSchemaImpl embedded = new McfCalendarColorSchemaImpl();
		embedded.setSchema("USER_sample");
		McfCalendarColorCellImpl cell = new McfCalendarColorCellImpl();
		cell.setType("CALENDAR_CELL_TYPE_MONTH");
		cell.setTextColor("#123456");
		embedded.addCell(cell);
		Assert.assertEquals("#123456", catalogue.getSchema("USER_sample", java.util.Collections.singletonList(embedded))
				.getCell("CALENDAR_CELL_TYPE_MONTH").get("textcolor"));
		Assert.assertNull(new CalendarColorCatalogue(new File(resources, "calendar_schema.xml"),
				new File(resources, "calendar_synonym.xml"), new File(images, "missing"))
				.getSchema("USER_sample"));
	}

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
	public void userColorSchemaUsesCellOverridesAndDefaultColors() throws Exception {
		File resources = temporaryFolder.newFolder("user-schema-resources");
		write(resources, "calendar_layout.xml", "<calendar_layout/>");
		write(resources, "calendar_schema.xml", "<calendar_schema/>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");
		File entries = new File(resources, "calendarentries");
		Assert.assertTrue(entries.mkdir());
		Files.write(new File(entries, "pl_PL.xml").toPath(),
				"<holidays><country iso=\"Polska\" locale=\"pl_PL\"/></holidays>".getBytes(StandardCharsets.UTF_8));
		CalendarResources loaded = CalendarResources.load(resources, "pl_PL");

		McfCalendarColorSchemaImpl userSchema = new McfCalendarColorSchemaImpl();
		userSchema.setSchema("USER_custom");
		userSchema.setGridColor("#123456");
		McfCalendarColorCellImpl defaultCell = new McfCalendarColorCellImpl();
		defaultCell.setTextColor("#000000");
		defaultCell.setBackgroundColor("transparent");
		userSchema.setCellDefault(defaultCell);
		McfCalendarColorCellImpl monthCell = new McfCalendarColorCellImpl();
		monthCell.setType("CALENDAR_CELL_TYPE_MONTH");
		monthCell.setTextColor("#333333");
		userSchema.addCell(monthCell);

		CalendarColorCatalogue.ColorSchema resolved = loaded.getColors().getSchema("USER_custom",
				java.util.Collections.singletonList(userSchema));

		Assert.assertEquals("#123456", resolved.getGridColor());
		Assert.assertEquals("#333333", resolved.getCell("CALENDAR_CELL_TYPE_MONTH").get("textcolor"));
		Assert.assertEquals("transparent", resolved.getCell("CALENDAR_CELL_TYPE_MONTH").get("bgcolor"));
		Assert.assertEquals("#000000", resolved.getCell("CALENDAR_CELL_TYPE_YEAR").get("textcolor"));
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

	@Test
	public void loadsLegacySingleCalendarEntriesFileFromCewe5() throws Exception {
		File resources = temporaryFolder.newFolder("cewe5-resources");
		write(resources, "calendar_layout.xml", "<calendar_layout/>");
		write(resources, "calendar_schema.xml", "<calendar_schema/>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");
		Files.write(new File(resources, "calendarentries.xml").toPath(),
				("<holidays><country iso=\"Polska\"><nationwide>"
				+ "<day id=\"19\" honor=\"free\" name=\"Nowy Rok\"/>"
				+ "<day date=\"XXXX-11-11\" honor=\"free\" name=\"Święto Niepodległości\"/>"
				+ "</nationwide><province name=\"dolnośląskie\">"
				+ "<vacation name=\"Ferie zimowe\" start=\"2015-01-19\" end=\"2015-02-01\"/>"
				+ "</province></country></holidays>").getBytes(StandardCharsets.UTF_8));

		CalendarResources byLanguage = CalendarResources.load(resources, "pl_PL");
		Assert.assertEquals("Polska", byLanguage.getHolidays().getCountryAttributes().get("iso"));
		Assert.assertEquals(2, byLanguage.getHolidays().getHolidays().size());
		Assert.assertEquals("Ferie zimowe", byLanguage.getHolidays().getVacations().get(0).getAttribute("name"));

		CalendarResources byCountry = CalendarResources.load(resources, null, "Polska");
		Assert.assertEquals("Święto Niepodległości",
				byCountry.getHolidays().getHolidays().get(1).getAttribute("name"));

		try {
			CalendarResources.load(resources, null, "Deutschland");
			Assert.fail("Country mismatch should fail");
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("Deutschland"));
		}
	}

	@Test
	public void calendarEntriesDirectoryTakesPrecedenceOverLegacyFile() throws Exception {
		File resources = temporaryFolder.newFolder("mixed-resources");
		write(resources, "calendar_layout.xml", "<calendar_layout/>");
		write(resources, "calendar_schema.xml", "<calendar_schema/>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");
		write(resources, "calendarentries.xml", "<holidays><country iso=\"Legacy\"/></holidays>");
		File entries = new File(resources, "calendarentries");
		Assert.assertTrue(entries.mkdir());
		write(entries, "pl_PL.xml", "<holidays><country iso=\"Polska\" locale=\"pl_PL\"/></holidays>");

		Assert.assertEquals("Polska",
				CalendarResources.load(resources, "pl_PL").getHolidays().getCountryAttributes().get("iso"));
	}

	@Test
	public void loadsInstalledCewe518CalendarResources() throws Exception {
		File resources = new File("C:/Work/CEWEroznewersje/tmp/FOTOJOKER Fotoswiat 5.1.8/Resources");
		org.junit.Assume.assumeTrue(new File(resources, "calendarentries.xml").isFile());

		CalendarResources loaded = CalendarResources.load(resources, "pl_PL");

		HolidayCatalogue holidays = loaded.getHolidays();
		Assert.assertEquals("Polska", holidays.getCountryAttributes().get("iso"));
		Assert.assertEquals(24, holidays.getHolidays().size());
		Assert.assertEquals(64, holidays.getVacations().size());
		// calendar_schema.xml repeats bgcoloreven="#E1EFCE" in one cell of this schema.
		Assert.assertEquals("#E1EFCE", loaded.getColors().getSchema("Green/white nuances")
				.getCell("CALENDAR_CELL_TYPE_HEAD_EMPTY").get("bgcoloreven"));
	}

	@Test
	public void toleratesRepeatedIdenticalAttributes() throws Exception {
		File resources = temporaryFolder.newFolder("duplicate-attribute-resources");
		write(resources, "calendar_schema.xml", "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><calendar_schema>"
				+ "<calendararea schema=\"Grün\" gridcolor=\"#ffffff\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_EMPTY\" bgcoloreven=\"#E1EFCE\" bgcolorodd=\"#ffffff\""
				+ " bgcoloreven='#E1EFCE'/>"
				+ "</calendararea></calendar_schema>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");

		CalendarColorCatalogue colors = new CalendarColorCatalogue(new File(resources, "calendar_schema.xml"),
				new File(resources, "calendar_synonym.xml"));

		java.util.Map<String, String> cell = colors.getSchema("Grün").getCell("CALENDAR_CELL_TYPE_HEAD_EMPTY");
		Assert.assertEquals("#E1EFCE", cell.get("bgcoloreven"));
		Assert.assertEquals("#ffffff", cell.get("bgcolorodd"));
	}

	@Test
	public void rejectsRepeatedAttributesWithDifferentValues() throws Exception {
		File resources = temporaryFolder.newFolder("conflicting-attribute-resources");
		write(resources, "calendar_schema.xml", "<calendar_schema><calendararea schema=\"A\">"
				+ "<cell type=\"X\" bgcoloreven=\"#111111\" bgcoloreven=\"#222222\"/>"
				+ "</calendararea></calendar_schema>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");

		try {
			new CalendarColorCatalogue(new File(resources, "calendar_schema.xml"),
					new File(resources, "calendar_synonym.xml"));
			Assert.fail("Conflicting duplicate attributes should fail");
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("calendar_schema.xml"));
		}
	}

	private static void write(File directory, String name, String contents) throws IOException {
		Files.write(new File(directory, name).toPath(), contents.getBytes(StandardCharsets.ISO_8859_1));
	}
}
