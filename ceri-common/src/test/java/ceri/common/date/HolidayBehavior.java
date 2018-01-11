package ceri.common.date;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;

public class HolidayBehavior {

	@Test
	public void shouldReturnDateForTheYear() {
		assertThat(UsHoliday.christmasDay.day.date(2018), is(LocalDate.of(2018, 12, 25)));
		assertThat(UsHoliday.veteransDay.observed.date(2018), is(LocalDate.of(2018, 11, 11)));
		assertThat(UsHoliday.veteransDay.observed.date(2017), is(LocalDate.of(2017, 11, 10)));
		assertThat(UsHoliday.veteransDay.observed.date(2016), is(LocalDate.of(2016, 11, 11)));
		assertThat(UsHoliday.independenceDay.day.date(2018), is(LocalDate.of(2018, 7, 4)));
		exerciseEnum(UsHoliday.class);
	}

	@Test
	public void shouldEncapsulateFixedDay() {
		assertThat(Holiday.of(Month.FEBRUARY, 28).date(2018), is(LocalDate.of(2018, 2, 28)));
		assertException(() -> Holiday.of(Month.FEBRUARY, 29).date(2018));
	}

	@Test
	public void shouldEncapsulateNthDayInTheMonth() {
		assertThat(Holiday.nthDayInMonth(2, DayOfWeek.FRIDAY, Month.APRIL).date(2018),
			is(LocalDate.of(2018, 4, 13)));
	}

	@Test
	public void shouldEncapsulateLastDayInTheMonth() {
		assertThat(Holiday.lastDayInMonth(DayOfWeek.FRIDAY, Month.APRIL).date(2018),
			is(LocalDate.of(2018, 4, 27)));
	}

}
