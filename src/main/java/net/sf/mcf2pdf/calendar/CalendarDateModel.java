package net.sf.mcf2pdf.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.sf.mcf2pdf.calendar.HolidayCatalogue.Holiday;
import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;

/** Builds calendar months, ISO weeks, days, holidays, and user events from project data. */
public final class CalendarDateModel {

	private static final DateTimeFormatter START_DATE_FORMAT =
			DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
	private static final String[] POLISH_MONTHS = {
			"Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
			"Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
	};
	private static final String[] POLISH_WEEKDAYS = { "Pn", "Wt", "Śr", "Czw", "Pt", "Sb", "Nd" };

	private final YearMonth startMonth;
	private final Locale locale;
	private final String selectedProvince;
	private final HolidayCatalogue holidays;
	private final List<EventDate> events;
	private final Set<String> unsupportedHolidayIds;

	public CalendarDateModel(LocalDate startDate, String language, String province, HolidayCatalogue holidays,
			Collection<? extends McfCalendarEvent> calendarEvents) {
		if (startDate == null) {
			throw new IllegalArgumentException("Calendar start date is required");
		}
		if (holidays == null) {
			throw new IllegalArgumentException("Calendar holiday catalogue is required");
		}
		startMonth = YearMonth.from(startDate);
		locale = parseLocale(language);
		selectedProvince = province == null ? null : province.trim();
		this.holidays = holidays;
		events = parseEvents(calendarEvents);
		unsupportedHolidayIds = findUnsupportedHolidayIds(holidays.getHolidays(), startMonth.getYear());
	}

	public static LocalDate parseStartDate(String value) {
		if (value == null) {
			return null;
		}
		try {
			return LocalDate.parse(value, START_DATE_FORMAT);
		}
		catch (DateTimeParseException e) {
			return null;
		}
	}

	public static LocalDate easterSunday(int year) {
		if (year < 1583) {
			throw new IllegalArgumentException("Gregorian Easter is not defined by this algorithm before 1583");
		}
		int a = year % 19;
		int b = year / 100;
		int c = year % 100;
		int d = b / 4;
		int e = b % 4;
		int f = (b + 8) / 25;
		int g = (b - f + 1) / 3;
		int h = (19 * a + b - d - g + 15) % 30;
		int i = c / 4;
		int k = c % 4;
		int l = (32 + 2 * e + 2 * i - h - k) % 7;
		int m = (a + 11 * h + 22 * l) / 451;
		int month = (h + l - 7 * m + 114) / 31;
		int day = (h + l - 7 * m + 114) % 31 + 1;
		return LocalDate.of(year, month, day);
	}

	public static String monthName(Month month, String language) {
		if (month == null) {
			throw new IllegalArgumentException("Month is required");
		}
		if ("pl".equals(parseLocale(language).getLanguage())) {
			return POLISH_MONTHS[month.getValue() - 1];
		}
		Locale locale = parseLocale(language);
		String name = month.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, locale);
		return capitalize(name, locale);
	}

	public static String weekdayAbbreviation(DayOfWeek day, String language) {
		if (day == null) {
			throw new IllegalArgumentException("Weekday is required");
		}
		if ("pl".equals(parseLocale(language).getLanguage())) {
			return POLISH_WEEKDAYS[day.getValue() - 1];
		}
		return day.getDisplayName(java.time.format.TextStyle.SHORT, parseLocale(language));
	}

	public CalendarMonth getMonth(int monthOffset) {
		if (monthOffset < 0) {
			throw new IllegalArgumentException("Calendar month offset must not be negative");
		}
		return new CalendarMonth(this, startMonth.plusMonths(monthOffset));
	}

	public CalendarDay getDay(LocalDate date) {
		if (date == null) {
			throw new IllegalArgumentException("Calendar date is required");
		}
		int weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear());
		int weekYear = date.get(WeekFields.ISO.weekBasedYear());
		return createDay(date, YearMonth.from(date), weekNumber, weekYear);
	}

	public Set<String> getUnsupportedHolidayIds() {
		return unsupportedHolidayIds;
	}

	private CalendarDay createDay(LocalDate date, YearMonth month, int weekNumber, int weekYear) {
		return new CalendarDay(date, YearMonth.from(date).equals(month), weekNumber, weekYear,
				resolveHolidays(date), resolveEvents(date));
	}

	private List<HolidayOccurrence> resolveHolidays(LocalDate date) {
		List<HolidayOccurrence> result = new ArrayList<HolidayOccurrence>();
		for (Holiday holiday : holidays.getHolidays()) {
			if (!provinceMatches(holiday.getProvince())) {
				continue;
			}
			LocalDate occurrence = resolveHolidayDate(holiday, date.getYear());
			if (date.equals(occurrence)) {
				result.add(new HolidayOccurrence(holiday, occurrence));
			}
		}
		return Collections.unmodifiableList(result);
	}

	private boolean provinceMatches(String holidayProvince) {
		return holidayProvince == null || holidayProvince.isEmpty()
				|| selectedProvince != null && holidayProvince.equalsIgnoreCase(selectedProvince);
	}

	private List<McfCalendarEvent> resolveEvents(LocalDate date) {
		List<McfCalendarEvent> result = new ArrayList<McfCalendarEvent>();
		for (EventDate event : events) {
			if (event.isRecurrent
					? event.date.getMonthValue() == date.getMonthValue() && event.date.getDayOfMonth() == date.getDayOfMonth()
					: event.date.equals(date)) {
				result.add(event.event);
			}
		}
		return Collections.unmodifiableList(result);
	}

	private static LocalDate resolveHolidayDate(Holiday holiday, int year) {
		String date = holiday.getAttribute("date");
		if (date != null && !date.isEmpty()) {
			if (date.matches("XXXX-\\d{2}-\\d{2}")) {
				return LocalDate.of(year, Integer.parseInt(date.substring(5, 7)),
						Integer.parseInt(date.substring(8, 10)));
			}
			try {
				LocalDate parsed = LocalDate.parse(date);
				return parsed.getYear() == year ? parsed : null;
			}
			catch (DateTimeParseException e) {
				throw new IllegalArgumentException("Invalid CEWE calendar holiday date format");
			}
		}

		String id = holiday.getAttribute("id");
		if (id == null || id.isEmpty()) {
			return null;
		}
		final int holidayId;
		try {
			holidayId = Integer.parseInt(id);
		}
		catch (NumberFormatException e) {
			return null;
		}
		LocalDate easter = easterSunday(year);
		switch (holidayId) {
		case 1:
			return LocalDate.of(year, Month.JANUARY, 6);
		case 2:
			return easter.minusDays(3);
		case 3:
			return easter.minusDays(2);
		case 4:
			return easter;
		case 5:
			return easter.plusDays(1);
		case 6:
			return LocalDate.of(year, Month.MAY, 1);
		case 7:
			return easter.plusDays(39);
		case 8:
			return easter.plusDays(49);
		case 9:
			return easter.plusDays(50);
		case 10:
			return easter.plusDays(60);
		case 11:
			return LocalDate.of(year, Month.AUGUST, 15);
		case 12:
			return LocalDate.of(year, Month.OCTOBER, 31);
		case 13:
			return LocalDate.of(year, Month.NOVEMBER, 1);
		case 14:
			return LocalDate.of(year, Month.NOVEMBER, 23)
					.with(TemporalAdjusters.previous(DayOfWeek.WEDNESDAY));
		case 15:
			return LocalDate.of(year, Month.DECEMBER, 24);
		case 16:
			return LocalDate.of(year, Month.DECEMBER, 25);
		case 17:
			return LocalDate.of(year, Month.DECEMBER, 26);
		case 18:
			return LocalDate.of(year, Month.DECEMBER, 31);
		case 19:
			return LocalDate.of(year, Month.JANUARY, 1);
		case 20:
			return easter.minusDays(48);
		case 21:
			return easter.minusDays(46);
		case 22:
			return easter.minusDays(7);
		default:
			return null;
		}
	}

	private static Set<String> findUnsupportedHolidayIds(List<Holiday> holidays, int year) {
		Set<String> result = new LinkedHashSet<String>();
		for (Holiday holiday : holidays) {
			String id = holiday.getAttribute("id");
			String date = holiday.getAttribute("date");
			if (id != null && (date == null || date.isEmpty())
					&& resolveHolidayDate(holiday, year) == null) {
				result.add(id);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	private static List<EventDate> parseEvents(Collection<? extends McfCalendarEvent> calendarEvents) {
		List<EventDate> result = new ArrayList<EventDate>();
		if (calendarEvents == null) {
			return result;
		}
		int index = 0;
		for (McfCalendarEvent event : calendarEvents) {
			if (event == null) {
				throw new IllegalArgumentException("Calendar event at index " + index + " is null");
			}
			LocalDate date = parseEventDate(event.getDateTime());
			if (date == null) {
				throw new IllegalArgumentException("Calendar event at index " + index + " has an invalid DateTime");
			}
			result.add(new EventDate(event, date, "yes".equalsIgnoreCase(trim(event.getRecurrent()))));
			index++;
		}
		return Collections.unmodifiableList(result);
	}

	private static LocalDate parseEventDate(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		String trimmed = value.trim();
		try {
			return LocalDate.from(DateTimeFormatter.ISO_DATE_TIME.parse(trimmed));
		}
		catch (DateTimeParseException e) {
			try {
				return LocalDate.parse(trimmed);
			}
			catch (DateTimeParseException ignored) {
				return null;
			}
		}
	}

	private static Locale parseLocale(String language) {
		return language == null || language.trim().isEmpty()
				? Locale.getDefault()
				: Locale.forLanguageTag(language.replace('_', '-'));
	}

	private static String capitalize(String value, Locale locale) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return value.substring(0, 1).toUpperCase(locale) + value.substring(1);
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

	private static final class EventDate {
		private final McfCalendarEvent event;
		private final LocalDate date;
		private final boolean isRecurrent;

		private EventDate(McfCalendarEvent event, LocalDate date, boolean isRecurrent) {
			this.event = event;
			this.date = date;
			this.isRecurrent = isRecurrent;
		}
	}

	public static final class CalendarMonth {
		private final YearMonth yearMonth;
		private final String name;
		private final List<CalendarWeek> weeks;
		private final List<CalendarDay> days;

		private CalendarMonth(CalendarDateModel model, YearMonth yearMonth) {
			this.yearMonth = yearMonth;
			name = monthName(yearMonth.getMonth(), model.locale.toLanguageTag());
			List<CalendarWeek> parsedWeeks = new ArrayList<CalendarWeek>();
			List<CalendarDay> parsedDays = new ArrayList<CalendarDay>();
			LocalDate firstMonday = yearMonth.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			LocalDate lastSunday = yearMonth.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
			for (LocalDate monday = firstMonday; !monday.isAfter(lastSunday); monday = monday.plusWeeks(1)) {
				int weekNumber = monday.get(WeekFields.ISO.weekOfWeekBasedYear());
				int weekYear = monday.get(WeekFields.ISO.weekBasedYear());
				List<CalendarDay> weekDays = new ArrayList<CalendarDay>(7);
				for (int i = 0; i < 7; i++) {
					LocalDate date = monday.plusDays(i);
					CalendarDay day = model.createDay(date, yearMonth, weekNumber, weekYear);
					weekDays.add(day);
					if (day.isInMonth()) {
						parsedDays.add(day);
					}
				}
				parsedWeeks.add(new CalendarWeek(monday, weekNumber, weekYear, weekDays));
			}
			weeks = Collections.unmodifiableList(parsedWeeks);
			days = Collections.unmodifiableList(parsedDays);
		}

		public YearMonth getYearMonth() {
			return yearMonth;
		}

		public String getName() {
			return name;
		}

		public LocalDate getFirstDay() {
			return yearMonth.atDay(1);
		}

		public LocalDate getLastDay() {
			return yearMonth.atEndOfMonth();
		}

		public List<CalendarWeek> getWeeks() {
			return weeks;
		}

		public List<CalendarDay> getDays() {
			return days;
		}
	}

	public static final class CalendarWeek {
		private final LocalDate monday;
		private final int isoWeekNumber;
		private final int isoWeekBasedYear;
		private final List<CalendarDay> days;

		private CalendarWeek(LocalDate monday, int isoWeekNumber, int isoWeekBasedYear, List<CalendarDay> days) {
			this.monday = monday;
			this.isoWeekNumber = isoWeekNumber;
			this.isoWeekBasedYear = isoWeekBasedYear;
			this.days = Collections.unmodifiableList(new ArrayList<CalendarDay>(days));
		}

		public LocalDate getMonday() {
			return monday;
		}

		public int getIsoWeekNumber() {
			return isoWeekNumber;
		}

		public int getIsoWeekBasedYear() {
			return isoWeekBasedYear;
		}

		public List<CalendarDay> getDays() {
			return days;
		}
	}

	public static final class CalendarDay {
		private final LocalDate date;
		private final boolean inMonth;
		private final int isoWeekNumber;
		private final int isoWeekBasedYear;
		private final List<HolidayOccurrence> holidays;
		private final List<McfCalendarEvent> events;

		private CalendarDay(LocalDate date, boolean inMonth, int isoWeekNumber, int isoWeekBasedYear,
				List<HolidayOccurrence> holidays, List<McfCalendarEvent> events) {
			this.date = date;
			this.inMonth = inMonth;
			this.isoWeekNumber = isoWeekNumber;
			this.isoWeekBasedYear = isoWeekBasedYear;
			this.holidays = holidays;
			this.events = events;
		}

		public LocalDate getDate() {
			return date;
		}

		public DayOfWeek getDayOfWeek() {
			return date.getDayOfWeek();
		}

		public String getWeekdayAbbreviation(String language) {
			return CalendarDateModel.weekdayAbbreviation(getDayOfWeek(), language);
		}

		public boolean isInMonth() {
			return inMonth;
		}

		public boolean isWeekend() {
			return getDayOfWeek() == DayOfWeek.SATURDAY || getDayOfWeek() == DayOfWeek.SUNDAY;
		}

		public boolean isSunday() {
			return getDayOfWeek() == DayOfWeek.SUNDAY;
		}

		public int getIsoWeekNumber() {
			return isoWeekNumber;
		}

		public int getIsoWeekBasedYear() {
			return isoWeekBasedYear;
		}

		public List<HolidayOccurrence> getHolidays() {
			return holidays;
		}

		public boolean isHoliday() {
			for (HolidayOccurrence holiday : holidays) {
				if (holiday.isVisible()) {
					return true;
				}
			}
			return false;
		}

		public boolean isDayOff() {
			for (HolidayOccurrence holiday : holidays) {
				if (holiday.isDayOff()) {
					return true;
				}
			}
			return false;
		}

		public List<McfCalendarEvent> getEvents() {
			return events;
		}
	}

	public static final class HolidayOccurrence {
		private final Holiday holiday;
		private final LocalDate date;

		private HolidayOccurrence(Holiday holiday, LocalDate date) {
			this.holiday = holiday;
			this.date = date;
		}

		public LocalDate getDate() {
			return date;
		}

		public String getName() {
			return holiday.getAttribute("name");
		}

		public String getHonor() {
			return holiday.getAttribute("honor");
		}

		public String getProvince() {
			return holiday.getProvince();
		}

		public String getAttribute(String name) {
			return holiday.getAttribute(name);
		}

		public boolean isVisible() {
			return !"ignore".equalsIgnoreCase(getHonor());
		}

		public boolean isDayOff() {
			return "free".equalsIgnoreCase(getHonor());
		}
	}
}
