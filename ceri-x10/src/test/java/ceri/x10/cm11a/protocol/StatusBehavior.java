package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.time.DateUtil.UTC_EPOCH;
import org.junit.Test;
import ceri.x10.command.House;

public class StatusBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Status t = Status.builder().date(UTC_EPOCH).build();
		Status eq0 = Status.builder().date(UTC_EPOCH).build();
		Status ne0 = Status.builder().build();
		Status ne1 = Status.builder().date(UTC_EPOCH).batteryTimerReset().build();
		Status ne2 = Status.builder().date(UTC_EPOCH).batteryTimer(1).build();
		Status ne3 = Status.builder().date(UTC_EPOCH).house(House.D).build();
		Status ne4 = Status.builder().date(UTC_EPOCH).firmware(2).build();
		Status ne5 = Status.builder().date(UTC_EPOCH).addressed(0xff).build();
		Status ne6 = Status.builder().date(UTC_EPOCH).onOff(0x7f).build();
		Status ne7 = Status.builder().date(UTC_EPOCH).dim(0x3f).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldEncode() {
		Status status = Status.builder().date(UTC_EPOCH).build();
		assertArray(status.encode(), 0, 0, 0, 0, 0, 1, 4, 1, 0, 0, 0, 0, 0, 0);
	}

}
