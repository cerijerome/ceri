package ceri.serial.spi;

import java.io.IOException;
import org.junit.Test;
import ceri.common.io.Direction;
import ceri.common.test.Assert;

public class SpiBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Spi.NULL.mode(SpiMode.MODE_3);
		Spi.NULL.lsbFirst(true);
		Spi.NULL.bitsPerWord(8);
		Spi.NULL.maxSpeedHz(555);
		Assert.equal(Spi.NULL.mode(), SpiMode.MODE_0);
		Assert.equal(Spi.NULL.lsbFirst(), false);
		Assert.equal(Spi.NULL.bitsPerWord(), 0);
		Assert.equal(Spi.NULL.maxSpeedHz(), 0);
		Assert.find(Spi.NULL, ".*NULL$");
	}

	@Test
	public void shouldProvideNoOpTransfer() throws IOException {
		Spi.NULL.transfer(Direction.out, 3).execute();
	}
}
