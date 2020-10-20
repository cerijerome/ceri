package ceri.common.time;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEnum;
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
		assertEquals(cal.get(Calendar.HOUR), 1);
	}

}
