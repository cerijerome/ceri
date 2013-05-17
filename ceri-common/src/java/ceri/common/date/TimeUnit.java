package ceri.common.date;

import java.util.Map;
import java.util.TreeMap;

public enum TimeUnit {
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

	public static Map<TimeUnit, Integer> fromMillisec(long ms) {
		Map<TimeUnit, Integer> map = new TreeMap<>();
		TimeUnit[] values = TimeUnit.values();
		for (int i = values.length - 1; i >= 0; i--) {
			TimeUnit unit = values[i];
			int count = (int) (ms / unit.ms);
			if (count > 0) map.put(unit, count);
			ms = ms % unit.ms;
		}
		return map;
	}

	public static long toMillisec(Map<TimeUnit, Integer> units) {
		long ms = 0;
		for (Map.Entry<TimeUnit, Integer> entry : units.entrySet()) {
			ms += (entry.getKey().ms * entry.getValue());
		}
		return ms;
	}

	public static void main(String[] args) {
		System.out.println(TimeUnit.fromMillisec(7 * 24 * 60 * 60 * 1000));
		Map<TimeUnit, Integer> units = fromMillisec(9999999999L);
		System.out.println(units);
		System.out.println(toMillisec(units));
	}

}
