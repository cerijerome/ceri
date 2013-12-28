package ceri.common.date;

import java.util.Calendar;
import java.util.Map;
import ceri.common.unit.NormalizedValue;
import ceri.common.unit.Unit;

/**
 * Time units to be used with NormalizedValue and Calendar.
 * For other use cases java.util.concurrent.TimeUnit is preferred.
 */
public enum TimeUnit implements Unit {
	millisec("ms", CalendarField.millisec, 1),
	second("s", CalendarField.second, 1000),
	minute("m", CalendarField.minute, 60 * second.ms),
	hour("h", CalendarField.hour, 60 * minute.ms),
	day("d", CalendarField.day, 24 * hour.ms);

	public final String shortName;
	public final long ms;
	public final CalendarField field;

	private TimeUnit(String shortName, CalendarField field, long ms) {
		this.shortName = shortName;
		this.field = field;
		this.ms = ms;
	}

	@Override
	public long units() {
		return ms;
	}

	/**
	 * Normalizes milliseconds into days, hours, minutes, seconds, and milliseconds.
	 */
	public static NormalizedValue<TimeUnit> normalize(long ms) {
		return NormalizedValue.create(ms, TimeUnit.class);
	}

	/**
	 * Set given calendar with normalized unit values.
	 */
	public static void set(Calendar cal, NormalizedValue<TimeUnit> value) {
		for (Map.Entry<TimeUnit, Long> entry : value.values.entrySet())
			entry.getKey().field.set(cal, entry.getValue().intValue());
	}
	
}
