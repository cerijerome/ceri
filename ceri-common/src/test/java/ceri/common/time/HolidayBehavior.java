package ceri.common.time;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEnum;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;

public class HolidayBehavior {

	@Test
	public void shouldReturnDateForTheYear() {
		assertEquals(UsHoliday.christmasDay.day.date(2018), LocalDate.of(2018, 12, 25));
		assertEquals(UsHoliday.veteransDay.observed.date(2018), LocalDate.of(2018, 11, 12));
		assertEquals(UsHoliday.veteransDay.observed.date(2017), LocalDate.of(2017, 11, 10));
		assertEquals(UsHoliday.veteransDay.observed.date(2016), LocalDate.of(2016, 11, 11));
		assertEquals(UsHoliday.independenceDay.day.date(2018), LocalDate.of(2018, 7, 4));
		exerciseEnum(UsHoliday.class);
	}

	@Test
	public void shouldEncapsulateFixedDay() {
		assertEquals(Holiday.of(Month.FEBRUARY, 28).date(2018), LocalDate.of(2018, 2, 28));
		assertThrown(() -> Holiday.of(Month.FEBRUARY, 29).date(2018));
	}

	@Test
	public void shouldEncapsulateNthDayInTheMonth() {
		assertEquals(Holiday.nthDayInMonth(2, DayOfWeek.FRIDAY, Month.APRIL).date(2018),
			LocalDate.of(2018, 4, 13));
	}

	@Test
	public void shouldEncapsulateLastDayInTheMonth() {
		assertEquals(Holiday.lastDayInMonth(DayOfWeek.FRIDAY, Month.APRIL).date(2018),
			LocalDate.of(2018, 4, 27));
	}

}
