package ceri.x10.cm11a.protocol;

import java.time.Month;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.time.Dates;
import ceri.x10.command.House;

public class ClockBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Clock.builder().date(Dates.UTC_EPOCH).build();
		var eq0 = Clock.builder().date(Dates.UTC_EPOCH).build();
		var ne0 = Clock.of();
		var ne1 = Clock.of(House.B);
		var ne2 = Clock.builder().date(Dates.UTC_EPOCH).house(House.C).build();
		var ne3 = Clock.builder().date(Dates.UTC_EPOCH).clearBatteryTimer(true).build();
		var ne4 = Clock.builder().date(Dates.UTC_EPOCH).clearMonitoredStatus(true).build();
		var ne5 = Clock.builder().date(Dates.UTC_EPOCH).purgeTimer(true).build();
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldDecode() {
		Clock clock = Clock.decode(Testing.reader(0x9b, 0, 0, 0, 1, 0x04, 0x57));
		Assert.equal(clock.house, House.G);
		Assert.equal(clock.date.getMonth(), Month.JANUARY);
		Assert.equal(clock.date.getDayOfMonth(), 1);
		Assert.equal(clock.date.getHour(), 0);
		Assert.equal(clock.date.getMinute(), 0);
		Assert.equal(clock.date.getSecond(), 0);
		Assert.yes(clock.clearBatteryTimer);
		Assert.yes(clock.clearMonitoredStatus);
		Assert.yes(clock.purgeTimer);
	}

	@Test
	public void shouldEncode() {
		Clock clock = Clock.builder().date(Dates.UTC_EPOCH).house(House.E)
			.clearBatteryTimer(true).clearMonitoredStatus(true).purgeTimer(true).build();
		Assert.array(clock.encode(), 0x9b, 0, 0, 0, 1, 0x4, 0x17);
	}
}
