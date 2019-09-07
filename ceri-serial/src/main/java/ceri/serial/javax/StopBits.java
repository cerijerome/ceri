package ceri.serial.javax;

import static ceri.common.math.MathUtil.approxEqual;
import ceri.common.util.BasicUtil;

public enum StopBits {
	_1(1.0, purejavacomm.SerialPort.STOPBITS_1),
	_1_5(1.5, purejavacomm.SerialPort.STOPBITS_1_5),
	_2(2.0, purejavacomm.SerialPort.STOPBITS_2);

	private static final double PRECISION = 0.1;
	public final int value;
	public final double actual;

	StopBits(double actual, int value) {
		this.value = value;
		this.actual = actual;
	}

	public static StopBits from(int value) {
		return BasicUtil.find(StopBits.class, t -> t.value == value);
	}

	public static StopBits fromActual(double actual) {
		return BasicUtil.find(StopBits.class, t -> approxEqual(t.actual, actual, PRECISION));
	}

}
