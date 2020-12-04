package ceri.serial.spi.pulse;

import static ceri.serial.spi.pulse.PulseCycle.Type.nbit;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit27;
import static ceri.serial.spi.pulse.PulseCycle.Type.nbit9;
import ceri.serial.spi.pulse.PulseCycle.Type;

/**
 * Standard cycle types and instances.
 */
public class PulseCycles {

	private PulseCycles() {}

	public static PulseStats pulseStats(PulseCycle cycle, int pulseFreqHz) {
		return cycle.stats(pulseFreqHz * cycle.pulseBits);
	}

	public static PulseCycle cycle(Type type, int n, int offset, int t0Bits, int t1Bits) {
		if (type == nbit27) return NBit27PulseCycle.of(n, offset, t0Bits, t1Bits);
		if (type == nbit9) return NBit9PulseCycle.of(n, offset, t0Bits, t1Bits);
		if (type == nbit) return NBitPulseCycle.of(n, offset, t0Bits, t1Bits);
		return null;
	}

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

		Std(Type type, int n, int offset, int t0Bits, int t1Bits) {
			cycle = cycle(type, n, offset, t0Bits, t1Bits);
		}
	}

}
