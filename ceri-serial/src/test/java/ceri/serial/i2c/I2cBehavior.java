package ceri.serial.i2c;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertFind;
import static ceri.common.test.Assert.assertUnordered;
import java.io.IOException;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.serial.i2c.jna.I2cDev.i2c_func;

public class I2cBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		I2c.NULL.reset();
		I2c.NULL.retries(3);
		I2c.NULL.timeout(1000);
		I2c.NULL.smBusPec(true);
		I2c.NULL.smBus(I2cAddress.of(0x28));
		assertUnordered(I2c.NULL.functions(), i2c_func.xcoder.all());
		I2c.NULL.writeData(I2cAddress.of(0x28), 0xab, 0xcd);
		assertArray(I2c.NULL.readData(I2cAddress.of(0x28), ArrayUtil.bytes.of(1, 2, 3), 3), 0, 0,
			0);
		assertFind(I2c.NULL, ".*NULL$");
	}

}
