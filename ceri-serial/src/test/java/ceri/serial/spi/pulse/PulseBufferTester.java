package ceri.serial.spi.pulse;

import static ceri.common.math.MathUtil.divideUp;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit9;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.PulsePrinter;
import ceri.serial.spi.pulse.PulseCycle.Type;
import ceri.serial.spi.pulse.PulseCycles.Std;

/**
 * Prints details on pulse buffer cycles.
 */
public class PulseBufferTester {
	private static final int DATA_HZ = 800_000;
	private static final BinaryPrinter printer =
		BinaryPrinter.builder().showHex(false).bytesPerColumn(8).columns(1).build();

	public static void main(String[] args) {
		for (Std cycle : Std.values())
			print(cycle, 8);
	}

	public static void print(Std std, int size) {
		print(std.cycle, size);
	}

	public static void print(Type type, int n, int offset, int t0Bits, int t1Bits, int size) {
		print(PulseCycles.cycle(type, n, offset, t0Bits, t1Bits), size);
	}

	private static void print(PulseCycle cycle, int size) {
		System.out.println("############################################################");
		System.out.printf("Cycle: %s n=%d off=%d t0=%d t1=%d Hz=%d%n", cycle.type(),
			cycle.pulseBits, cycle.pulseOffsetBits, cycle.t0Bits, cycle.t1Bits,
			cycle.pulseBits * DATA_HZ);
		System.out.println(PulseCycles.pulseStats(cycle, DATA_HZ));
		System.out.println("############################################################");
		printIndexes(cycle, size);
		printChanges(cycle.buffer(size));
	}

	private static void printIndexes(PulseCycle cycle, int size) {
		System.out.println("Bit indexes:");
		System.out.println("dbit  dB o    sB o sbit   sB     t0       t1     len");
		for (int i = 0; i < size * Byte.SIZE / 2; i++) {
			int t0 = cycle.t0Pos(i);
			PulseStats stats = PulseCycles.pulseStats(cycle, DATA_HZ);
			System.out.printf("(%03d) %02d %d => %02d %d (%03d) %3s %4sns(%d) %sns(%d) %sns%n", i,
				i / Byte.SIZE, i % Byte.SIZE, t0 / Byte.SIZE, t0 % Byte.SIZE, t0,
				cycle.storageBytes(divideUp(i + 1, Byte.SIZE)), (int) stats.t0Ns, cycle.t0Bits,
				(int) stats.t1Ns, cycle.t1Bits, (int) stats.pulseNs);
		}
		System.out.println();
	}

	private static void printChanges(PulseBuffer b) {
		System.out.println("Before changes:");
		printBuffer(b);
		printPulse(b);
		int val = 1;
		for (int i = 0; i < b.dataSize(); i++) {
			b.setByte(i, val);
			val <<= 1;
		}
		System.out.println("After changes:");
		printBuffer(b);
		printPulse(b);
		System.out.println();
	}

	private static void printBuffer(PulseBuffer b) {
		printer.print(b.buffer());
	}

	private static void printPulse(PulseBuffer b) {
		byte[] data = b.buffer().copy();
		Type type = b.cycle.type();
		if (type == nbit27) printPulse27(data);
		else if (type == nbit9) printPulse9(data);
		else printPulse(data, b.cycle.pulseBits);
	}

	private static void printPulse(byte[] data, int bits) {
		PulsePrinter p = PulsePrinter.ofBits((72 / bits) * bits);
		for (int i = 0; i < data.length; i++)
			p.print(data[i]);
		p.newLine();
	}

	private static void printPulse9(byte[] data) {
		PulsePrinter p = PulsePrinter.ofBits(9 * 8);
		for (int i = 0; i < data.length; i++)
			p.print(data[i]).print(false);
		p.newLine();
	}

	private static void printPulse27(byte[] data) {
		PulsePrinter p = PulsePrinter.ofBits(27 * 3);
		int i = 0;
		while (i < data.length) {
			int n = Math.min(3, data.length - i);
			if (n > 0) p.printBit(data[i], 0).print(data, i, n);
			if (n == 3) p.printBits(0, 2);
			i += n;
		}
		p.newLine();
	}

}
