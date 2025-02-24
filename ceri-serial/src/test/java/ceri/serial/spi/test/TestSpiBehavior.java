package ceri.serial.spi.test;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.io.Direction;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.test.TestSpi.Request;

public class TestSpiBehavior {

	@Test
	public void shouldConfigureSpi() throws IOException {
		var spi = TestSpi.of();
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
	public void shouldCaptureOutput() throws IOException {
		var spi = TestSpi.of();
		var xfer = spi.transfer(Direction.out, 10);
		xfer.write(bytes(1, 2, 3, 4, 5));
		xfer.execute();
		spi.xfer.assertAuto(Request.out(1, 2, 3, 4, 5));
	}

	@Test
	public void shouldProvideInput() throws IOException {
		var spi = TestSpi.of();
		var xfer = spi.transfer(Direction.in, 10);
		spi.xfer.autoResponses(ByteProvider.of(1, 2, 3, 4, 5));
		xfer.execute();
		assertArray(xfer.read(), 1, 2, 3, 4, 5, 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldHandleDuplexIo() throws IOException {
		var spi = TestSpi.of();
		var xfer = spi.transfer(Direction.duplex, 10);
		spi.xfer.autoResponses(ByteProvider.of(1, 2, 3, 4, 5));
		xfer.write(bytes(6, 7, 8, 9));
		xfer.execute();
		assertArray(xfer.read(), 1, 2, 3, 4); // only 4 bytes
		spi.xfer.assertAuto(Request.duplex(6, 7, 8, 9));
	}
}
