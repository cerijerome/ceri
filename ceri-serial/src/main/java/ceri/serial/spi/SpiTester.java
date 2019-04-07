package ceri.serial.spi;

import static ceri.log.util.LogUtil.startupValues;
import java.io.IOException;
import ceri.common.util.BasicUtil;
import ceri.common.util.StartupValues;

public class SpiTester {
	// private static final BinaryPrinter printer =
	// BinaryPrinter.builder().showChar(false).columns(1).bytesPerColumn(8).build();

	public static void main(String[] args) throws IOException {
		StartupValues v = startupValues(args);
		String inOut = v.next("inOut").get("out");
		int size = v.next("size").asInt(1024);
		int speed = v.next("speed").asInt(2400000);
		int delay = v.next("delay").asInt(50);
		int mode = v.next("mode").asInt(0);
		int bus = v.next("bus").asInt(0);
		int chip = v.next("chip").asInt(0);
		int repeat = v.next("repeat").asInt(100);
		int repeatDelay = v.next("repeatDelay").asInt(0);
		Integer fill = v.next("fill").asInt();

		try (SpiDevice dev = openDevice(bus, chip, inOut)) {
			dev.mode(SpiMode.of(mode));
			dev.maxSpeedHz(speed);
			print(dev);

			//SpiTransfer xfer = dev.transfer(size).speedHz(speed).delayMicros(delay);
			SpiTransfer xfer = dev.transfer(size).delayMicros(delay);

			System.out.print("Fill:");
			long t0 = System.currentTimeMillis();
			for (int i = 0xff; i >= 0x00; i--) {
				System.out.printf(" %02x", i, xfer.size());
				int fillValue = fill != null ? fill : i;
				xfer.write(fill(xfer.size(), fillValue));
				for (int j = 0; j < repeat; j++) {
					xfer.execute();
					BasicUtil.delayMicros(repeatDelay);
				}
			}
			long t1 = System.currentTimeMillis();
			System.out.printf("\nTime taken: %.2fs%n", (t1 - t0) / 1000.0);
		}
	}

	private static SpiDevice openDevice(int bus, int chip, String inOut) throws IOException {
		switch (inOut) {
		case "i":
		case "in":
			return SpiDevice.openIn(bus, chip);
		case "o":
		case "out":
			return SpiDevice.openOut(bus, chip);
		default:
			return SpiDevice.openDuplex(bus, chip);
		}
	}

	private static void print(SpiDevice dev) throws IOException {
		System.out.printf("SpiDevice(#%x):%n", dev.hashCode());
		System.out.printf("       mode: %s%n", dev.mode());
		System.out.printf("bitsPerWord: %d%n", dev.bitsPerWord());
		System.out.printf("   lsbFirst: %s%n", dev.lsbFirst());
		System.out.printf(" maxSpeedHz: %d%n", dev.maxSpeedHz());
	}

	private static byte[] fill(int size, int value) {
		byte[] buffer = new byte[size];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte) value;
		return buffer;
	}

}
