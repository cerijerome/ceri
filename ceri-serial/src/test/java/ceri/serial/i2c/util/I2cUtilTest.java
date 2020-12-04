package ceri.serial.i2c.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.serial.i2c.util.I2cUtil.address;
import org.junit.Test;

public class I2cUtilTest {

	@Test
	public void testMicros() {
		assertEquals(I2cUtil.micros(800000, address(0x33, true), 5), 79L);
		assertEquals(I2cUtil.micros(800000, address(0x33, false), 5), 68L);
	}

}
