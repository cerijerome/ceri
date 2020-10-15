package ceri.common.time;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import java.util.Calendar;
import org.junit.Test;

public class CalendarFieldBehavior {

	@Test
	public void testCoverage() {
		exerciseEnum(CalendarField.class);
	}

	@Test
	public void shouldSetCalendarField() {
		Calendar cal = Calendar.getInstance();
		CalendarField.hour.set(cal, 1);
		assertThat(cal.get(Calendar.HOUR), is(1));
	}

}
