package ceri.common.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.unit.NormalizedValue;

public class TimeUnitBehavior {

	@Test
	public void shouldConvertMsToCorrectUnits() {
		NormalizedValue<TimeUnit> n = NormalizedValue.create(9999L, TimeUnit.class);
		assertThat(n.value(TimeUnit.millisec), is(999L));
		assertThat(n.value(TimeUnit.second), is(9L));
		assertThat(n.value(TimeUnit.minute), is(0L));

		n = NormalizedValue.create(TimeUnit.day.ms * 2 - 1, TimeUnit.class);
		assertThat(n.value(TimeUnit.millisec), is(999L));
		assertThat(n.value(TimeUnit.second), is(59L));
		assertThat(n.value(TimeUnit.minute), is(59L));
		assertThat(n.value(TimeUnit.hour), is(23L));
		assertThat(n.value(TimeUnit.day), is(1L));
	}

	@Test
	public void shouldConvertUnitsToMsCorrectly() {
		NormalizedValue<TimeUnit> n = NormalizedValue.builder(TimeUnit.class)
			.value(999, TimeUnit.millisec)
			.value(9, TimeUnit.second)
			.build();
		assertThat(n.value, is(9999L));

		n = NormalizedValue.builder(TimeUnit.class).value(999, TimeUnit.millisec)
			.value(59, TimeUnit.second)
			.value(59, TimeUnit.minute)
			.value(23, TimeUnit.hour)
			.value(1, TimeUnit.day)
			.build();
		assertThat(n.value, is(TimeUnit.day.ms * 2 - 1));
	}
	
}
