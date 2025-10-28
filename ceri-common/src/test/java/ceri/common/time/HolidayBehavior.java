package ceri.common.time;

import static ceri.common.test.Testing.exerciseEnum;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;
import ceri.common.test.Assert;

public class HolidayBehavior {

	@Test
	public void shouldReturnDateForTheYear() {
		Assert.equal(Holiday.Us.christmasDay.day.date(2018), LocalDate.of(2018, 12, 25));
		Assert.equal(Holiday.Us.veteransDay.observed.date(2018), LocalDate.of(2018, 11, 12));
		Assert.equal(Holiday.Us.veteransDay.observed.date(2017), LocalDate.of(2017, 11, 10));
		Assert.equal(Holiday.Us.veteransDay.observed.date(2016), LocalDate.of(2016, 11, 11));
		Assert.equal(Holiday.Us.independenceDay.day.date(2018), LocalDate.of(2018, 7, 4));
		exerciseEnum(Holiday.Us.class);
	}

	@Test
	public void shouldEncapsulateFixedDay() {
		Assert.equal(Holiday.of(Month.FEBRUARY, 28).date(2018), LocalDate.of(2018, 2, 28));
		Assert.thrown(() -> Holiday.of(Month.FEBRUARY, 29).date(2018));
	}

	@Test
	public void shouldEncapsulateNthDayInTheMonth() {
		Assert.equal(Holiday.nthDayInMonth(2, DayOfWeek.FRIDAY, Month.APRIL).date(2018),
			LocalDate.of(2018, 4, 13));
	}

	@Test
	public void shouldEncapsulateLastDayInTheMonth() {
		Assert.equal(Holiday.lastDayInMonth(DayOfWeek.FRIDAY, Month.APRIL).date(2018),
			LocalDate.of(2018, 4, 27));
	}
}
