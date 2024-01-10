package ceri.serial.spi.pulse;

import static ceri.common.exception.ExceptionUtil.exceptionf;
import static ceri.common.math.MathUtil.lcm;
import static ceri.common.util.BasicUtil.unused;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit9;
import static java.lang.Math.min;
import java.util.Objects;

/**
 * Encapsulates mapping logic to create pulse shapes from high and low bits, such as driving WS2812
 * led strips. T0 is the high (short) pulse, and T1 is the low (long) pulse. Some SPI drivers have a
 * gap between bytes, which allows for more compact 3-pulses per byte; 3/3/2 bits with 1 bit for T0,
 * 2 bits for T1. Otherwise 3 or 4 bits per pulse is typical, with the same T0 and T1 allocation.
 */
public class PulseCycle {
	private static final int MIN_T0 = 1;
	private static final int MIN_T1 = MIN_T0 + 1;
	private final int cycleStorageBytes;
	private final int cycleStorageBits;
	private final int cycleDataBits;
	public final int pulseOffsetBits;
	public final int t0Bits;
	public final int t1Bits;
	public final int pulseBits; // bit ratio
	public final Type type;

	/**
	 * Pulse bit cycle.
	 */
	public enum Type {
		nbit,
		nbit9,
		nbit27;
	}

	/**
	 * Standard pulse mappings.
	 */
	public static enum Std {
		_3(nbit, 3, 0, 1, 2),
		_3_9(nbit9, 3, 0, 1, 2),
		_4(nbit, 4, 0, 1, 2),
		_4_9(nbit9, 4, 0, 1, 2),
		_4_27(nbit27, 4, 2, 1, 2),
		_5(nbit, 5, 0, 1, 3),
		_5_9(nbit9, 5, 0, 1, 3),
		_5_27(nbit27, 5, 2, 1, 3),
		_6(nbit, 6, 0, 1, 3),
		_6_9(nbit9, 6, 0, 1, 2),
		_6_27(nbit27, 6, 2, 1, 3),
		_7(nbit, 7, 0, 2, 4),
		_7_9(nbit9, 7, 0, 2, 4),
		_7_27(nbit27, 7, 0, 2, 4),
		_8(nbit, 8, 0, 2, 4),
		_8_9(nbit9, 8, 0, 2, 4),
		_8_27(nbit27, 8, 0, 2, 4),
		_9(nbit, 9, 0, 2, 5),
		_9_9(nbit9, 9, 0, 2, 5),
		_9_27(nbit27, 9, 2, 2, 5);

		public final PulseCycle cycle;

		private Std(Type type, int n, int offset, int t0Bits, int t1Bits) {
			cycle = of(type, n, offset, t0Bits, t1Bits);
		}
	}

	/**
	 * Timing statistics based on pulse cycle and bit frequency.
	 */
	public static class Stats {
		private static final double HZ_IN_NS = 1_000_000_000.0;
		public final PulseCycle cycle;
		public final int freqHz;
		public final double bitNs; // nanos for 1 bit
		public final double pulseNs; // nanos for 1 pulse
		public final double t0Ns; // nanos for t0 pulse
		public final double t1Ns; // nanos for t1 pulse

		Stats(PulseCycle cycle, int freqHz) {
			this.cycle = cycle;
			this.freqHz = freqHz;
			bitNs = HZ_IN_NS / freqHz;
			t0Ns = cycle.t0Bits * bitNs;
			t1Ns = cycle.t1Bits * bitNs;
			pulseNs = cycle.pulseBits * bitNs;
		}

		@Override
		public int hashCode() {
			return Objects.hash(cycle, freqHz);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Stats other) && Objects.equals(cycle, other.cycle)
				&& other.freqHz == freqHz;
		}

		@Override
		public String toString() {
			return String.format("%s[t0=%.0fns, t1=%.0fns, pulse=%.0fns]",
				getClass().getSimpleName(), t0Ns, t1Ns, pulseNs);
		}
	}

	/**
	 * General pulse cycle constructor.
	 */
	public static PulseCycle of(Type type, int n, int offset, int t0Bits, int t1Bits) {
		if (type == nbit27) return nbit27(n, offset, t0Bits, t1Bits);
		if (type == nbit9) return nbit9(n, offset, t0Bits, t1Bits);
		if (type == nbit) return nbit(n, offset, t0Bits, t1Bits);
		throw exceptionf("Unsupported pulse cycle: type=%s n=%d, off=%d, t0=%d t1=%d", type, n,
			offset, t0Bits, t1Bits);
	}

	/**
	 * Pulse cycle for a signal with no padding between bytes.
	 */
	private static PulseCycle nbit(int n, int offset, int t0Bits, int t1Bits) {
		validateMin(n, MIN_T1 + 1, "Len");
		validateRange(offset, 0, n - MIN_T1, "Offset");
		// Min 1 more bit for t1, 1 bit for padding between pulses
		validateRange(t0Bits, MIN_T0, min(n - 1 - 1, n - 1 - offset), "t0 len");
		validateRange(t1Bits, t0Bits + 1, min(n - 1, n - offset), "t1 len");
		return new PulseCycle(lcm(n, Byte.SIZE), n, offset, t0Bits, t1Bits, Type.nbit);
	}

	/**
	 * 9-bit aligned pulse cycle for signal with 1 bit of padding after each byte. As seen on SPI0
	 * output for Raspberry Pi 3B.
	 */
	private static PulseCycle nbit9(int n, int offset, int t0Bits, int t1Bits) {
		validateMin(n, MIN_T1 + 1, "Len");
		int count = Math.ceilDiv(Byte.SIZE - 1, n);
		int spareBits = Byte.SIZE - MIN_T1 - ((count - 1) * n);
		validateRange(offset, 0, spareBits, "Offset");
		// Min 1 more bit for t1, 1 bit for padding between pulses
		int maxT0Bits = min(n - 1 - 1, MIN_T0 + spareBits - offset);
		int maxT1Bits = min(n - 1, MIN_T1 + spareBits - offset);
		validateRange(t0Bits, MIN_T0, maxT0Bits, "t0Bits");
		validateRange(t1Bits, t0Bits + 1, maxT1Bits, "t1Bits");
		return new PulseCycle(Byte.SIZE, n, offset, t0Bits, t1Bits, Type.nbit9) {
			@Override
			public int cycleSignalBits() {
				return Byte.SIZE + 1;
			}
		};
	}

	/**
	 * 27-bit aligned pulse cycle for signal with 2-3 bits of padding after 3 bytes, followed by an
	 * elongated first data bit. As seen on SPI1 output for Raspberry Pi 3B.
	 */
	private static PulseCycle nbit27(int n, int offset, int t0Bits, int t1Bits) {
		validateMin(n, MIN_T1 + 2, "Len");
		int minT0 = offset == 0 ? MIN_T0 + 1 : MIN_T0;
		int minT1 = minT0 + 1;
		int count = Math.ceilDiv(25 - 1, n);
		int cycleStorageBits = (3 * Byte.SIZE) + 1; // double-size fake bit 0
		int cycleSignalBits = cycleStorageBits + 2;
		int spareBits = cycleStorageBits - minT1 - ((count - 1) * n);
		if (offset != 0) validateRange(offset, 2, spareBits, "Offset");
		int maxT0Bits = min(n - 1 - 1, minT0 + spareBits - offset);
		int maxT1Bits = min(n - 1, minT1 + spareBits - offset);
		validateRange(t0Bits, minT0, maxT0Bits, "t0Bits");
		validateRange(t1Bits, t0Bits + 1, maxT1Bits, "t1Bits");
		return new PulseCycle(cycleStorageBits, n, offset, t0Bits, t1Bits, Type.nbit27) {
			@Override
			public int cycleSignalBits() {
				return cycleSignalBits;
			}

			@Override
			public int t0Pos(int dataBit) {
				return firstCycleBit(dataBit) ? super.t0Pos(dataBit) : super.t0Pos(dataBit) - 1;
			}

			@Override
			public int t0Bits(int dataBit) {
				return firstCycleBit(dataBit) ? super.t0Bits(dataBit) - 1 : super.t0Bits(dataBit);
			}

			@Override
			public int t1Bits(int dataBit) {
				return t0Bits(dataBit) + t1Bits - t0Bits;
			}

			private boolean firstCycleBit(int dataBit) {
				return pulseOffsetBits == 0 && subCycleBit(dataBit) == 0;
			}
		};
	}

	private PulseCycle(int cycleStorageBits, int pulseBits, int pulseOffsetBits, int t0Bits,
		int t1Bits, Type type) {
		this.cycleStorageBits = cycleStorageBits;
		this.cycleStorageBytes = cycleStorageBits / Byte.SIZE;
		this.cycleDataBits = cycleDataBits(cycleStorageBits, pulseBits, pulseOffsetBits, t1Bits);
		this.pulseBits = pulseBits;
		this.pulseOffsetBits = pulseOffsetBits;
		this.t0Bits = t0Bits;
		this.t1Bits = t1Bits;
		this.type = type;
	}

	/**
	 * The number of signal bits in a cycle.
	 */
	public int cycleSignalBits() {
		return cycleStorageBits;
	}

	/**
	 * Calculates the number of bytes required to store the data as pulses.
	 */
	public int storageBytes(int dataBytes) {
		int dataBit = (dataBytes * Byte.SIZE) - 1;
		int pos = t0Pos(dataBit) + t1Bits(dataBit);
		return Math.ceilDiv(pos, Byte.SIZE);
	}

	/**
	 * Returns timing statistics based on bit frequency.
	 */
	public Stats stats(int bitFreqHz) {
		return new Stats(this, bitFreqHz);
	}

	/**
	 * Returns timing statistics based on pulse frequency.
	 */
	public Stats pulseStats(int pulseFreqHz) {
		return stats(pulseFreqHz * pulseBits);
	}

	/**
	 * Creates a buffer for pulse data to be stored.
	 */
	public PulseBuffer buffer(int dataSize) {
		return new PulseBuffer(this, dataSize);
	}

	/**
	 * Returns the pulse start bit index for the data bit.
	 */
	public int t0Pos(int dataBit) {
		int cycles = cycle(dataBit);
		int subCycleBit = subCycleBit(dataBit);
		return offset(cycles) + (subCycleBit * pulseBits) + pulseOffsetBits;
	}

	/**
	 * Returns the number of bits in the off pulse.
	 */
	public int t0Bits(int dataBit) {
		unused(dataBit);
		return t0Bits;
	}

	/**
	 * Returns the number of bits in the on pulse.
	 */
	public int t1Bits(int dataBit) {
		unused(dataBit);
		return t1Bits;
	}

	/**
	 * Returns the pulse cycles index from the data bit index.
	 */
	public int cycle(int dataBit) {
		return dataBit / cycleDataBits;
	}

	/**
	 * Returns the bit index within a pulse cycle from the data bit index.
	 */
	public int subCycleBit(int dataBit) {
		return dataBit % cycleDataBits;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cycleStorageBits, pulseBits, pulseOffsetBits, t0Bits, t1Bits);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PulseCycle other)) return false;
		if (cycleStorageBits != other.cycleStorageBits) return false;
		if (pulseBits != other.pulseBits) return false;
		if (pulseOffsetBits != other.pulseOffsetBits) return false;
		if (t0Bits != other.t0Bits) return false;
		if (t1Bits != other.t1Bits) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d%s(%d+%d,%d:%d)", pulseBits, type, cycleStorageBits,
			pulseOffsetBits, t0Bits, t1Bits);
	}

	private int offset(int cycles) {
		return cycles * cycleStorageBytes * Byte.SIZE;
	}

	private int cycleDataBits(int cycleStorageBits, int pulseBits, int pulseOffsetBits,
		int t1Bits) {
		int availableBits = cycleStorageBits - pulseOffsetBits;
		int cycleDataBits = availableBits / pulseBits;
		if (availableBits % pulseBits >= t1Bits) cycleDataBits++;
		return cycleDataBits;
	}
}
