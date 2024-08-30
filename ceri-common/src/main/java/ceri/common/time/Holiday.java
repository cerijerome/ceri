package ceri.common.time;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

/**
 * Represents a holiday that resolves to a date for a given year.
 */
public interface Holiday {

	LocalDate date(int year);

	static Holiday nthDayInMonth(int n, DayOfWeek day, Month month) {
		return year -> LocalDate.of(year, month, 1)
			.with(TemporalAdjusters.dayOfWeekInMonth(n, day));
	}

	static Holiday lastDayInMonth(DayOfWeek day, Month month) {
		return year -> LocalDate.of(year, month, 1).with(TemporalAdjusters.lastInMonth(day));
	}

	static Holiday of(Month month, int day) {
		return year -> LocalDate.of(year, month, day);
	}

	/**
	 * An observed holiday for Saturday occurs on Friday, and for Sunday occurs on Monday.
	 */
	static Holiday observed(Holiday holiday) {
		return year -> {
			LocalDate date = holiday.date(year);
			DayOfWeek day = date.getDayOfWeek();
			if (day == SATURDAY) return date.minusDays(1);
			if (day == SUNDAY) return date.plusDays(1);
			return date;
		};
	}

	/**
	 * Common US holidays.
	 */
	public enum Us {
		mlkJrDay(nthDayInMonth(3, MONDAY, JANUARY), false),
		presidentsDay(nthDayInMonth(3, MONDAY, FEBRUARY), false),
		memorialDay(lastDayInMonth(MONDAY, MAY), false),
		independenceDay(year -> LocalDate.of(year, JULY, 4), true),
		laborDay(nthDayInMonth(1, MONDAY, SEPTEMBER), false),
		columbusDay(nthDayInMonth(2, MONDAY, OCTOBER), false),
		veteransDay(year -> LocalDate.of(year, NOVEMBER, 11), true),
		thanksgivingDay(nthDayInMonth(4, THURSDAY, NOVEMBER), false),
		christmasDay(year -> LocalDate.of(year, DECEMBER, 25), true);

		public final Holiday day;
		public final Holiday observed;

		private Us(Holiday day, boolean observed) {
			this.day = day;
			this.observed = observed ? Holiday.observed(day) : day;
		}
	}
}
