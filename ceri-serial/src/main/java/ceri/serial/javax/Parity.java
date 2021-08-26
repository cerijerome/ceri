package ceri.serial.javax;

import ceri.common.collection.EnumUtil;

public enum Parity {
	none(purejavacomm.SerialPort.PARITY_NONE),
	even(purejavacomm.SerialPort.PARITY_EVEN),
	odd(purejavacomm.SerialPort.PARITY_ODD),
	space(purejavacomm.SerialPort.PARITY_SPACE),
	mark(purejavacomm.SerialPort.PARITY_MARK);

	public final int value;

	Parity(int value) {
		this.value = value;
	}

	public static Parity from(int value) {
		return EnumUtil.find(Parity.class, t -> t.value == value);
	}

}
