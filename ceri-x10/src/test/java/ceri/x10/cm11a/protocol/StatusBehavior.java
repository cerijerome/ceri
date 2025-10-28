package ceri.x10.cm11a.protocol;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.time.Dates;
import ceri.x10.command.House;

public class StatusBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Status.builder().date(Dates.UTC_EPOCH).build();
		var eq0 = Status.builder().date(Dates.UTC_EPOCH).build();
		var ne0 = Status.builder().build();
		var ne1 = Status.builder().date(Dates.UTC_EPOCH).batteryTimerReset().build();
		var ne2 = Status.builder().date(Dates.UTC_EPOCH).batteryTimer(1).build();
		var ne3 = Status.builder().date(Dates.UTC_EPOCH).house(House.D).build();
		var ne4 = Status.builder().date(Dates.UTC_EPOCH).firmware(2).build();
		var ne5 = Status.builder().date(Dates.UTC_EPOCH).addressed(0xff).build();
		var ne6 = Status.builder().date(Dates.UTC_EPOCH).onOff(0x7f).build();
		var ne7 = Status.builder().date(Dates.UTC_EPOCH).dim(0x3f).build();
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldEncode() {
		var status = Status.builder().date(Dates.UTC_EPOCH).build();
		Assert.array(status.encode(), 0, 0, 0, 0, 0, 1, 4, 1, 0, 0, 0, 0, 0, 0);
	}
}
