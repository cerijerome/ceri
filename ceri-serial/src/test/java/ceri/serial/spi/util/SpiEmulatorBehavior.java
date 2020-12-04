package ceri.serial.spi.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.SpiMode;

public class SpiEmulatorBehavior {

	@Test
	public void shouldConfigureSpi() {
		var spi = SpiEmulator.echo();
		spi.mode(SpiMode.MODE_1);
		spi.lsbFirst(true);
		spi.bitsPerWord(9);
		spi.maxSpeedHz(10000);
		assertEquals(spi.mode(), SpiMode.MODE_1);
		assertEquals(spi.lsbFirst(), true);
		assertEquals(spi.bitsPerWord(), 9);
		assertEquals(spi.maxSpeedHz(), 10000);
	}

	@Test
	public void shouldIgnoreOutput() throws IOException {
		var spi = SpiEmulator.echo();
		var xfer = spi.transfer(Direction.out, 5);
		xfer.write(ArrayUtil.bytes(1, 2, 3, 4, 5));
		xfer.execute();
		assertArray(xfer.read());
	}

	@Test
	public void shouldProvideBlankInput() throws IOException {
		var spi = SpiEmulator.echo();
		var xfer = spi.transfer(Direction.in, 5);
		xfer.write(ArrayUtil.bytes(1, 2, 3, 4, 5)); // ignored
		xfer.execute();
		assertArray(xfer.read(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEcho() throws IOException {
		var spi = SpiEmulator.echo();
		var xfer = spi.transfer(Direction.duplex, 5);
		xfer.write(ArrayUtil.bytes(1, 2, 3, 4, 5));
		xfer.execute();
		assertArray(xfer.read(), 1, 2, 3, 4, 5);
	}

}
