package ceri.serial.comm;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class SerialParamsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		SerialParams t = SerialParams.of(1200, DataBits._5, StopBits._1_5, Parity.even);
		SerialParams eq0 = SerialParams.of(1200, DataBits._5, StopBits._1_5, Parity.even);
		SerialParams eq1 = SerialParams.from("1200,5,1.5,e");
		SerialParams ne0 = SerialParams.from("2400,5,1.5,e");
		SerialParams ne1 = SerialParams.from("1200,6,1.5,e");
		SerialParams ne2 = SerialParams.from("1200,5,1,e");
		SerialParams ne3 = SerialParams.from("1200,5,1.5,n");
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldFailToCreateFromBadString() {
		assertThrown(() -> SerialParams.from(""));
		assertThrown(() -> SerialParams.from("1200x,7,1,n"));
		assertThrown(() -> SerialParams.from("1200,4,1,n"));
		assertThrown(() -> SerialParams.from("1200,9,1,n"));
		assertThrown(() -> SerialParams.from("1200,7,0,n"));
		assertThrown(() -> SerialParams.from("1200,7,1,x"));
	}

	@Test
	public void shouldProvideTimingData() {
		SerialParams p = SerialParams.from("250000,7,2,n");
		assertEquals(p.bitsPerFrame(), 10);
		assertEquals(p.microsPerBit(), 4.0);
		assertEquals(p.microsPerFrame(), 40.0);
		assertEquals(p.sendTimeMicros(10), 400.0);
	}

}
