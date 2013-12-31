package ceri.common.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import ceri.common.unit.NormalizedValue;

public class TimeUnitBehavior {

	@Test
	public void testSetCalendar() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(0);
		NormalizedValue<TimeUnit> n = NormalizedValue.create(1000000000, TimeUnit.class);
		TimeUnit.set(cal, n);
		assertThat(cal.get(Calendar.YEAR), is(1970));
		assertThat(cal.get(Calendar.MONTH), is(0));
		assertThat(cal.get(Calendar.DATE), is(12));
		assertThat(cal.get(Calendar.HOUR_OF_DAY), is(13));
		assertThat(cal.get(Calendar.MINUTE), is(46));
		assertThat(cal.get(Calendar.SECOND), is(40));
		assertThat(cal.get(Calendar.MILLISECOND), is(0));
	}
	
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
