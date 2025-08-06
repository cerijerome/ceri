package ceri.serial.comm;

import ceri.common.collection.Enums;
import ceri.common.data.TypeTranscoder;
import ceri.common.math.MathUtil;
import ceri.serial.comm.jna.CSerial;

public enum StopBits {
	_1(CSerial.STOPBITS_1, 1.0),
	_2(CSerial.STOPBITS_2, 2.0),
	_1_5(CSerial.STOPBITS_1_5, 1.5);

	private static final TypeTranscoder<StopBits> xcoder =
		TypeTranscoder.of(t -> t.value, StopBits.class);
	private static final double PRECISION = 0.1;
	public final int value;
	public final double bits;

	public static StopBits from(int value) {
		return xcoder.decode(value);
	}

	public static StopBits fromBits(double bits) {
		return Enums.find(StopBits.class, t -> MathUtil.approxEqual(t.bits, bits, PRECISION));
	}

	private StopBits(int value, double bits) {
		this.value = value;
		this.bits = bits;
	}

	public int minBits() {
		return this == _1 ? 1 : 2;
	}
}
