package ceri.serial.spi.util;

import static ceri.log.util.LogUtil.startupValues;
import java.io.IOException;
import java.util.Arrays;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.util.StartupValues;
import ceri.serial.spi.Spi;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.SpiDevice;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;

public class SpiTester {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		StartupValues v = startupValues(args);
		Direction direction = v.next("direction").apply(Direction::valueOf, Direction.out);
		int size = v.next("size").asInt(8);
		int speedHz = v.next("speedHz").asInt(100000);
		int delay = v.next("delay").asInt(50);
		int mode = v.next("mode").asInt(0);
		int bus = v.next("bus").asInt(0);
		int chip = v.next("chip").asInt(0);
		int fill = v.next("fill").asInt(0xff);
		int repeat = v.next("repeat").asInt(1);
		int repeatDelayMs = v.next("repeatDelayMs").asInt(0);

		// try (Spi spi = SpiEmulator.echo(bus, chip, direction)) {
		try (Spi spi = SpiDevice.open(bus, chip, direction)) {
			spi.mode(SpiMode.of(mode));
			spi.maxSpeedHz(speedHz);
			print(spi);

			SpiTransfer xfer = spi.transfer(size).delayMicros(delay);

			System.out.printf("Fill: 0x%02x x %d%n", fill, xfer.size());
			xfer.write(fill(xfer.size(), fill));
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < repeat; i++) {
				xfer.execute();
				ConcurrentUtil.delay(repeatDelayMs);
			}
			long t1 = System.currentTimeMillis();
			System.out.printf("Time taken: %.2fs%n", (t1 - t0) / 1000.0);
		}
	}

	private static void print(Spi spi) throws IOException {
		System.out.printf("SpiDevice(#%x):%n", spi.hashCode());
		System.out.printf("       mode: %s%n", spi.mode());
		System.out.printf("bitsPerWord: %d%n", spi.bitsPerWord());
		System.out.printf("   lsbFirst: %s%n", spi.lsbFirst());
		System.out.printf(" maxSpeedHz: %d%n", spi.maxSpeedHz());
	}

	private static byte[] fill(int size, int value) {
		byte[] buffer = new byte[size];
		Arrays.fill(buffer, (byte) value);
		return buffer;
	}

}
