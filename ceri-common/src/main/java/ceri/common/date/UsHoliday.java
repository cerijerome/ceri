package ceri.common.date;

import static ceri.common.date.Holiday.lastDayInMonth;
import static ceri.common.date.Holiday.nthDayInMonth;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import java.time.LocalDate;

public enum UsHoliday {
	mlkJrDay(nthDayInMonth(3, MONDAY, JANUARY)),
	presidentsDay(nthDayInMonth(3, MONDAY, FEBRUARY)),
	memorialDay(lastDayInMonth(MONDAY, MAY)),
	independenceDay(year -> LocalDate.of(year, JULY, 4), true),
	laborDay(nthDayInMonth(1, MONDAY, SEPTEMBER)),
	columbusDay(nthDayInMonth(2, MONDAY, OCTOBER)),
	veteransDay(year -> LocalDate.of(year, NOVEMBER, 11), true),
	thanksgivingDay(nthDayInMonth(4, THURSDAY, NOVEMBER)),
	christmasDay(year -> LocalDate.of(year, DECEMBER, 25), true);

	public final Holiday day;
	public final Holiday observed;

	UsHoliday(Holiday day) {
		this(day, false);
	}
	
	UsHoliday(Holiday day, boolean observed) {
		this.day = day;
		this.observed = observed ? Holiday.observed(day) : day;
	}

}
