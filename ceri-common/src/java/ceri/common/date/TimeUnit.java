package ceri.common.date;

import ceri.common.unit.NormalizedValue;
import ceri.common.unit.Unit;

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

	public static NormalizedValue<TimeUnit> normalize(long value) {
		return NormalizedValue.create(value, TimeUnit.class);
	}

}
