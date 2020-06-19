package ceri.serial.spi.pulse;

import static ceri.common.math.MathUtil.ceilDiv;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import static java.lang.Math.min;

/**
 * Pulse cycle for signal with no padding between bytes.
 */
public class NBit27PulseCycle extends PulseCycle {
	private static final int CYCLE_STORAGE_BITS = (3 * Byte.SIZE) + 1; // double-size fake bit 0
	private static final int CYCLE_SIGNAL_BITS = CYCLE_STORAGE_BITS + 2;
	private static final int MIN_T0 = 1; // for offset > 0
	private static final int MIN_T1 = MIN_T0 + 1; // for offset > 0
	private static final int MIN_LEN = MIN_T1 + 2;

	public static NBit27PulseCycle of(int n, int offset, int t0Bits, int t1Bits) {
		validateMin(n, MIN_LEN, "Len");
		int minT0 = offset == 0 ? MIN_T0 + 1 : MIN_T0;
		int minT1 = minT0 + 1;
		int count = ceilDiv(25 - 1, n);
		int spareBits = CYCLE_STORAGE_BITS - minT1 - ((count - 1) * n);
		if (offset != 0) validateRange(offset, 2, spareBits, "Offset");
		validateRange(t0Bits, minT0, min(n - 1 - 1, minT0 + spareBits - offset), "t0 len");
		validateRange(t1Bits, t0Bits + 1, min(n - 1, minT1 + spareBits - offset), "t1 len");
		return new NBit27PulseCycle(n, offset, t0Bits, t1Bits);
	}

	private NBit27PulseCycle(int pulseBits, int pulseOffsetBits, int t0Bits, int t1Bits) {
		super(CYCLE_STORAGE_BITS, pulseBits, pulseOffsetBits, t0Bits, t1Bits);
	}

	@Override
	public Type type() {
		return Type.nbit27;
	}

	@Override
	public int cycleSignalBits() {
		return CYCLE_SIGNAL_BITS;
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

}
