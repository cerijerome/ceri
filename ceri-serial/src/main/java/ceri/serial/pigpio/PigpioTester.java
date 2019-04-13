package ceri.serial.pigpio;

import static ceri.log.util.LogUtil.startupValues;
import java.io.IOException;
import ceri.common.util.BasicUtil;
import ceri.common.util.StartupValues;

public class PigpioTester {
	// private static final BinaryPrinter printer =
	// BinaryPrinter.builder().showChar(false).columns(1).bytesPerColumn(8).build();

	public static void main(String[] args) throws IOException {
		StartupValues v = startupValues(args);
		PigpioSpiChannel channel = PigpioSpiChannel.from(v.next("channel").asInt(0));
		int speed = v.next("speed").asInt(2400000);
		PigpioSpiFlags flags = PigpioSpiFlags.of(v.next("flags").asInt(0));
		int fill = v.next("fill").asInt(0xff);
		int size = v.next("size").asInt(1024);
		int repeat = v.next("repeat").asInt(1);
		int repeatDelay = v.next("repeatDelay").asInt(0);

		try (Pigpio pigpio = Pigpio.of()) {
			try (PigpioSpi spi = pigpio.spiOpen(channel, speed, flags)) {

				long t0 = System.currentTimeMillis();
				for (int i = 0; i < repeat; i++) {
					PigpioSpiTransfer xfer = PigpioSpiTransfer.out(spi, size);
					System.out.printf("Fill: 0x%02x x %d%n", fill, xfer.size());
					xfer.write(fill(xfer.size(), fill));
					xfer.execute();
					BasicUtil.delayMicros(repeatDelay);
				}
				long t1 = System.currentTimeMillis();
				System.out.printf("Time taken: %.2fs%n", (t1 - t0) / 1000.0);
			}
		}
	}

	private static byte[] fill(int size, int value) {
		byte[] buffer = new byte[size];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte) value;
		return buffer;
	}

}
