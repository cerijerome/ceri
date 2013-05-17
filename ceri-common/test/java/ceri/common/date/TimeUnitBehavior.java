package ceri.common.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class TimeUnitBehavior {

	@Test
	public void shouldConvertMsToCorrectUnits() {
		Map<TimeUnit, Integer> map = new HashMap<>();
		assertThat(TimeUnit.fromMillisec(0), is(map));
		
		map.put(TimeUnit.millisec, 999);
		map.put(TimeUnit.second, 9);
		assertThat(TimeUnit.fromMillisec(9999), is(map));
		
		map.clear();
		map.put(TimeUnit.millisec, 999);
		map.put(TimeUnit.second, 59);
		map.put(TimeUnit.minute, 59);
		map.put(TimeUnit.hour, 23);
		map.put(TimeUnit.day, 1);
		assertThat(TimeUnit.fromMillisec(TimeUnit.day.ms * 2 - 1), is(map));
	}

	@Test
	public void shouldConvertUnitsToMsCorrectly() {
		Map<TimeUnit, Integer> map = new HashMap<>();
		assertThat(TimeUnit.toMillisec(map), is(0L));
		
		map.put(TimeUnit.hour, 2);
		map.put(TimeUnit.second, 22);
		long t = (2L * 60 * 60 * 1000) + (22L * 1000);
		assertThat(TimeUnit.toMillisec(map), is(t));

		map.clear();
		map.put(TimeUnit.millisec, 999);
		map.put(TimeUnit.second, 59);
		map.put(TimeUnit.minute, 59);
		map.put(TimeUnit.hour, 23);
		map.put(TimeUnit.day, 999);
		t = (24L * 60 * 60 * 1000) * 1000 - 1;
		assertThat(TimeUnit.toMillisec(map), is(t));
	}
	
}
