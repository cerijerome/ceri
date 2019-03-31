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
		int chip = v.next("chip").asInt(0);
		int repeat = v.next("repeat").asInt(100);
		int repeatDelay = v.next("repeatDelay").asInt(0);

		try (SpiDevice dev = openDevice(chip, inOut)) {
			dev.mode(SpiMode.of(mode));
			dev.maxSpeedHz(speed);
			print(dev);

			SpiTransfer xfer =
				transfer(size, inOut).speedHz(speed).bitsPerWord(8).delayMicros(delay);

			System.out.print("Fill:");
			for (int i = 0xff; i >= 0x00; i--) {
				System.out.printf(" %02x", i, xfer.size());
				xfer.write(fill(xfer.size(), i));
				for (int j = 0; j < repeat; j++) {
					xfer.transfer(dev);
					BasicUtil.delayMicros(repeatDelay);
				}
			}
			System.out.println();
		}
	}

	private static SpiDevice openDevice(int chip, String inOut) throws IOException {
		switch (inOut) {
		case "i":
		case "in":
			return SpiDevice.openIn(0, chip);
		case "o":
		case "out":
			return SpiDevice.openOut(0, chip);
		default:
			return SpiDevice.openInOut(0, chip);
		}
	}

	private static SpiTransfer transfer(int size, String inOut) {
		switch (inOut) {
		case "i":
		case "in":
			return SpiTransfer.in(size);
		case "o":
		case "out":
			return SpiTransfer.out(size);
		default:
			return SpiTransfer.duplex(size);
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
