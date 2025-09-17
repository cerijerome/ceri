package ceri.common.time;

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
			if (day == DayOfWeek.SATURDAY) return date.minusDays(1);
			if (day == DayOfWeek.SUNDAY) return date.plusDays(1);
			return date;
		};
	}

	/**
	 * Common US holidays.
	 */
	public enum Us {
		mlkJrDay(nthDayInMonth(3, DayOfWeek.MONDAY, Month.JANUARY), false),
		presidentsDay(nthDayInMonth(3, DayOfWeek.MONDAY, Month.FEBRUARY), false),
		memorialDay(lastDayInMonth(DayOfWeek.MONDAY, Month.MAY), false),
		independenceDay(year -> LocalDate.of(year, Month.JULY, 4), true),
		laborDay(nthDayInMonth(1, DayOfWeek.MONDAY, Month.SEPTEMBER), false),
		columbusDay(nthDayInMonth(2, DayOfWeek.MONDAY, Month.OCTOBER), false),
		veteransDay(year -> LocalDate.of(year, Month.NOVEMBER, 11), true),
		thanksgivingDay(nthDayInMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER), false),
		christmasDay(year -> LocalDate.of(year, Month.DECEMBER, 25), true);

		public final Holiday day;
		public final Holiday observed;

		private Us(Holiday day, boolean observed) {
			this.day = day;
			this.observed = observed ? Holiday.observed(day) : day;
		}
	}
}
