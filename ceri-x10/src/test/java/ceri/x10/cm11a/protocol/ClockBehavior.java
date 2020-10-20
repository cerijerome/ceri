package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.test.TestUtil.reader;
import static ceri.common.time.DateUtil.UTC_EPOCH;
import java.time.Month;
import org.junit.Test;
import ceri.x10.command.House;

public class ClockBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Clock t = Clock.builder().date(UTC_EPOCH).build();
		Clock eq0 = Clock.builder().date(UTC_EPOCH).build();
		Clock ne0 = Clock.of();
		Clock ne1 = Clock.of(House.B);
		Clock ne2 = Clock.builder().date(UTC_EPOCH).house(House.C).build();
		Clock ne3 = Clock.builder().date(UTC_EPOCH).clearBatteryTimer(true).build();
		Clock ne4 = Clock.builder().date(UTC_EPOCH).clearMonitoredStatus(true).build();
		Clock ne5 = Clock.builder().date(UTC_EPOCH).purgeTimer(true).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldDecode() {
		Clock clock = Clock.decode(reader(0x9b, 0, 0, 0, 1, 0x04, 0x57));
		assertEquals(clock.house, House.G);
		assertEquals(clock.date.getMonth(), Month.JANUARY);
		assertEquals(clock.date.getDayOfMonth(), 1);
		assertEquals(clock.date.getHour(), 0);
		assertEquals(clock.date.getMinute(), 0);
		assertEquals(clock.date.getSecond(), 0);
		assertTrue(clock.clearBatteryTimer);
		assertTrue(clock.clearMonitoredStatus);
		assertTrue(clock.purgeTimer);
	}

	@Test
	public void shouldEncode() {
		Clock clock = Clock.builder().date(UTC_EPOCH).house(House.E).clearBatteryTimer(true)
			.clearMonitoredStatus(true).purgeTimer(true).build();
		assertArray(clock.encode(), 0x9b, 0, 0, 0, 1, 0x4, 0x17);
	}

}
