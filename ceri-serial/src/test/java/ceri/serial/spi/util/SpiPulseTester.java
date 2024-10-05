package ceri.serial.spi.util;

import java.io.IOException;
import java.util.function.Supplier;
import org.apache.logging.log4j.Level;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.io.Direction;
import ceri.common.math.MathUtil;
import ceri.common.util.StartupValues;
import ceri.log.util.LogUtil;
import ceri.serial.spi.Spi;
import ceri.serial.spi.SpiDevice;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.pulse.PulseCycle;
import ceri.serial.spi.pulse.SpiPulseConfig;
import ceri.serial.spi.pulse.SpiPulseTransmitter;

public class SpiPulseTester {

	public static void main(String[] args) throws IOException {
		args = new String[] { "5", "1" };
		StartupValues v = LogUtil.startupValues(Level.WARN, args);
		int size = v.next("size", p -> p.toInt(4));
		int bus = v.next("bus", p -> p.toInt(0));
		int chip = v.next("chip", p -> p.toInt(0));
		int speed = v.next("speed", p -> p.toInt(3200000));
		int delayMicros = v.next("delay", p -> p.toInt(50));
		SpiMode mode = new SpiMode(v.next("mode", p -> p.toInt(0)));
		PulseCycle.Type pulseType = v.next("pulseType", p -> p.toEnum(PulseCycle.Type.nbit9));
		int pulseBits = v.next("pulseBits", p -> p.toInt(4));
		int pulseOffset = v.next("pulseOffset", p -> p.toInt(0));
		int pulseT0 = v.next("pulseT0", p -> p.toInt(1));
		int pulseT1 = v.next("pulseT1", p -> p.toInt(2));

		SpiPulseConfig config = SpiPulseConfig.builder(size)
			.cycle(PulseCycle.of(pulseType, pulseBits, pulseOffset, pulseT0, pulseT1))
			.delayMicros(delayMicros).build();
		try (var fd = SpiDevice.Config.of(bus, chip, Direction.out).open()) {
			Spi spi = SpiDevice.of(fd);
			spi.mode(mode);
			spi.maxSpeedHz(speed);
			try (SpiPulseTransmitter processor = SpiPulseTransmitter.of(1, spi, config)) {
				ConcurrentUtil.delay(1000);
				runCycles(processor);
			}
		}
	}

	private static void runCycles(SpiPulseTransmitter spi) {
		while (true) {
			cycle(spi, () -> fill(spi.length(), 0xff, 0, 0));
			cycle(spi, () -> fill(spi.length(), 0, 0xff, 0));
			cycle(spi, () -> fill(spi.length(), 0, 0, 0xff));
			cycle(spi, () -> fill(spi.length(), 0xff, 0xff, 0));
			cycle(spi, () -> fill(spi.length(), 0, 0xff, 0xff));
			cycle(spi, () -> fill(spi.length(), 0xff, 0, 0xff));
			cycle(spi, () -> fill(spi.length(), 0xff));
			cycle(spi, () -> fill(spi.length(), 0));
			cycle(spi, () -> rnd(spi.length()));
		}
	}

	private static void cycle(SpiPulseTransmitter spi, Supplier<byte[]> supplier) {
		cycle(spi, supplier, 1);
	}

	private static void cycle(SpiPulseTransmitter spi, Supplier<byte[]> supplier, int cycles) {
		byte[] data = supplier.get();
		System.out.print("Cycle started:");
		for (int i = 0; i < Math.min(3, data.length); i++)
			System.out.printf(" 0x%02x", data[i]);
		System.out.println("...");
		for (int i = 0; i < cycles; i++) {
			spi.copyFrom(0, data);
			spi.send();
			ConcurrentUtil.delay(200);
		}
		System.out.println("stopped");
		ConcurrentUtil.delay(1000);
	}

	private static byte[] fill(int size, int... values) {
		if (values == null || values.length == 0) values = new int[] { 0 };
		byte[] data = new byte[size];
		int i = 0;
		for (int j = 0; j < data.length; j++) {
			data[j] = (byte) values[i++];
			if (i >= values.length) i = 0;
		}
		return data;
	}

	private static byte[] rnd(int size) {
		byte[] rnd = new byte[size];
		for (int i = 0; i < rnd.length; i++)
			rnd[i] = (byte) MathUtil.random(0, 0xff);
		return rnd;
	}

}
