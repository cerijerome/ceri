package ceri.common.date;

import java.util.Calendar;

/**
 * Enumeration of Calendar field types.
 */
public enum CalendarField {
	millisec(Calendar.MILLISECOND, 0),
	second(Calendar.SECOND, 0),
	minute(Calendar.MINUTE, 0),
	hour(Calendar.HOUR_OF_DAY, 0),
	day(Calendar.DAY_OF_MONTH, 1),
	month(Calendar.MONTH, 0),
	year(Calendar.YEAR, 0);

	public final int calendarField;
	public final int firstValue;

	CalendarField(int calendarField, int firstValue) {
		this.calendarField = calendarField;
		this.firstValue = firstValue;
	}

	/**
	 * Sets given Calendar field with zero-based value. Days start at 0 for 1st of the month.
	 */
	public void set(Calendar cal, int value) {
		cal.set(calendarField, value + firstValue);
	}

}
