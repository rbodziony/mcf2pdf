package net.sf.mcf2pdf.pagebuild;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfCalendarArea;

public class PageCalendarTest {

	private static McfCalendarArea schema(String layoutSchema) {
		McfCalendarArea calendar = Mockito.mock(McfCalendarArea.class);
		Mockito.when(calendar.getLayoutSchema()).thenReturn(layoutSchema);
		return calendar;
	}

	@Test
	public void testYearLayoutShowsYearOfStartDate() {
		PageCalendar year = PageCalendar.create("01.01.2026", "pl_PL", Mockito.mock(McfArea.class),
				schema("Year (Long-Name-Big)"), null);

		Assert.assertEquals("2026", year.getText());
	}

	@Test
	public void testMonthLayoutShowsCapitalizedPolishMonthName() {
		McfArea area = Mockito.mock(McfArea.class);

		Assert.assertEquals("Styczeń",
				PageCalendar.create("01.01.2019", "pl_PL", area, schema("Month"), LocalDate.of(2019, 1, 1)).getText());
		Assert.assertEquals("Luty",
				PageCalendar.create("01.01.2019", "pl_PL", area, schema("Month_center"), LocalDate.of(2019, 2, 1)).getText());
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
