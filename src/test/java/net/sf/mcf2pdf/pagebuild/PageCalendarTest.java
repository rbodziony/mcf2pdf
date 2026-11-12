package net.sf.mcf2pdf.pagebuild;

import java.time.LocalDate;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import net.sf.mcf2pdf.calendar.CalendarColorCatalogue;
import net.sf.mcf2pdf.calendar.CalendarLayoutCatalogue;
import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfCalendarArea;

public class PageCalendarTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private CalendarLayoutCatalogue layouts;
	private CalendarColorCatalogue colors;

	@Before
	public void setUp() throws Exception {
		File layoutFile = temporaryFolder.newFile("calendar_layout.xml");
		Files.write(layoutFile.toPath(), ("<calendar_layout>"
				+ "<calendararea schema=\"Year (Long-Name-Big)\"><cell type=\"CALENDAR_CELL_TYPE_YEAR\">"
				+ "<font bold=\"true\" size=\"64\" face=\"FranklinGothic\"/></cell></calendararea>"
				+ "<calendararea schema=\"Month\"><cell type=\"CALENDAR_CELL_TYPE_MONTH\" "
				+ "textalign=\"AlignTop,AlignLeft\"><font size=\"22\" face=\"FranklinGothic\" bold=\"true\"/>"
				+ "</cell></calendararea>"
				+ "<calendararea schema=\"Month_center\"><cell type=\"CALENDAR_CELL_TYPE_MONTH\" "
				+ "textalign=\"AlignTop,AlignHCenter\"><font size=\"22\" face=\"FranklinGothic\" bold=\"true\"/>"
				+ "</cell></calendararea></calendar_layout>").getBytes(StandardCharsets.UTF_8));
		File schemaFile = temporaryFolder.newFile("calendar_schema.xml");
		Files.write(schemaFile.toPath(), ("<calendar_schema><calendararea schema=\"Gray\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_YEAR\" textcolor=\"#000000\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_MONTH\" textcolor=\"#333333\"/>"
				+ "</calendararea></calendar_schema>").getBytes(StandardCharsets.UTF_8));
		File synonymFile = temporaryFolder.newFile("calendar_synonym.xml");
		Files.write(synonymFile.toPath(), "<synonyms/>".getBytes(StandardCharsets.UTF_8));
		layouts = new CalendarLayoutCatalogue(layoutFile);
		colors = new CalendarColorCatalogue(schemaFile, synonymFile);
	}

	private static McfCalendarArea schema(String layoutSchema) {
		McfCalendarArea calendar = Mockito.mock(McfCalendarArea.class);
		Mockito.when(calendar.getLayoutSchema()).thenReturn(layoutSchema);
		Mockito.when(calendar.getColorSchema()).thenReturn("Gray");
		return calendar;
	}

	@Test
	public void testYearLayoutShowsYearOfStartDate() {
		McfCalendarArea calendar = schema("Year (Long-Name-Big)");
		Mockito.when(calendar.getFontSizePercentage()).thenReturn(-20);
		PageCalendar year = PageCalendar.create("01.01.2026", "pl_PL", Mockito.mock(McfArea.class),
				calendar, null, layouts, colors);

		Assert.assertEquals("2026", year.getText());
		Assert.assertEquals("FranklinGothic", year.getFontFamily());
		Assert.assertTrue(year.isBold());
		Assert.assertEquals(51.2f, year.getFontSizePoints(), 0.01f);
		Assert.assertEquals("AlignVCenter,AlignHCenter", year.getTextAlignment());
		Assert.assertEquals("#000000", year.getTextColor());
	}

	@Test
	public void testMonthLayoutShowsCapitalizedPolishMonthName() {
		McfArea area = Mockito.mock(McfArea.class);
		McfCalendarArea monthCalendar = schema("Month");
		Mockito.when(monthCalendar.getFontSizePercentage()).thenReturn(20);
		McfCalendarArea centeredCalendar = schema("Month_center");

		Assert.assertEquals("Styczeń",
				PageCalendar.create("01.01.2019", "pl_PL", area, monthCalendar, LocalDate.of(2019, 1, 1),
						layouts, colors).getText());
		Assert.assertEquals("Luty",
				PageCalendar.create("01.01.2019", "pl_PL", area, centeredCalendar, LocalDate.of(2019, 2, 1),
						layouts, colors).getText());
		PageCalendar month = PageCalendar.create("01.01.2019", "pl_PL", area, monthCalendar,
				LocalDate.of(2019, 1, 1), layouts, colors);
		PageCalendar centered = PageCalendar.create("01.01.2019", "pl_PL", area, centeredCalendar,
				LocalDate.of(2019, 2, 1), layouts, colors);
		Assert.assertEquals(26.4f, month.getFontSizePoints(), 0.01f);
		Assert.assertEquals("#333333", month.getTextColor());
		Assert.assertEquals("AlignTop,AlignLeft", month.getTextAlignment());
		Assert.assertEquals("AlignTop,AlignHCenter", centered.getTextAlignment());
	}

	@Test
	public void testMonthWithoutSheetMonthAndUnsupportedLayoutsAreSkipped() {
		McfArea area = Mockito.mock(McfArea.class);

		Assert.assertNull(PageCalendar.create("01.01.2019", "pl_PL", area, schema("Month"), null));
		Assert.assertNull(PageCalendar.create("01.01.2019", "pl_PL", area, schema("OneDayPerRow (1)"),
				LocalDate.of(2019, 1, 1)));
		Assert.assertNull(PageCalendar.create("not a date", "pl_PL", area, schema("Year (Long-Name-Big)"), null));
	}

	@Test
	public void testParseStartDate() {
		Assert.assertEquals(LocalDate.of(2026, 1, 1), PageCalendar.parse("01.01.2026"));
		Assert.assertNull(PageCalendar.parse(null));
		Assert.assertNull(PageCalendar.parse("2026-01-01"));
	}
}
