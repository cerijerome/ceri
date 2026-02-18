package ceri.serial.i2c;

import java.io.IOException;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.test.Assert;
import ceri.serial.i2c.jna.I2cDev.i2c_func;

public class I2cBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		I2c.NULL.reset();
		I2c.NULL.retries(3);
		I2c.NULL.timeout(1000);
		I2c.NULL.smBusPec(true);
		I2c.NULL.smBus(I2cAddress.of(0x28));
		Assert.unordered(I2c.NULL.functions(), i2c_func.xcoder.all());
		I2c.NULL.writeData(I2cAddress.of(0x28), 0xab, 0xcd);
		Assert.array(I2c.NULL.readData(I2cAddress.of(0x28), Array.BYTE.of(1, 2, 3), 3), 0, 0,
			0);
		Assert.find(I2c.NULL, ".*NULL$");
	}
}
