package ceri.serial.spi.util;

import static ceri.log.util.LogUtil.startupValues;
import java.io.IOException;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.io.Direction;
import ceri.common.util.StartupValues;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiDevice;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;

public class SpiTester {

	public static void main(String[] args) throws IOException {
		StartupValues v = startupValues(Level.WARN, args);
		Direction direction = v.next("direction", p -> p.toEnum(Direction.out));
		int size = v.next("size", p -> p.toInt(8));
		int speedHz = v.next("speedHz", p -> p.toInt(100000));
		int delay = v.next("delay", p -> p.toInt(50));
		int mode = v.next("mode", p -> p.toInt(0));
		int bus = v.next("bus", p -> p.toInt(0));
		int chip = v.next("chip", p -> p.toInt(0));
		int fill = v.next("fill", p -> p.toInt(0xff));
		int repeat = v.next("repeat", p -> p.toInt(1));
		int repeatDelayMs = v.next("repeatDelayMs", p -> p.toInt(0));

		try (var fd = SpiDevice.Config.of(bus, chip, direction).open()) {
			// Spi spi = SpiEmulator.echo();
			Spi spi = SpiDevice.of(fd);
			spi.mode(new SpiMode(mode));
			spi.maxSpeedHz(speedHz);
			print(spi);

			SpiTransfer xfer = spi.transfer(Direction.out, size).delayMicros(delay);

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
