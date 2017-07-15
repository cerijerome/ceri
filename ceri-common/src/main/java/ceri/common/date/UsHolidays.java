package ceri.common.date;

import static ceri.common.date.Holiday.lastDayInMonth;
import static ceri.common.date.Holiday.nthDayInMonth;
import static ceri.common.date.Holiday.observed;
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

public class UsHolidays {

	public static final Holiday MLK_JR_DAY = nthDayInMonth(3, MONDAY, JANUARY);
	public static final Holiday PRESIDENTS_DAY = nthDayInMonth(3, MONDAY, FEBRUARY);
	public static final Holiday MEMORIAL_DAY = lastDayInMonth(MONDAY, MAY);
	public static final Holiday INDEPENDENCE_DAY = year -> LocalDate.of(year, JULY, 4);
	public static final Holiday INDEPENDENCE_DAY_OBSERVED = observed(INDEPENDENCE_DAY);
	public static final Holiday LABOR_DAY = nthDayInMonth(1, MONDAY, SEPTEMBER);
	public static final Holiday COLUMBUS_DAY = nthDayInMonth(2, MONDAY, OCTOBER);
	public static final Holiday VETERANS_DAY = year -> LocalDate.of(year, NOVEMBER, 11);
	public static final Holiday VETERANS_DAY_OBSERVED = observed(VETERANS_DAY);
	public static final Holiday THANKSGIVING_DAY = nthDayInMonth(4, THURSDAY, NOVEMBER);
	public static final Holiday CHRISTMAS_DAY = year -> LocalDate.of(year, DECEMBER, 25);
	public static final Holiday CHRISTMAS_DAY_OBSERVED = observed(CHRISTMAS_DAY);

	public static void main(String[] args) {
		System.out.println(MLK_JR_DAY.date(2017));
		System.out.println(INDEPENDENCE_DAY.date(2017));
		System.out.println(LABOR_DAY.date(2017));
		System.out.println(THANKSGIVING_DAY.date(2017));
	}
	
}
