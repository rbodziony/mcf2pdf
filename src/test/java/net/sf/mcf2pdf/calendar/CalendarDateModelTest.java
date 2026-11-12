package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.calendar.CalendarDateModel.CalendarDay;
import net.sf.mcf2pdf.calendar.CalendarDateModel.CalendarMonth;
import net.sf.mcf2pdf.calendar.CalendarDateModel.HolidayOccurrence;
import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;
import net.sf.mcf2pdf.mcfelements.impl.McfCalendarEventImpl;

public class CalendarDateModelTest {

	private static final String[] EASTER_SUNDAYS = {
			"2010-04-04", "2011-04-24", "2012-04-08", "2013-03-31",
			"2014-04-20", "2015-04-05", "2016-03-27", "2017-04-16",
			"2018-04-01", "2019-04-21", "2020-04-12", "2021-04-04",
			"2022-04-17", "2023-04-09", "2024-03-31", "2025-04-20"
	};

	private static final int[] EASTER_RELATED_IDS = { 2, 3, 4, 5, 7, 8, 9, 10, 20, 21, 22 };

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void easterAndRelatedHolidaysMatch2010Through2025() throws Exception {
		HolidayCatalogue catalogue = catalogueForIds(EASTER_RELATED_IDS);
		for (int year = 2010; year <= 2025; year++) {
			CalendarDateModel model = model(year, catalogue, Collections.<McfCalendarEvent>emptyList());
			LocalDate easter = LocalDate.parse(EASTER_SUNDAYS[year - 2010]);

			Assert.assertEquals(easter, CalendarDateModel.easterSunday(year));
			assertHolidayId(model, easter.minusDays(3), 2);
			assertHolidayId(model, easter.minusDays(2), 3);
			assertHolidayId(model, easter, 4);
			assertHolidayId(model, easter.plusDays(1), 5);
			assertHolidayId(model, easter.plusDays(39), 7);
			assertHolidayId(model, easter.plusDays(49), 8);
			assertHolidayId(model, easter.plusDays(50), 9);
			assertHolidayId(model, easter.plusDays(60), 10);
			assertHolidayId(model, easter.minusDays(48), 20);
			assertHolidayId(model, easter.minusDays(46), 21);
			assertHolidayId(model, easter.minusDays(7), 22);
		}
	}

	@Test
	public void fixedHolidaysMatch2010Through2025() throws Exception {
		HolidayCatalogue catalogue = catalogueForIds(1, 6, 11, 12, 13, 15, 16, 17, 18, 19);
		for (int year = 2010; year <= 2025; year++) {
			CalendarDateModel model = model(year, catalogue, Collections.<McfCalendarEvent>emptyList());
			assertHolidayId(model, LocalDate.of(year, 1, 1), 19);
			assertHolidayId(model, LocalDate.of(year, 1, 6), 1);
			assertHolidayId(model, LocalDate.of(year, 5, 1), 6);
			assertHolidayId(model, LocalDate.of(year, 8, 15), 11);
			assertHolidayId(model, LocalDate.of(year, 10, 31), 12);
			assertHolidayId(model, LocalDate.of(year, 11, 1), 13);
			assertHolidayId(model, LocalDate.of(year, 12, 24), 15);
			assertHolidayId(model, LocalDate.of(year, 12, 25), 16);
			assertHolidayId(model, LocalDate.of(year, 12, 26), 17);
			assertHolidayId(model, LocalDate.of(year, 12, 31), 18);
		}
	}

	@Test
	public void wildcardFixedDatesResolveForEveryYear() throws Exception {
		HolidayCatalogue catalogue = catalogue(
				"<day date=\"XXXX-05-03\" name=\"May third\" honor=\"free\"/>"
				+ "<day date=\"XXXX-11-11\" name=\"November eleventh\" honor=\"free\"/>");
		for (int year = 2010; year <= 2025; year++) {
			CalendarDateModel model = model(year, catalogue, Collections.<McfCalendarEvent>emptyList());
			Assert.assertEquals("May third", model.getDay(LocalDate.of(year, 5, 3)).getHolidays().get(0).getName());
			Assert.assertEquals("November eleventh",
					model.getDay(LocalDate.of(year, 11, 11)).getHolidays().get(0).getName());
		}
	}

	@Test
	public void monthWeeksStartMondayAndUseIsoWeekNumbers() throws Exception {
		CalendarDateModel model = model(2026, emptyCatalogue(), Collections.<McfCalendarEvent>emptyList());
		CalendarMonth february = model.getMonth(1);

		Assert.assertEquals(YearMonth.of(2026, 2), february.getYearMonth());
		Assert.assertEquals(28, february.getDays().size());
		Assert.assertEquals(DayOfWeek.MONDAY, february.getWeeks().get(0).getMonday().getDayOfWeek());
		Assert.assertEquals(LocalDate.of(2026, 1, 26), february.getWeeks().get(0).getMonday());
		Assert.assertEquals(5, february.getWeeks().get(0).getIsoWeekNumber());
		Assert.assertEquals(7, february.getWeeks().get(0).getDays().size());
		Assert.assertEquals(5, model.getDay(LocalDate.of(2026, 2, 1)).getIsoWeekNumber());
		Assert.assertTrue(model.getDay(LocalDate.of(2026, 2, 1)).isSunday());
		Assert.assertTrue(model.getDay(LocalDate.of(2026, 2, 1)).isWeekend());
	}

	@Test
	public void monthOffsetsAndIsoWeekYearHandleYearBoundary() throws Exception {
		HolidayCatalogue catalogue = emptyCatalogue();
		CalendarDateModel model = model(2026, catalogue, Collections.<McfCalendarEvent>emptyList());

		Assert.assertEquals(YearMonth.of(2027, 1), model.getMonth(12).getYearMonth());
		CalendarDay newYear = model.getDay(LocalDate.of(2021, 1, 1));
		Assert.assertEquals(53, newYear.getIsoWeekNumber());
		Assert.assertEquals(2020, newYear.getIsoWeekBasedYear());

		CalendarDateModel december = new CalendarDateModel(LocalDate.of(2026, 12, 1), "pl_PL", null,
				catalogue, Collections.<McfCalendarEvent>emptyList());
		Assert.assertEquals(YearMonth.of(2026, 12), december.getMonth(0).getYearMonth());
		Assert.assertEquals(YearMonth.of(2027, 1), december.getMonth(1).getYearMonth());
	}

	@Test
	public void leapFebruaryHasTwentyNineDays() throws Exception {
		CalendarDateModel model = new CalendarDateModel(LocalDate.of(2028, 1, 1), "pl_PL", null,
				emptyCatalogue(), Collections.<McfCalendarEvent>emptyList());

		Assert.assertEquals(29, model.getMonth(1).getDays().size());
		Assert.assertEquals(LocalDate.of(2028, 2, 29), model.getMonth(1).getLastDay());
	}

	@Test
	public void localizedNamesUseCewePolishAbbreviationsAndFallbackElsewhere() {
		Assert.assertEquals("Styczeń", CalendarDateModel.monthName(Month.JANUARY, "pl_PL"));
		Assert.assertEquals("Czw", CalendarDateModel.weekdayAbbreviation(DayOfWeek.THURSDAY, "pl_PL"));
		Assert.assertEquals("Nd", CalendarDateModel.weekdayAbbreviation(DayOfWeek.SUNDAY, "pl_PL"));
		Assert.assertEquals("Śr", CalendarDateModel.weekdayAbbreviation(DayOfWeek.WEDNESDAY, "pl_PL"));
		Assert.assertEquals("January", CalendarDateModel.monthName(Month.JANUARY, "en_US"));
		Assert.assertEquals("Thu", CalendarDateModel.weekdayAbbreviation(DayOfWeek.THURSDAY, "en_US"));
	}

	@Test
	public void eventsSupportExactAndAnnualDates() throws Exception {
		McfCalendarEventImpl annual = event("annual", "2026-03-20T09:30:00+01:00", "yes");
		McfCalendarEventImpl exact = event("exact", "2026-03-21T09:30:00", "no");
		CalendarDateModel model = model(2026, emptyCatalogue(), Arrays.asList(annual, exact));

		Assert.assertEquals(1, model.getDay(LocalDate.of(2026, 3, 20)).getEvents().size());
		Assert.assertEquals(1, model.getDay(LocalDate.of(2026, 3, 21)).getEvents().size());
		Assert.assertEquals("exact", model.getDay(LocalDate.of(2026, 3, 21)).getEvents().get(0).getName());
		Assert.assertEquals(1, model.getDay(LocalDate.of(2027, 3, 20)).getEvents().size());
		Assert.assertEquals("annual", model.getDay(LocalDate.of(2027, 3, 20)).getEvents().get(0).getName());
		Assert.assertTrue(model.getDay(LocalDate.of(2027, 3, 21)).getEvents().isEmpty());
	}

	@Test
	public void holidaysRespectProvinceAndHonor() throws Exception {
		HolidayCatalogue catalogue = catalogue(
				"<nationwide><day date=\"XXXX-05-03\" name=\"national\" honor=\"free\"/></nationwide>"
				+ "<province name=\"Mazowieckie\"><day date=\"XXXX-05-03\" name=\"regional\" honor=\"show\"/></province>"
				+ "<province name=\"Pomorskie\"><day date=\"XXXX-05-03\" name=\"ignored\" honor=\"ignore\"/></province>");
		CalendarDateModel model = new CalendarDateModel(LocalDate.of(2026, 1, 1), "pl_PL", "Mazowieckie",
				catalogue, Collections.<McfCalendarEvent>emptyList());
		CalendarDay day = model.getDay(LocalDate.of(2026, 5, 3));

		Assert.assertEquals(2, day.getHolidays().size());
		Assert.assertTrue(day.isHoliday());
		Assert.assertTrue(day.isDayOff());
		Assert.assertEquals("Mazowieckie", day.getHolidays().get(1).getProvince());
		Assert.assertTrue(day.getHolidays().get(1).isVisible());
		Assert.assertFalse(day.getHolidays().get(1).isDayOff());
	}

	@Test
	public void unknownHolidayIdsAreReportedWithoutFailing() throws Exception {
		CalendarDateModel model = model(2026, catalogue("<day id=\"999\" name=\"unknown\" honor=\"show\"/>"),
				Collections.<McfCalendarEvent>emptyList());

		Assert.assertEquals(Collections.singleton("999"), model.getUnsupportedHolidayIds());
		Assert.assertTrue(model.getDay(LocalDate.of(2026, 1, 1)).getHolidays().isEmpty());
	}

	@Test
	public void fixedDateDoesNotDependOnItsOptionalId() throws Exception {
		HolidayCatalogue catalogue = catalogue(
				"<day id=\"999\" date=\"XXXX-05-03\" name=\"fixed\" honor=\"free\"/>");
		CalendarDateModel model = model(2026, catalogue, Collections.<McfCalendarEvent>emptyList());

		Assert.assertTrue(model.getUnsupportedHolidayIds().isEmpty());
		Assert.assertEquals(1, model.getDay(LocalDate.of(2026, 5, 3)).getHolidays().size());
	}

	@Test
	public void startDateParsingIsStrict() {
		Assert.assertEquals(LocalDate.of(2026, 1, 1), CalendarDateModel.parseStartDate("01.01.2026"));
		Assert.assertNull(CalendarDateModel.parseStartDate("31.02.2026"));
		Assert.assertNull(CalendarDateModel.parseStartDate("2026-01-01"));
	}

	private CalendarDateModel model(int year, HolidayCatalogue catalogue, List<? extends McfCalendarEvent> events) {
		return new CalendarDateModel(LocalDate.of(year, 1, 1), "pl_PL", null, catalogue, events);
	}

	private HolidayCatalogue emptyCatalogue() throws Exception {
		return catalogue("");
	}

	private HolidayCatalogue catalogueForIds(int... ids) throws Exception {
		StringBuilder entries = new StringBuilder();
		for (int id : ids) {
			entries.append("<day id=\"").append(id).append("\" name=\"holiday-").append(id)
					.append("\" honor=\"free\"/>");
		}
		return catalogue(entries.toString());
	}

	private HolidayCatalogue catalogue(String entries) throws Exception {
		File file = temporaryFolder.newFile();
		String xml = "<holidays><country>" + entries + "</country></holidays>";
		Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
		return new HolidayCatalogue(file);
	}

	private static McfCalendarEventImpl event(String name, String dateTime, String recurrent) {
		McfCalendarEventImpl event = new McfCalendarEventImpl();
		event.setName(name);
		event.setDateTime(dateTime);
		event.setRecurrent(recurrent);
		return event;
	}

	private static void assertHolidayId(CalendarDateModel model, LocalDate date, int expectedId) {
		for (HolidayOccurrence holiday : model.getDay(date).getHolidays()) {
			if (String.valueOf(expectedId).equals(holiday.getAttribute("id"))) {
				return;
			}
		}
		Assert.fail("Expected CEWE holiday id " + expectedId + " on " + date);
	}
}
