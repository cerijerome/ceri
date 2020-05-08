package ceri.serial.spi.pulse;

import static ceri.common.math.MathUtil.divideUp;
import static ceri.common.validation.ValidationUtil.validateMinL;
import static ceri.common.validation.ValidationUtil.validateRangeL;
import static java.lang.Math.min;

/**
 * Pulse cycle for signal with 1+ bit padding after each byte.
 */
public class NBit9PulseCycle extends PulseCycle {
	private static final int CYCLE_STORAGE_BITS = Byte.SIZE;
	private static final int CYCLE_SIGNAL_BITS = CYCLE_STORAGE_BITS + 1;
	private static final int MIN_T0 = 1;
	private static final int MIN_T1 = MIN_T0 + 1;
	private static final int MIN_LEN = MIN_T1 + 1;

	public static NBit9PulseCycle of(int n, int offset, int t0Bits, int t1Bits) {
		validateMinL(n, MIN_LEN, "Len");
		int count = divideUp(Byte.SIZE - 1, n);
		int spareBits = CYCLE_STORAGE_BITS - MIN_T1 - ((count - 1) * n);
		validateRangeL(offset, 0, spareBits, "Offset");
		// Min 1 more bit for t1, 1 bit for padding between pulses
		validateRangeL(t0Bits, MIN_T0, min(n - 1 - 1, MIN_T0 + spareBits - offset), "t0 len");
		validateRangeL(t1Bits, t0Bits + 1, min(n - 1, MIN_T1 + spareBits - offset), "t1 len");
		return new NBit9PulseCycle(CYCLE_STORAGE_BITS, n, offset, t0Bits, t1Bits);
	}

	private NBit9PulseCycle(int cycleStorageBits, int pulseBits, int pulseOffsetBits, int t0Bits,
		int t1Bits) {
		super(cycleStorageBits, pulseBits, pulseOffsetBits, t0Bits, t1Bits);
	}

	@Override
	public Type type() {
		return Type.nbit9;
	}

	@Override
	public int cycleSignalBits() {
		return CYCLE_SIGNAL_BITS;
	}

}
