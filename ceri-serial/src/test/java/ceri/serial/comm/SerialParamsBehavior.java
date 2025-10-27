package ceri.serial.comm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class SerialParamsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = SerialParams.of(1200, DataBits._5, StopBits._1_5, Parity.even);
		var eq0 = SerialParams.of(1200, DataBits._5, StopBits._1_5, Parity.even);
		var eq1 = SerialParams.from("1200,5,1.5,e");
		var ne0 = SerialParams.from("2400,5,1.5,e");
		var ne1 = SerialParams.from("1200,6,1.5,e");
		var ne2 = SerialParams.from("1200,5,1,e");
		var ne3 = SerialParams.from("1200,5,1.5,n");
		TestUtil.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldFailToCreateFromBadString() {
		Assert.thrown(() -> SerialParams.from(""));
		Assert.thrown(() -> SerialParams.from("1200x,7,1,n"));
		Assert.thrown(() -> SerialParams.from("1200,4,1,n"));
		Assert.thrown(() -> SerialParams.from("1200,9,1,n"));
		Assert.thrown(() -> SerialParams.from("1200,7,0,n"));
		Assert.thrown(() -> SerialParams.from("1200,7,1,x"));
	}

	@Test
	public void shouldProvideTimingData() {
		var p = SerialParams.from("250000,7,2,n");
		Assert.equal(p.bitsPerFrame(), 10);
		Assert.equal(p.microsPerBit(), 4.0);
		Assert.equal(p.microsPerFrame(), 40.0);
		Assert.equal(p.sendTimeMicros(10), 400.0);
	}
}
