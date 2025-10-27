package ceri.serial.i2c.util;

import static ceri.serial.i2c.util.I2cUtil.address;
import org.junit.Test;
import ceri.common.test.Assert;

public class I2cUtilTest {

	@Test
	public void testMicros() {
		Assert.equal(I2cUtil.micros(0, address(0x33, true), 5), 0L);
		Assert.equal(I2cUtil.micros(800000, address(0x33, true), 5), 79L);
		Assert.equal(I2cUtil.micros(800000, address(0x33, false), 5), 68L);
	}

}
