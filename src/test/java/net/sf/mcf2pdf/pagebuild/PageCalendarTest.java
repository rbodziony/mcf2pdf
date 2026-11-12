package net.sf.mcf2pdf.pagebuild;

import java.time.LocalDate;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import net.sf.mcf2pdf.calendar.CalendarColorCatalogue;
import net.sf.mcf2pdf.calendar.CalendarDateModel;
import net.sf.mcf2pdf.calendar.HolidayCatalogue;
import net.sf.mcf2pdf.calendar.CalendarLayoutCatalogue;
import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfCalendarArea;
import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

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
				+ "</cell></calendararea>"
				+ "<calendararea schema=\"OneDayPerRow (1)\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_TOPLINE\" paddingleft=\"25\" paddingright=\"10\" "
				+ "textalign=\"AlignRight,AlignVCenter\" bordertype=\"CALENDAR_BORDER_BOTTOM\">"
				+ "<font size=\"13\" face=\"SansSerif\"/></cell>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_WEEKDAY\" paddingleft=\"25\" paddingright=\"10\" "
				+ "textalign=\"AlignRight,AlignVCenter\" bordertype=\"CALENDAR_BORDER_BOTTOM\">"
				+ "<font size=\"13\" face=\"SansSerif\"/></cell>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_SATURDAY\" paddingleft=\"25\" paddingright=\"10\" "
				+ "textalign=\"AlignRight,AlignVCenter\" bordertype=\"CALENDAR_BORDER_BOTTOM\">"
				+ "<font size=\"13\" face=\"SansSerif\"/></cell>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_SUNDAY\" paddingleft=\"25\" paddingright=\"10\" "
				+ "textalign=\"AlignRight,AlignVCenter\" bordertype=\"CALENDAR_BORDER_BOTTOM\">"
				+ "<font bold=\"true\" size=\"13\" face=\"SansSerif\"/></cell>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_WEEKDAY\" paddingleft=\"15\" "
				+ "bordertype=\"CALENDAR_BORDER_BOTTOM\"><font size=\"13\" face=\"SansSerif\"/></cell>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_SATURDAY\" paddingleft=\"15\" "
				+ "bordertype=\"CALENDAR_BORDER_BOTTOM\"><font size=\"13\" face=\"SansSerif\"/></cell>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_SUNDAY\" paddingleft=\"15\" "
				+ "bordertype=\"CALENDAR_BORDER_BOTTOM\"><font bold=\"true\" size=\"13\" face=\"SansSerif\"/></cell>"
				+ "</calendararea></calendar_layout>").getBytes(StandardCharsets.UTF_8));
		File schemaFile = temporaryFolder.newFile("calendar_schema.xml");
		Files.write(schemaFile.toPath(), ("<calendar_schema><calendararea schema=\"Gray\" gridcolor=\"#eeeeee\">"
				+ "<cell type=\"CALENDAR_CELL_TYPE_YEAR\" textcolor=\"#000000\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_MONTH\" textcolor=\"#333333\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_TOPLINE\" textcolor=\"#000000\" bgcolor=\"#ffffff\" "
				+ "bordercolor=\"#eeeeee\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_WEEKDAY\" textcolor=\"#000000\" bgcolor=\"#ffffff\" "
				+ "bordercolor=\"#eeeeee\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_SATURDAY\" textcolor=\"#000000\" bgcolor=\"#d2d2d2\" "
				+ "bordercolor=\"#eeeeee\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_SUNDAY\" textcolor=\"#000000\" bgcolor=\"#d2d2d2\" "
				+ "bordercolor=\"#eeeeee\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_WEEKDAY\" textcolor=\"#000000\" bgcolor=\"#cccccc\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_SATURDAY\" textcolor=\"#000000\" bgcolor=\"#b3b3b3\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_HEAD_SUNDAY\" textcolor=\"#000000\" bgcolor=\"#b3b3b3\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_WEEK_OF_YEAR\" textcolor=\"#000000\" bgcolor=\"#eeeeee\"/>"
				+ "<cell type=\"CALENDAR_CELL_TYPE_COMMENT\" textcolor=\"#000000\" bgcolor=\"transparent\"/>"
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

	@Test
	public void testOneDayPerRowRendersWeekdaysWeekendShadingHolidaysAndEvents() throws Exception {
		File holidayFile = temporaryFolder.newFile("calendarentries.xml");
		Files.write(holidayFile.toPath(), ("<calendarentries><country>"
				+ "<day date=\"XXXX-01-01\" name=\"New Year\" id=\"19\" honor=\"free\"/>"
				+ "</country></calendarentries>").getBytes(StandardCharsets.UTF_8));
		HolidayCatalogue holidays = new HolidayCatalogue(holidayFile);
		McfCalendarEvent event = Mockito.mock(McfCalendarEvent.class);
		Mockito.when(event.getName()).thenReturn("Birthday");
		Mockito.when(event.getDateTime()).thenReturn("2026-01-01");
		Mockito.when(event.getRecurrent()).thenReturn("no");
		CalendarDateModel dateModel = new CalendarDateModel(LocalDate.of(2026, 1, 1), "pl_PL", null, holidays,
				Collections.singletonList(event));

		McfCalendarArea calendarArea = schema("OneDayPerRow (1)");
		Mockito.when(calendarArea.getFont()).thenReturn("SansSerif");
		Mockito.when(calendarArea.getFontSizePercentage()).thenReturn(0);
		Mockito.when(calendarArea.getFontSizePercentageHolidays()).thenReturn(-50);
		Mockito.when(calendarArea.getShowHoliday()).thenReturn("1");
		McfArea area = Mockito.mock(McfArea.class);
		Mockito.when(area.getWidth()).thenReturn(900f);
		Mockito.when(area.getHeight()).thenReturn(1610f);

		PageCalendar table = PageCalendar.createFromDateModel("01.01.2026", "pl_PL", area, calendarArea,
				dateModel.getMonth(0), layouts, colors, Collections.<net.sf.mcf2pdf.mcfelements.McfCalendarColorSchema>emptyList());
		PageRenderContext context = new PageRenderContext(254, 1,
				Mockito.mock(McfResourceScanner.class), null);
		BufferedImage image = table.renderAsBitmap(context, new Point(), 0, 0);

		int firstRowY = Math.round(image.getHeight() / 31f * 0.5f);
		int saturdayRowY = Math.round(image.getHeight() / 31f * 2.5f);
		Assert.assertEquals(0xffffff, image.getRGB(image.getWidth() - 2, firstRowY) & 0xffffff);
		Assert.assertEquals(0xd2d2d2, image.getRGB(image.getWidth() - 2, saturdayRowY) & 0xffffff);
		Assert.assertTrue(countDarkPixels(image, image.getWidth() / 2, firstRowY - 10,
				image.getWidth() / 2 - 2, 20) > 0);
		Assert.assertTrue(countDarkPixels(image, 0, 0, image.getWidth() / 10, image.getHeight() / 5) > 0);
	}

	private static int countDarkPixels(BufferedImage image, int x, int y, int width, int height) {
		int count = 0;
		for (int px = Math.max(0, x); px < Math.min(image.getWidth(), x + width); px++) {
			for (int py = Math.max(0, y); py < Math.min(image.getHeight(), y + height); py++) {
				int rgb = image.getRGB(px, py) & 0xffffff;
				if (((rgb >> 16) & 0xff) < 100 && ((rgb >> 8) & 0xff) < 100 && (rgb & 0xff) < 100) {
					count++;
				}
			}
		}
		return count;
	}
}
