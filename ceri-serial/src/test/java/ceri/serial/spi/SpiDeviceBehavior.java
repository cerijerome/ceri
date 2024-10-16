package ceri.serial.spi;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.ImmutableUtil.asList;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.spi.SpiMode.MODE_2;
import static ceri.serial.spi.SpiMode.MODE_3;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_MESSAGE;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_BITS_PER_WORD;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_LSB_FIRST;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_MAX_SPEED_HZ;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_MODE32;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_BITS_PER_WORD;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_LSB_FIRST;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_MAX_SPEED_HZ;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_MODE;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_MODE32;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import ceri.common.io.Direction;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.serial.spi.jna.TestSpiCLibNative;

public class SpiDeviceBehavior {
	private TestSpiCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private FileDescriptor fd;
	private SpiDevice spi;

	@After
	public void after() {
		CloseableUtil.close(fd, enc);
		fd = null;
		enc = null;
		spi = null;
		lib = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = SpiDevice.Config.of(1, 1);
		var eq0 = SpiDevice.Config.of(1, 1);
		var eq1 = SpiDevice.Config.builder().bus(1).chip(1).build();
		var ne0 = SpiDevice.Config.of(0, 1);
		var ne1 = SpiDevice.Config.of(1, 0);
		var ne2 = SpiDevice.Config.of(1, 1, Direction.in);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldOpenDevice() throws IOException {
		lib = TestSpiCLibNative.of();
		enc = TestCLibNative.register(lib);
		assertThrown(() -> SpiDevice.Config.of(0, 0, null));
		try (var fd = SpiDevice.Config.of(0, 1, Direction.in).open()) {}
		try (var fd = SpiDevice.Config.of(1, 1, Direction.out).open()) {}
		try (var fd = SpiDevice.Config.of(1, 0).open()) {}
		lib.open.assertValues( //
			new OpenArgs("/dev/spidev0.1", 0, 0), //
			new OpenArgs("/dev/spidev1.1", 1, 0), //
			new OpenArgs("/dev/spidev1.0", 2, 0));
	}

	@Test
	public void shouldOverrideFdCreation() throws IOException {
		var config = SpiDevice.Config.builder().openFn(c -> FileDescriptor.NULL).build();
		try (var fd = config.open()) {}
	}

	@Test
	public void shouldSetParameters() throws IOException {
		init();
		spi.mode(MODE_2);
		spi.mode(new SpiMode(0x123)); // 32-bit
		spi.lsbFirst(true);
		spi.lsbFirst(false);
		spi.bitsPerWord(3);
		spi.maxSpeedHz(200);
		lib.ioctlSpiInt.assertValues( //
			List.of(SPI_IOC_WR_MODE, MODE_2.value()), //
			List.of(SPI_IOC_WR_MODE32, 0x123), //
			List.of(SPI_IOC_WR_LSB_FIRST, 1), //
			List.of(SPI_IOC_WR_LSB_FIRST, 0), //
			List.of(SPI_IOC_WR_BITS_PER_WORD, 3), //
			List.of(SPI_IOC_WR_MAX_SPEED_HZ, 200));
	}

	@Test
	public void shouldGetParameters() throws IOException {
		init();
		lib.ioctlSpiInt.autoResponses(MODE_3.value(), 1, 0, 3, 200);
		assertEquals(spi.mode(), MODE_3);
		assertEquals(spi.lsbFirst(), true);
		assertEquals(spi.lsbFirst(), false);
		assertEquals(spi.bitsPerWord(), 3);
		assertEquals(spi.maxSpeedHz(), 200);
		lib.ioctlSpiInt.assertValues( //
			List.of(SPI_IOC_RD_MODE32), //
			List.of(SPI_IOC_RD_LSB_FIRST), //
			List.of(SPI_IOC_RD_LSB_FIRST), //
			List.of(SPI_IOC_RD_BITS_PER_WORD), //
			List.of(SPI_IOC_RD_MAX_SPEED_HZ));
	}

	@Test
	public void shouldTransferOut() throws IOException {
		init();
		var xfer = spi.transfer(Direction.out, 5);
		xfer.write(bytes(1, 2, 3, 4, 5));
		xfer.execute();
		assertArray(xfer.read());
		lib.ioctlSpiMsg.assertValues(
			List.of(SPI_IOC_MESSAGE(1), provider(1, 2, 3, 4, 5), 5, 0, 0, 0, 0, 0, 0));
	}

	@Test
	public void shouldTransferIn() throws IOException {
		init();
		lib.ioctlSpiMsg.autoResponses(provider(5, 4, 3, 2, 1));
		var xfer = spi.transfer(Direction.in, 5);
		xfer.write(bytes(1, 2, 3)); // ignored
		xfer.execute();
		assertArray(xfer.read(), 5, 4, 3, 2, 1);
		lib.ioctlSpiMsg.assertValues(asList(SPI_IOC_MESSAGE(1), null, 5, 0, 0, 0, 0, 0, 0));
	}

	@Test
	public void shouldTransferDuplex() throws IOException {
		init();
		lib.ioctlSpiMsg.autoResponses(provider(5, 4, 3, 2, 1));
		var xfer = spi.transfer(Direction.duplex, 5);
		xfer.write(bytes(5, 6, 7, 8, 9));
		xfer.execute();
		assertArray(xfer.read(), 5, 4, 3, 2, 1);
		lib.ioctlSpiMsg.assertValues(
			List.of(SPI_IOC_MESSAGE(1), provider(5, 6, 7, 8, 9), 5, 0, 0, 0, 0, 0, 0));
	}

	@Test
	public void shouldConfigureTransfer() throws IOException {
		init();
		var xfer = spi.transfer(Direction.duplex, 5).limit(4).speedHz(200).delayMicros(10)
			.bitsPerWord(3).csChange(true).txNbits(1).rxNbits(2);
		assertEquals(xfer.size(), 4);
		assertEquals(xfer.sizeMax(), 5);
		assertEquals(xfer.direction(), Direction.duplex);
		assertEquals(xfer.speedHz(), 200);
		assertEquals(xfer.delayMicros(), 10);
		assertEquals(xfer.bitsPerWord(), 3);
		assertEquals(xfer.csChange(), true);
		assertEquals(xfer.txNbits(), 1);
		assertEquals(xfer.rxNbits(), 2);
		xfer.csChange(false);
		assertEquals(xfer.csChange(), false);
	}

	@Test
	public void shouldDetermineSpeed() throws IOException {
		init();
		lib.ioctlSpiInt.autoResponses(0, 200);
		var xfer = spi.transfer(Direction.out, 3);
		assertEquals(spi.speedHz(xfer), 0); // 0 from spi, 0 from xfer
		assertEquals(spi.speedHz(xfer), 200); // 200 from spi, 0 from xfer
		assertEquals(spi.speedHz(xfer.speedHz(100)), 100); // 200 from spi, 100 from xfer

	}

	@Test
	public void shouldDetermineBitsPerWord() throws IOException {
		init();
		lib.ioctlSpiInt.autoResponses(0, 7);
		var xfer = spi.transfer(Direction.out, 3);
		assertEquals(spi.bitsPerWord(xfer), 8); // 0 from spi, 0 from xfer
		assertEquals(spi.bitsPerWord(xfer), 7); // 7 from spi, 0 from xfer
		assertEquals(spi.bitsPerWord(xfer.bitsPerWord(6)), 6); // 7 from spi, 6 from xfer

	}

	private void init() throws IOException {
		lib = TestSpiCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = SpiDevice.Config.of(0, 0).open();
		spi = SpiDevice.of(fd);
	}
}
