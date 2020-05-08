package ceri.serial.spi.pulse;

import static ceri.common.math.MathUtil.lcm;
import static ceri.common.validation.ValidationUtil.validateMinL;
import static ceri.common.validation.ValidationUtil.validateRangeL;
import static java.lang.Math.min;

/**
 * Pulse cycle for signal with no padding between bytes.
 */
public class NBitPulseCycle extends PulseCycle {
	private static final int MIN_T0 = 1;
	private static final int MIN_T1 = MIN_T0 + 1;
	private static final int MIN_LEN = MIN_T1 + 1;

	public static NBitPulseCycle of(int n, int offset, int t0Bits, int t1Bits) {
		validateMinL(n, MIN_LEN, "Len");
		validateRangeL(offset, 0, n - MIN_T1, "Offset");
		// Min 1 more bit for t1, 1 bit for padding between pulses
		validateRangeL(t0Bits, MIN_T0, min(n - 1 - 1, n - 1 - offset), "t0 len");
		validateRangeL(t1Bits, t0Bits + 1, min(n - 1, n - offset), "t1 len");
		return new NBitPulseCycle(lcm(n, Byte.SIZE), n, offset, t0Bits, t1Bits);
	}

	private NBitPulseCycle(int cycleStorageBits, int pulseBits, int pulseOffsetBits, int t0Bits,
		int t1Bits) {
		super(cycleStorageBits, pulseBits, pulseOffsetBits, t0Bits, t1Bits);
	}

	@Override
	public Type type() {
		return Type.nbit;
	}

}
