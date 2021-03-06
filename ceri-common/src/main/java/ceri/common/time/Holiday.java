package ceri.common.time;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

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

	static Holiday observed(Holiday holiday) {
		return year -> {
			LocalDate date = holiday.date(year);
			DayOfWeek day = date.getDayOfWeek();
			if (day == SATURDAY) return date.minusDays(1);
			if (day == SUNDAY) return date.plusDays(1);
			return date;
		};
	}

}
