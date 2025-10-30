package ceri.serial.spi.util;

import java.io.IOException;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.io.Direction;
import ceri.common.io.StringPrintStream;
import ceri.common.test.Assert;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.pulse.PulseBuffer;
import ceri.serial.spi.pulse.SpiPulseConfig;

public class SpiEmulatorBehavior {

	@Test
	public void shouldConfigureSpi() throws IOException {
		var spi = SpiEmulator.echo();
		spi.mode(SpiMode.MODE_1);
		spi.lsbFirst(true);
		spi.bitsPerWord(9);
		spi.maxSpeedHz(10000);
		Assert.equal(spi.mode(), SpiMode.MODE_1);
		Assert.equal(spi.lsbFirst(), true);
		Assert.equal(spi.bitsPerWord(), 9);
		Assert.equal(spi.maxSpeedHz(), 10000);
	}

	@Test
	public void shouldIgnoreOutput() throws IOException {
		var spi = SpiEmulator.echo().delay(false);
		var xfer = spi.transfer(Direction.out, 5);
		xfer.write(Array.bytes.of(1, 2, 3, 4, 5));
		xfer.execute();
		Assert.array(xfer.read());
	}

	@Test
	public void shouldProvideBlankInput() throws IOException {
		var spi = SpiEmulator.echo().delay(false);
		var xfer = spi.transfer(Direction.in, 5);
		xfer.write(Array.bytes.of(1, 2, 3, 4, 5)); // ignored
		xfer.execute();
		Assert.array(xfer.read(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEcho() throws IOException {
		var spi = SpiEmulator.echo().delay(false);
		var xfer = spi.transfer(Direction.duplex, 5);
		xfer.write(Array.bytes.of(1, 2, 3, 4, 5));
		xfer.execute();
		Assert.array(xfer.read(), 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldDelayForTransferTime() throws IOException {
		var spi = SpiEmulator.echo();
		var xfer = spi.transfer(Direction.duplex, 3).speedHz(25000000);
		xfer.write(Array.bytes.of(1, 2, 3));
		xfer.execute(); // < 1us
		Assert.array(xfer.read(), 1, 2, 3);
	}

	@Test
	public void shouldPrintPulsesWithSpacing() throws IOException {
		try (var out = StringPrintStream.of()) {
			var config = SpiPulseConfig.of(2);
			var buffer = config.buffer();
			var spi = SpiEmulator.pulsePrinter(out, config.cycle()).delay(false);
			sendPulses(spi, buffer, 0x85, 0xf3);
			Assert.equal(compactPulse(out.toString()), "10000101 11110011");
		}
	}

	@Test
	public void shouldPrintPulses() throws IOException {
		try (var out = StringPrintStream.of()) {
			var buffer = SpiPulseConfig.of(2).buffer();
			var spi = SpiEmulator.pulsePrinter(out).delay(false);
			sendPulses(spi, buffer, 0x85, 0xf3);
			Assert.equal(compactPulse(out.toString()), "1000010111110011");
		}
	}

	private static void sendPulses(Spi spi, PulseBuffer buffer, int... bytes) throws IOException {
		var xfer = spi.transfer(Direction.out, buffer.storageSize());
		buffer.setBytes(0, bytes);
		buffer.writePulseTo(xfer.out());
		xfer.execute();
	}

	private static final String compactPulse(String s) {
		s = s.replace("\u2587\u2587\u2581\u2581", "1");
		s = s.replace("\u2587\u2581\u2581\u2581", "0");
		return s.trim();
	}
}
